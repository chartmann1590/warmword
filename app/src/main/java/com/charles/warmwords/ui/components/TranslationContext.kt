package com.charles.warmwords.ui.components

import androidx.compose.runtime.staticCompositionLocalOf
import com.charles.warmwords.translation.TranslationDownloadState
import com.charles.warmwords.translation.TranslationManager

/**
 * Carries the live translation state to every composable in the tree so
 * [TranslatedText] can react to language/model changes without extra plumbing.
 */
class TranslationContext(
    val manager: TranslationManager,
    val targetCode: String,
    val downloadState: TranslationDownloadState
) {
    /** Translation is switched on (a real, non-English language is selected). */
    val active: Boolean get() = manager.isActive

    /** The en->target model is downloaded and ready. */
    val modelReady: Boolean get() = downloadState is TranslationDownloadState.Ready
}

val LocalAppTranslation = staticCompositionLocalOf<TranslationContext?> { null }