package com.charles.warmwords.ui.components

import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit

/**
 * Returns [text] translated into the user's selected language via ML Kit on
 * device. Returns the original immediately and swaps in the translation once
 * available, caching repeat strings. When translation is off/English, the
 * device translator can't handle the text, or [enabled] is false, the original
 * is returned.
 */
@Composable
fun rememberTranslatedString(text: String, enabled: Boolean = true): String {
    val context = LocalAppTranslation.current
    val manager = context?.manager
    val targetCode = context?.targetCode
    val modelReady = context?.modelReady == true
    val shouldTranslate = context != null && context.active && enabled && text.isNotBlank()

    // Key on targetCode + modelReady as well as text: when the on-device model
    // finishes downloading (or the user changes language), every on-screen string
    // is re-translated in place without needing to leave/re-enter the screen.
    var result by remember(text, shouldTranslate, targetCode, modelReady) { mutableStateOf(text) }

    LaunchedEffect(text, shouldTranslate, targetCode, modelReady) {
        if (!shouldTranslate || !modelReady) {
            result = text
        } else {
            result = manager?.translate(text) ?: text
        }
    }

    return result
}

/**
 * Drop-in replacement for Material3 [Text] that first translates [text] with the
 * on-device ML Kit translator. Accepts the same parameters as [Text]. Pass
 * [enabled] = false for content that is still changing (e.g. streaming AI
 * output) so the partial text is not translated repeatedly.
 */
@Composable
fun TranslatedText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontStyle: FontStyle? = null,
    fontWeight: FontWeight? = null,
    fontFamily: FontFamily? = null,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    textDecoration: TextDecoration? = null,
    textAlign: TextAlign? = null,
    lineHeight: TextUnit = TextUnit.Unspecified,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    minLines: Int = 1,
    onTextLayout: (TextLayoutResult) -> Unit = {},
    style: TextStyle = LocalTextStyle.current,
    enabled: Boolean = true
) {
    Text(
        text = rememberTranslatedString(text, enabled),
        modifier = modifier,
        color = color,
        fontSize = fontSize,
        fontStyle = fontStyle,
        fontWeight = fontWeight,
        fontFamily = fontFamily,
        letterSpacing = letterSpacing,
        textDecoration = textDecoration,
        textAlign = textAlign,
        lineHeight = lineHeight,
        overflow = overflow,
        softWrap = softWrap,
        maxLines = maxLines,
        minLines = minLines,
        onTextLayout = onTextLayout,
        style = style
    )
}