package com.charles.warmwords.translation

/**
 * A language the user can choose for on-device translation.
 *
 * Codes are BCP-47 tags that are supported by ML Kit's on-device translator
 * (the full supported set lives in `TranslateLanguage`; we expose a curated
 * subset covering the languages most relevant to WarmWord users). Every code
 * here is a valid source AND target tag for ML Kit.
 */
data class DisplayLanguage(
    val code: String,
    val englishName: String,
    val nativeName: String
) {
    fun displayName() = "$nativeName ($englishName)"
}

object SupportedLanguages {

    val ALL: List<DisplayLanguage> = listOf(
        DisplayLanguage("en", "English", "English"),
        DisplayLanguage("ar", "Arabic", "العربية"),
        DisplayLanguage("zh", "Chinese (Simplified)", "中文"),
        DisplayLanguage("cs", "Czech", "Čeština"),
        DisplayLanguage("da", "Danish", "Dansk"),
        DisplayLanguage("nl", "Dutch", "Nederlands"),
        DisplayLanguage("fi", "Finnish", "Suomi"),
        DisplayLanguage("fr", "French", "Français"),
        DisplayLanguage("de", "German", "Deutsch"),
        DisplayLanguage("el", "Greek", "Ελληνικά"),
        DisplayLanguage("he", "Hebrew", "עברית"),
        DisplayLanguage("hi", "Hindi", "हिन्दी"),
        DisplayLanguage("hu", "Hungarian", "Magyar"),
        DisplayLanguage("id", "Indonesian", "Bahasa Indonesia"),
        DisplayLanguage("it", "Italian", "Italiano"),
        DisplayLanguage("ja", "Japanese", "日本語"),
        DisplayLanguage("ko", "Korean", "한국어"),
        DisplayLanguage("ms", "Malay", "Bahasa Melayu"),
        DisplayLanguage("no", "Norwegian", "Norsk"),
        DisplayLanguage("fa", "Persian", "فارسی"),
        DisplayLanguage("pl", "Polish", "Polski"),
        DisplayLanguage("pt", "Portuguese", "Português"),
        DisplayLanguage("ro", "Romanian", "Română"),
        DisplayLanguage("ru", "Russian", "Русский"),
        DisplayLanguage("es", "Spanish", "Español"),
        DisplayLanguage("sw", "Swahili", "Kiswahili"),
        DisplayLanguage("sv", "Swedish", "Svenska"),
        DisplayLanguage("tl", "Tagalog", "Tagalog"),
        DisplayLanguage("th", "Thai", "ไทย"),
        DisplayLanguage("tr", "Turkish", "Türkçe"),
        DisplayLanguage("uk", "Ukrainian", "Українська"),
        DisplayLanguage("ur", "Urdu", "اردو"),
        DisplayLanguage("vi", "Vietnamese", "Tiếng Việt")
    )

    /** English: the app's source language; selecting it means "no translation". */
    val ENGLISH: DisplayLanguage get() = ALL.first { it.code == "en" }

    fun byCode(code: String?): DisplayLanguage? =
        ALL.firstOrNull { it.code.equals(code, ignoreCase = true) }

    /** Supported language list for translation (excludes English, which needs no model). */
    val TRANSLATABLE: List<DisplayLanguage> get() = ALL.filter { it.code != "en" }
}
