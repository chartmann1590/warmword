package com.charles.warmwords.app.ai

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

data class WarmVoice(
    val name: String,
    val displayName: String,
    val qualityLabel: String,
    val requiresNetwork: Boolean,
    val quality: Int
)

private const val SAMPLE_LINE = "Hi, I'm WarmWord. I'm here to listen, whenever you need me."

@Singleton
class TextToSpeechManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var tts: TextToSpeech? = null
    private var ready = false
    private var appliedVoiceName: String? = null

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking

    private val _isReady = MutableStateFlow(false)
    val isReady: StateFlow<Boolean> = _isReady

    init {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                ready = true
                configureDefaults()
                _isReady.value = true
            } else {
                Log.e(TAG, "TextToSpeech init failed with status $status")
            }
        }
    }

    private fun configureDefaults() {
        val engine = tts ?: return
        engine.language = Locale.US
        val bestVoice = bestAvailableVoice(engine)
        bestVoice?.let {
            engine.voice = it
            appliedVoiceName = it.name
        }
        engine.setPitch(1.0f)
        engine.setSpeechRate(0.98f)
        engine.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                _isSpeaking.value = true
            }
            override fun onDone(utteranceId: String?) {
                _isSpeaking.value = false
                if (utteranceId?.startsWith("warmword_preview_") == true) restoreAppliedVoice()
            }
            override fun onError(utteranceId: String?) {
                _isSpeaking.value = false
                if (utteranceId?.startsWith("warmword_preview_") == true) restoreAppliedVoice()
            }
        })
    }

    private fun restoreAppliedVoice() {
        val engine = tts ?: return
        val name = appliedVoiceName ?: return
        engine.voices?.firstOrNull { it.name == name }?.let { engine.voice = it }
    }

    private fun bestAvailableVoice(engine: TextToSpeech): Voice? =
        engine.voices
            ?.filter { it.locale.language == Locale.ENGLISH.language && !it.isNetworkConnectionRequired && it.quality >= Voice.QUALITY_NORMAL }
            ?.maxByOrNull { it.quality }

    /**
     * Natural-sounding English voices, grouped by accent so the list shows real variety
     * (e.g. US, UK, Australian) instead of just whichever single accent happens to have the
     * most installed voices, and numbered within each accent so entries are distinguishable.
     */
    fun availableVoices(): List<WarmVoice> {
        val engine = tts ?: return emptyList()
        val candidates = engine.voices
            ?.filter {
                it.locale.language == Locale.ENGLISH.language &&
                    it.quality >= Voice.QUALITY_NORMAL &&
                    !it.features.orEmpty().contains(TextToSpeech.Engine.KEY_FEATURE_NOT_INSTALLED)
            }
            .orEmpty()

        return candidates
            .groupBy { it.locale.displayCountry.ifBlank { it.locale.displayLanguage } }
            .toSortedMap(compareByDescending<String> { region -> region == "United States" }.thenBy { it }) // US first, then alphabetical
            .flatMap { (region, voicesInRegion) ->
                voicesInRegion
                    .sortedByDescending { it.quality }
                    .distinctBy { it.quality to it.isNetworkConnectionRequired }
                    .take(3)
                    .mapIndexed { index, voice -> voice.toWarmVoice(region, index + 1) }
            }
            .sortedByDescending { it.quality }
            .take(12)
    }

    fun currentVoiceName(): String? = appliedVoiceName

    /** Permanently switches the reply voice (persisted preference should call this on load). */
    fun applyVoice(voiceName: String?) {
        val engine = tts ?: return
        val voice = voiceName?.let { name -> engine.voices?.firstOrNull { it.name == name } }
            ?: bestAvailableVoice(engine)
        voice?.let {
            engine.voice = it
            appliedVoiceName = it.name
        }
    }

    /** Speaks a short sample using the given voice without permanently changing the applied voice. */
    fun previewVoice(voiceName: String) {
        val engine = tts ?: return
        val voice = engine.voices?.firstOrNull { it.name == voiceName } ?: return
        engine.voice = voice
        engine.speak(SAMPLE_LINE, TextToSpeech.QUEUE_FLUSH, null, "warmword_preview_${System.currentTimeMillis()}")
        // The applied (persisted) voice is restored in the utterance progress listener's onDone/onError,
        // once synthesis of this preview has actually finished reading engine.voice.
    }

    fun speak(text: String) {
        if (!ready || text.isBlank()) return
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "warmword_reply_${System.currentTimeMillis()}")
    }

    fun stop() {
        tts?.stop()
        _isSpeaking.value = false
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        ready = false
    }

    private fun Voice.toWarmVoice(region: String, indexInRegion: Int): WarmVoice {
        val qualityLabel = when {
            quality >= Voice.QUALITY_VERY_HIGH -> "Studio quality"
            quality >= Voice.QUALITY_HIGH -> "High quality"
            else -> "Standard quality"
        }
        val genderHint = when {
            name.contains("female", ignoreCase = true) -> " (Female)"
            name.contains("male", ignoreCase = true) -> " (Male)"
            else -> ""
        }
        return WarmVoice(
            name = name,
            displayName = "$region Voice $indexInRegion$genderHint",
            qualityLabel = qualityLabel,
            requiresNetwork = isNetworkConnectionRequired,
            quality = quality
        )
    }

    companion object {
        private const val TAG = "TextToSpeechManager"
    }
}
