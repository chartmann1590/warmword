package com.charles.warmwords.translation

import android.util.Log
import com.charles.warmwords.util.await
import com.google.mlkit.nl.languageid.LanguageIdentification
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.common.model.RemoteModelManager
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.TranslateRemoteModel
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Production [DeviceTranslator] backed by ML Kit's on-device translate + language
 * identification. Every download/scanning result stays on the device.
 */
@Singleton
class MlKitDeviceTranslator @Inject constructor() : DeviceTranslator {

    private val languageIdentifier = LanguageIdentification.getClient()
    private val remoteModelManager get() = RemoteModelManager.getInstance()

    @Volatile
    private var activeTarget: String = ""

    @Volatile
    private var activeTranslator: Translator? = null

    override suspend fun isModelDownloaded(code: String): Boolean =
        runCatching {
            val model = remoteModel(code)
            remoteModelManager.isModelDownloaded(model).await()
        }.getOrDefault(false)

    override suspend fun downloadModel(
        code: String,
        requiresWifi: Boolean
    ) {
        val model = remoteModel(code)
        if (remoteModelManager.isModelDownloaded(model).await()) {
            return
        }
        val conditions = DownloadConditions.Builder()
            .apply { if (requiresWifi) requireWifi() }
            .build()
        remoteModelManager.download(model, conditions).await()
    }

    override suspend fun deleteModel(code: String) {
        runCatching {
            val model = remoteModel(code)
            if (remoteModelManager.isModelDownloaded(model).await()) {
                remoteModelManager.deleteDownloadedModel(model).await()
            }
        }.onFailure { Log.w(TAG, "deleteModel($code) failed", it) }
    }

    override suspend fun setTarget(code: String) {
        val normalized = code.lowercase()
        if (normalized == activeTarget) return
        val previous = activeTranslator
        activeTarget = normalized
        activeTranslator = null
        if (normalized.isBlank() || normalized == "en") {
            previous?.close()
            return
        }
        val translator = runCatching {
            val targetTag = TranslateLanguage.fromLanguageTag(normalized)
                ?: error("Unsupported language tag: $normalized")
            Translation.getClient(
                TranslatorOptions.Builder()
                    .setSourceLanguage(TranslateLanguage.ENGLISH)
                    .setTargetLanguage(targetTag)
                    .build()
            )
        }.onFailure { Log.w(TAG, "Failed to create translator for $normalized", it) }
            .getOrNull()
        previous?.close()
        activeTranslator = translator
    }

    override suspend fun translate(text: String): String? {
        if (text.isBlank()) return null
        val target = activeTarget
        val translator = activeTranslator
        if (target.isBlank() || target == "en" || translator == null) return null
        if (looksNonTranslatable(text)) return null

        // Decide the source language on-device so we never translate text that is
        // already in the target language (and skip sources we can't handle).
        val detected = runCatching { languageIdentifier.identifyLanguage(text).await() }
            .getOrDefault("und")
            .lowercase()
            .substringBefore('-')

        when (detected) {
            "und", "" -> Unit // low-confidence -> assume English, attempt translation
            target -> return null // already in the user's language
            "en" -> Unit // default English source, translate
            else -> return null // unsupported source language; keep original
        }

        return runCatching { translator.translate(text).await() }
            .onFailure { Log.d(TAG, "translate failed for target=$target", it) }
            .getOrNull()
    }

    private fun remoteModel(code: String): TranslateRemoteModel {
        val language = TranslateLanguage.fromLanguageTag(code.lowercase())
            ?: error("Unsupported language tag: $code")
        return TranslateRemoteModel.Builder(language).build()
    }

    private fun looksNonTranslatable(text: String): Boolean {
        if (text.length < 2) return true
        // URLs, phone numbers, dial codes - never touch them.
        if (text.startsWith("http", ignoreCase = true) ||
            text.startsWith("tel:", ignoreCase = true) ||
            text.startsWith("www.", ignoreCase = true)
        ) return true
        // Pure numbers/symbols/emoji.
        if (!text.any { it.isLetter() }) return true
        return false
    }

    companion object {
        private const val TAG = "MlKitDeviceTranslator"
    }
}