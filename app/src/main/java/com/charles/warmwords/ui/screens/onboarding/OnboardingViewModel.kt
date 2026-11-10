package com.charles.warmwords.ui.screens.onboarding

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.charles.warmwords.ai.DownloadWorkerInput
import com.charles.warmwords.ai.DownloadWorkerOutput
import com.charles.warmwords.ai.ModelDownloadWorker
import com.charles.warmwords.data.local.entity.UserProfile
import com.charles.warmwords.domain.usecase.UserProfileUseCases
import com.charles.warmwords.translation.TranslationDownloadState
import com.charles.warmwords.translation.TranslationManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject
import javax.inject.Named

data class OnboardingUiState(
    val currentPage: Int = 0,
    val isDownloading: Boolean = false,
    val downloadProgress: Float = 0f,
    val downloadError: String? = null,
    val isComplete: Boolean = false,
    val showLowRamWarning: Boolean = false,
    val name: String = "",
    val selectedLanguage: String = "en",
    val translationDownloadState: TranslationDownloadState = TranslationDownloadState.Idle
)

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val userProfileUseCases: UserProfileUseCases,
    private val translationManager: TranslationManager,
    private val workManager: WorkManager,
    @Named("modelDownloadUrl") private val modelDownloadUrl: String,
    @Named("modelSha256") private val modelSha256: String,
    @Named("modelFileName") private val modelFileName: String,
    private val modelTotalBytes: Long
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState

    init {
        Log.d("OnboardingVM", "Init - url=$modelDownloadUrl, file=$modelFileName, total=$modelTotalBytes")
        startModelDownload()
        viewModelScope.launch {
            val stored = userProfileUseCases.getProfile()?.translationLanguageCode
            if (!stored.isNullOrBlank()) {
                _uiState.value = _uiState.value.copy(selectedLanguage = stored.lowercase())
            }
        }
        viewModelScope.launch {
            translationManager.downloadState.collect { state ->
                _uiState.value = _uiState.value.copy(translationDownloadState = state)
            }
        }
    }

    private var downloadWatch: Job? = null

    private fun startModelDownload() {
        try {
            val workRequest = OneTimeWorkRequestBuilder<ModelDownloadWorker>()
                .setInputData(
                    androidx.work.Data.Builder()
                        .putString(DownloadWorkerInput.KEY_MODEL_URL, modelDownloadUrl)
                        .putString(DownloadWorkerInput.KEY_MODEL_FILE_NAME, modelFileName)
                        .putLong(DownloadWorkerInput.KEY_MODEL_TOTAL_BYTES, modelTotalBytes)
                        .putString(DownloadWorkerInput.KEY_MODEL_SHA256, modelSha256)
                        .build()
                )
                .build()

            // KEEP: a download already running (screen rotated, app reopened) carries on instead of being
            // cancelled and restarted; a finished or failed one is replaced by this new request.
            workManager.enqueueUniqueWork(
                MODEL_WORK_NAME,
                ExistingWorkPolicy.KEEP,
                workRequest
            )
            Log.d("OnboardingVM", "Work enqueued successfully")

            downloadWatch?.cancel()
            downloadWatch = viewModelScope.launch {
                workManager.getWorkInfosForUniqueWorkFlow(MODEL_WORK_NAME).collect { infos ->
                    val info = infos.lastOrNull() ?: return@collect
                    val progressInt = info.progress.getInt("progress_percent", 0)
                    Log.d("OnboardingVM", "WorkInfo received: state=${info.state}, progress=$progressInt")
                    when (info.state) {
                        WorkInfo.State.SUCCEEDED -> {
                            // Only "Ready" once the model file is really on the phone, complete.
                            val path = info.outputData.getString(DownloadWorkerOutput.KEY_MODEL_PATH)
                            val f = path?.let { File(it) }
                            val present = f != null && f.exists() &&
                                (modelTotalBytes <= 0 || f.length() == modelTotalBytes)
                            _uiState.value = if (present) {
                                _uiState.value.copy(isDownloading = false, isComplete = true, downloadError = null, downloadProgress = 1f)
                            } else {
                                _uiState.value.copy(isDownloading = false, isComplete = false,
                                    downloadError = "The AI model didn't finish saving on your phone. Tap Retry to download it again.")
                            }
                        }
                        WorkInfo.State.FAILED, WorkInfo.State.CANCELLED -> {
                            val error = info.outputData.getString(DownloadWorkerOutput.KEY_DOWNLOAD_ERROR)
                            _uiState.value = _uiState.value.copy(
                                isDownloading = false,
                                isComplete = false,
                                downloadError = error ?: "The download stopped. Check your connection and tap Retry."
                            )
                        }
                        else -> {
                            _uiState.value = _uiState.value.copy(
                                isDownloading = true,
                                isComplete = false,
                                downloadError = null,
                                downloadProgress = if (progressInt > 0) progressInt / 100f else _uiState.value.downloadProgress
                            )
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("OnboardingVM", "Error starting model download", e)
            _uiState.value = _uiState.value.copy(
                isDownloading = false,
                isComplete = false,
                downloadError = e.message ?: "Failed to start download: ${e.javaClass.simpleName}"
            )
        }
    }

    fun saveProfile(name: String) {
        _uiState.value = _uiState.value.copy(name = name)
        viewModelScope.launch {
            val existing = userProfileUseCases.getProfile() ?: UserProfile()
            userProfileUseCases.saveProfile(
                existing.copy(
                    name = name,
                    modelDownloaded = true,
                    modelDownloadComplete = true,
                    onboardingComplete = true
                )
            )
        }
    }

    fun nextPage() {
        val currentPage = _uiState.value.currentPage
        if (currentPage < 3) {
            _uiState.value = _uiState.value.copy(currentPage = currentPage + 1)
        }
    }

    /** Selects the translation language (blank/"en" = no translation). */
    fun selectLanguage(code: String) {
        _uiState.value = _uiState.value.copy(selectedLanguage = code.lowercase())
        translationManager.selectLanguage(code)
    }

    fun previousPage() {
        val currentPage = _uiState.value.currentPage
        if (currentPage > 0) {
            _uiState.value = _uiState.value.copy(currentPage = currentPage - 1)
        }
    }

    fun retryDownload() {
        _uiState.value = _uiState.value.copy(downloadError = null, isDownloading = true, isComplete = false)
        startModelDownload()
    }

    private companion object {
        const val MODEL_WORK_NAME = "model_download_work"
    }

    fun setShowLowRamWarning(value: Boolean) {
        _uiState.value = _uiState.value.copy(showLowRamWarning = value)
    }
}
