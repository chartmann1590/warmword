package com.charles.warmwords.translation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SupportedLanguagesTest {

    @Test
    fun everyLanguageHasUniqueNonBlankCode() {
        val codes = SupportedLanguages.ALL.map { it.code }
        assertEquals("language codes must be unique", codes.size, codes.toSet().size)
        codes.forEach { assertTrue("code must not be blank: $it", it.isNotBlank()) }
    }

    @Test
    fun englishIsFirstAndPresent() {
        assertEquals("en", SupportedLanguages.ENGLISH.code)
        assertEquals("English", SupportedLanguages.ENGLISH.englishName)
        assertTrue(SupportedLanguages.ALL.contains(SupportedLanguages.ENGLISH))
    }

    @Test
    fun translatableExcludesEnglish() {
        assertFalse(SupportedLanguages.TRANSLATABLE.any { it.code == "en" })
        assertEquals(
            "TRANSLATABLE should be ALL minus English",
            SupportedLanguages.ALL.size - 1,
            SupportedLanguages.TRANSLATABLE.size
        )
    }

    @Test
    fun byCodeMatchesCaseInsensitively() {
        assertNotNull(SupportedLanguages.byCode("FR"))
        assertEquals("fr", SupportedLanguages.byCode("FR")?.code)
        assertEquals("es", SupportedLanguages.byCode("es")?.code)
        assertEquals(null, SupportedLanguages.byCode("xx"))
    }

    @Test
    fun displayNameCombinesNativeAndEnglish() {
        val spanish = SupportedLanguages.byCode("es")!!
        assertTrue(spanish.displayName().contains("Español"))
        assertTrue(spanish.displayName().contains("Spanish"))
    }
}