package com.charles.warmwords.app.ai

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SystemPromptTest {

    @Test
    fun defaultPersonaIsFree() {
        assertFalse(SystemPrompt.DEFAULT_PERSONA.isPremium)
        assertEquals("warm_companion", SystemPrompt.DEFAULT_PERSONA.id)
    }

    @Test
    fun exactlyFourPersonasArePremium() {
        val premium = SystemPrompt.PERSONAS.filter { it.isPremium }
        assertEquals(4, premium.size)
        assertTrue(premium.map { it.id }.containsAll(
            listOf("cbt_coach", "mindful_guide", "motivator", "quiet_listener")
        ))
    }

    @Test
    fun byIdReturnsDefaultForUnknown() {
        assertEquals(SystemPrompt.DEFAULT_PERSONA, SystemPrompt.byId("does_not_exist"))
    }
}
