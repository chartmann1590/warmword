package com.charles.warmwords.translation

/**
 * State of the ML Kit translation model download for the user's chosen language.
 */
sealed interface TranslationDownloadState {
    /** No translation selected (English) or model cleaned up. */
    data object Idle : TranslationDownloadState

    /** Actively downloading the en->[targetCode] model (indeterminate). */
    data class Downloading(val targetCode: String) : TranslationDownloadState

    /** Model for [targetCode] is downloaded and ready to translate. */
    data class Ready(val targetCode: String) : TranslationDownloadState

    /** Something went wrong downloading [targetCode]'s model. */
    data class Error(val targetCode: String, val message: String? = null) : TranslationDownloadState
}
