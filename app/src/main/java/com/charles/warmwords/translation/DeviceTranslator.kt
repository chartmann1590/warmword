package com.charles.warmwords.translation

/**
 * Android-free abstraction over on-device translation so business logic can be
 * unit-tested on the JVM. The production implementation wraps ML Kit.
 */
interface DeviceTranslator {

    /** True if the model translating English -> [code] is already downloaded. */
    suspend fun isModelDownloaded(code: String): Boolean

    /**
     * Downloads the English -> [code] model. Throws on failure (e.g. no Google
     * Play services, no network).
     */
    suspend fun downloadModel(
        code: String,
        requiresWifi: Boolean
    )

    /** Deletes the downloaded English -> [code] model, freeing storage. No-op if absent. */
    suspend fun deleteModel(code: String)

    /** Prepares the translator to translate English -> [code]. */
    suspend fun setTarget(code: String)

    /**
     * Translates English text [text] into the active target language.
     * Returns null when the text can't/shouldn't be translated (already in the
     * target language, unsupported source, model missing, empty result).
     */
    suspend fun translate(text: String): String?
}