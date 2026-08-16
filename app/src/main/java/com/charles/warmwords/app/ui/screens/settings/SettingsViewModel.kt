package com.charles.warmwords.app.ui.screens.settings

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.GsonBuilder
import com.charles.warmwords.app.ai.Persona
import com.charles.warmwords.app.ai.SystemPrompt
import com.charles.warmwords.app.ai.TextToSpeechManager
import com.charles.warmwords.app.ai.WarmVoice
import com.charles.warmwords.app.analytics.AnalyticsManager
import com.charles.warmwords.app.ads.AdsPreferences
import com.charles.warmwords.app.data.local.entity.UserProfile
import com.charles.warmwords.app.domain.usecase.ChatUseCases
import com.charles.warmwords.app.domain.usecase.JournalUseCases
import com.charles.warmwords.app.domain.usecase.UserProfileUseCases
import com.charles.warmwords.app.util.ModelConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class SettingsUiState(
    val profile: UserProfile? = null,
    val modelDownloaded: Boolean = false,
    val modelSizeBytes: Long = 0L,
    val journalCount: Int = 0,
    val availableVoices: List<WarmVoice> = emptyList()
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userProfileUseCases: UserProfileUseCases,
    private val chatUseCases: ChatUseCases,
    private val journalUseCases: JournalUseCases,
    private val textToSpeechManager: TextToSpeechManager,
    private val analyticsManager: AnalyticsManager,
    private val adsPreferences: AdsPreferences,
    @ApplicationContext private val context: Context
) : ViewModel() {

    val personalizedAdsEnabled: Boolean
        get() = adsPreferences.personalizedAdsEnabled

    fun setPersonalizedAdsEnabled(enabled: Boolean) {
        adsPreferences.setPersonalizedAdsEnabled(enabled)
    }

    val personas: List<Persona> = SystemPrompt.PERSONAS

    val uiState: StateFlow<SettingsUiState> = combine(
        userProfileUseCases.profile,
        textToSpeechManager.isReady
    ) { profile, ttsReady ->
        val modelDownloaded = profile?.modelDownloaded ?: false
        SettingsUiState(
            profile = profile,
            modelDownloaded = modelDownloaded,
            modelSizeBytes = if (modelDownloaded) ModelConfig.MODEL_TOTAL_BYTES else 0L,
            availableVoices = if (ttsReady) textToSpeechManager.availableVoices() else emptyList()
        )
    }.stateIn(viewModelScope, SharingStarted.Lazily, SettingsUiState())

    fun deleteAllData(onComplete: () -> Unit) {
        viewModelScope.launch {
            chatUseCases.deleteAll()
            journalUseCases.deleteAll()
            userProfileUseCases.deleteAll()

            val modelDir = File(
                context.getExternalFilesDir(null),
                "models/${ModelConfig.MODEL_VERSION}/${ModelConfig.MODEL_FILE_NAME}"
            )
            if (modelDir.exists()) {
                modelDir.delete()
            }

            val cacheDir = File(
                context.getExternalFilesDir(null),
                "models/${ModelConfig.MODEL_VERSION}"
            )
            if (cacheDir.exists()) {
                cacheDir.deleteRecursively()
            }

            analyticsManager.logEvent(AnalyticsManager.EVENT_DATA_DELETED)
            onComplete()
        }
    }

    fun updateProfile(name: String, pronouns: String) {
        viewModelScope.launch {
            val existing = userProfileUseCases.getProfile() ?: UserProfile()
            userProfileUseCases.updateProfile(
                existing.copy(name = name, pronouns = pronouns)
            )
        }
    }

    fun setVoiceRepliesEnabled(enabled: Boolean) {
        viewModelScope.launch {
            val existing = userProfileUseCases.getProfile() ?: UserProfile()
            userProfileUseCases.updateProfile(existing.copy(voiceRepliesEnabled = enabled))
        }
        analyticsManager.logEvent(AnalyticsManager.EVENT_VOICE_REPLIES_TOGGLED, mapOf("enabled" to enabled.toString()))
    }

    fun setPersona(personaId: String) {
        viewModelScope.launch {
            val existing = userProfileUseCases.getProfile() ?: UserProfile()
            userProfileUseCases.updateProfile(existing.copy(selectedPersonaId = personaId))
        }
        analyticsManager.logEvent(AnalyticsManager.EVENT_PERSONA_CHANGED, mapOf(AnalyticsManager.PARAM_PERSONA_ID to personaId))
    }

    fun setVoice(voiceName: String) {
        viewModelScope.launch {
            val existing = userProfileUseCases.getProfile() ?: UserProfile()
            userProfileUseCases.updateProfile(existing.copy(selectedVoiceName = voiceName))
        }
        textToSpeechManager.applyVoice(voiceName)
        analyticsManager.logEvent(AnalyticsManager.EVENT_VOICE_CHANGED)
    }

    fun previewVoice(voiceName: String) {
        textToSpeechManager.previewVoice(voiceName)
    }

    fun exportData(onResult: (Uri?) -> Unit) {
        viewModelScope.launch {
            try {
                val entries = journalUseCases.getAllEntriesOnce()
                val gson = GsonBuilder().setPrettyPrinting().create()
                val json = gson.toJson(mapOf("journalEntries" to entries))

                val exportsDir = File(context.getExternalFilesDir(null), "exports").apply { mkdirs() }
                val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                val exportFile = File(exportsDir, "warmword_export_$timestamp.json")
                exportFile.writeText(json)

                val uri = FileProvider.getUriForFile(
                    context,
                    "com.charles.warmwords.app.fileprovider",
                    exportFile
                )
                analyticsManager.logEvent(AnalyticsManager.EVENT_DATA_EXPORTED)
                onResult(uri)
            } catch (e: Exception) {
                Log.e("SettingsViewModel", "Export failed", e)
                analyticsManager.recordNonFatal(e)
                onResult(null)
            }
        }
    }
}
