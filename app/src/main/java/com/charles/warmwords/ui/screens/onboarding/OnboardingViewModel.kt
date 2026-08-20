package com.charles.warmwords.ui.screens.onboarding

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.charles.warmwords.ai.DownloadWorkerInput
import com.charles.warmwords.ai.ModelDownloadWorker
import com.charles.warmwords.data.local.entity.UserProfile
import com.charles.warmwords.domain.usecase.UserProfileUseCases
import com.charles.warmwords.translation.TranslationDownloadState
import com.charles.warmwords.translation.TranslationManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
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

            workManager.enqueueUniqueWork(
                "model_download_work",
                ExistingWorkPolicy.REPLACE,
                workRequest
            )
            Log.d("OnboardingVM", "Work enqueued successfully")

            viewModelScope.launch {
                workManager.getWorkInfoByIdLiveData(workRequest.id).observeForever { info ->
                    if (info != null) {
                        val progressInt = info.progress.getInt("progress_percent", 0)
                        val progress = progressInt / 100f
                        Log.d("OnboardingVM", "WorkInfo received: state=${info.state}, progress=${progress}")
                        _uiState.value = _uiState.value.copy(
                            isDownloading = !info.state.isFinished,
                            downloadProgress = progress
                        )

                        if (info.state.isFinished) {
                            if (info.state == androidx.work.WorkInfo.State.FAILED) {
                                val error = info.outputData.getString("download_error")
                                _uiState.value = _uiState.value.copy(
                                    downloadError = error ?: "Download failed. Please check your network connection."
                                )
                            } else {
                                _uiState.value = _uiState.value.copy(
                                    isComplete = true,
                                    downloadProgress = 1f
                                )
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("OnboardingVM", "Error starting model download", e)
            _uiState.value = _uiState.value.copy(
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
        _uiState.value = _uiState.value.copy(downloadError = null, isDownloading = true)
        startModelDownload()
    }

    fun setShowLowRamWarning(value: Boolean) {
        _uiState.value = _uiState.value.copy(showLowRamWarning = value)
    }
}
