package com.charles.warmwords.translation

import android.util.Log
import com.charles.warmwords.analytics.AnalyticsManager
import com.charles.warmwords.data.local.entity.UserProfile
import com.charles.warmwords.domain.usecase.UserProfileUseCases
import com.charles.warmwords.performance.PerformanceManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.LinkedHashMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * App-level coordinator for on-device ML Kit translation.
 *
 * Holds the user's chosen target language (persisted on the [UserProfile]),
 * drives the model download, and caches translations so already-seen text is
 * translated once. All translation happens on-device and nothing is sent to a
 * WarmWord server.
 */
@Singleton
class TranslationManager @Inject constructor(
    private val userProfileUseCases: UserProfileUseCases,
    private val deviceTranslator: DeviceTranslator,
    private val analyticsManager: AnalyticsManager,
    private val performanceManager: PerformanceManager
) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /** Selected target language code; blank or "en" means "no translation". */
    private val _targetLanguage = MutableStateFlow(DEFAULT_TARGET)
    val targetLanguage: StateFlow<String> = _targetLanguage.asStateFlow()

    private val _downloadState = MutableStateFlow<TranslationDownloadState>(TranslationDownloadState.Idle)
    val downloadState: StateFlow<TranslationDownloadState> = _downloadState.asStateFlow()

    private val cache = object : LinkedHashMap<String, String>(32, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, String>?): Boolean = size > 800
    }

    val isActive: Boolean
        get() {
            val code = _targetLanguage.value
            return code.isNotBlank() && code != "en"
        }

    val currentLanguage: String
        get() = _targetLanguage.value

    init {
        scope.launch {
            val stored = userProfileUseCases.getProfile()?.translationLanguageCode?.lowercase().orEmpty()
            _targetLanguage.value = stored
            if (stored.isNotBlank() && stored != "en") {
                deviceTranslator.setTarget(stored)
                if (deviceTranslator.isModelDownloaded(stored)) {
                    _downloadState.value = TranslationDownloadState.Ready(stored)
                } else {
                    downloadModel(stored)
                }
            }
        }
    }

    /**
     * Persists the user's language and (re)starts the model download when a real
     * language (not English) is selected. Calling with blank/"en" turns
     * translation off and frees the previously downloaded model.
     */
    fun selectLanguage(code: String) {
        val normalized = code.lowercase()
        val previous = _targetLanguage.value
        val previousBase = previous.substringBefore('-')
        val newBase = normalized.substringBefore('-')
        _targetLanguage.value = normalized

        scope.launch {
            val profile = userProfileUseCases.getProfile() ?: UserProfile()
            userProfileUseCases.updateProfile(
                profile.copy(translationLanguageCode = normalized.ifBlank { null })
            )

            if (newBase == "en" || normalized.isBlank()) {
                deviceTranslator.setTarget(normalized)
                if (previousBase.isNotBlank() && previousBase != "en" && previousBase != newBase) {
                    deviceTranslator.deleteModel(previousBase)
                }
                _downloadState.value = TranslationDownloadState.Idle
                analyticsManager.logEvent(
                    AnalyticsManager.EVENT_LANGUAGE_CHANGED,
                    mapOf(AnalyticsManager.PARAM_LANGUAGE to "en")
                )
            } else {
                deviceTranslator.setTarget(if (normalized.length < 7) newBase else normalized)
                if (previousBase != newBase) {
                    _downloadState.value = TranslationDownloadState.Idle
                    downloadModel(newBase)
                }
                analyticsManager.logEvent(
                    AnalyticsManager.EVENT_LANGUAGE_CHANGED,
                    mapOf(AnalyticsManager.PARAM_LANGUAGE to newBase)
                )
            }
        }
    }

    /** Deletes the downloaded model and switches translation off (used by "Delete all data"). */
    suspend fun deleteCurrentModel() {
        val current = _targetLanguage.value.substringBefore('-')
        if (current.isNotBlank() && current != "en") {
            deviceTranslator.deleteModel(current)
        }
        deviceTranslator.setTarget("")
        _targetLanguage.value = ""
        _downloadState.value = TranslationDownloadState.Idle
    }

    /** Re-attempts the download for the currently selected language after a failure. */
    fun retryLanguageDownload() {
        val current = _targetLanguage.value
        if (current.isNotBlank() && current != "en") {
            scope.launch { downloadModel(current.substringBefore('-')) }
        }
    }

    /**
     * Translates [text] into the selected language, returning the original text
     * verbatim when translation is off, unavailable, or the text is already in
     * the target language.
     */
    suspend fun translate(text: String): String {
        val target = _targetLanguage.value
        if (target.isBlank() || target == "en") return text
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return text

        val key = "$target|$trimmed"
        synchronized(cache) {
            cache[key]?.let { return it }
        }

        val translated = runCatching {
            performanceManager.traceSuspend(PerformanceManager.TRACE_TRANSLATE_CALL) {
                deviceTranslator.translate(trimmed)
            }
        }.getOrNull()
        if (translated.isNullOrBlank()) return text

        synchronized(cache) {
            cache[key] = translated
        }
        return translated
    }

    private suspend fun downloadModel(code: String) {
        val currentState = _downloadState.value
        if (currentState is TranslationDownloadState.Downloading &&
            currentState.targetCode == code
        ) return

        _downloadState.value = TranslationDownloadState.Downloading(code)
        try {
            if (deviceTranslator.isModelDownloaded(code)) {
                _downloadState.value = TranslationDownloadState.Ready(code)
                return
            }
            performanceManager.traceSuspend(PerformanceManager.TRACE_TRANSLATION_DOWNLOAD) {
                deviceTranslator.downloadModel(code, requiresWifi = false)
            }
            _downloadState.value = TranslationDownloadState.Ready(code)
            analyticsManager.logEvent(
                AnalyticsManager.EVENT_TRANSLATION_MODEL_READY,
                mapOf(AnalyticsManager.PARAM_LANGUAGE to code)
            )
        } catch (t: Throwable) {
            Log.w(TAG, "Translation model download failed for $code", t)
            _downloadState.value = TranslationDownloadState.Error(code, t.message)
            analyticsManager.recordNonFatal(t)
        }
    }

    companion object {
        private const val TAG = "TranslationManager"
        private const val DEFAULT_TARGET = ""
    }
}