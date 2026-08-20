package com.charles.warmwords.insights

import com.charles.warmwords.data.model.ChatMessageModel
import com.charles.warmwords.ui.screens.insights.buildChatSessions
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ChatSessionSummaryTest {

    private fun user(ts: Long, content: String) =
        ChatMessageModel.User(id = 0, timestamp = ts, content = content)

    private fun model(ts: Long, content: String) =
        ChatMessageModel.Model(id = 0, timestamp = ts, content = content)

    @Test
    fun emptyMessagesProduceNoSessions() {
        assertTrue(buildChatSessions(emptyList()).isEmpty())
    }

    @Test
    fun messagesWithinGapAreOneSession() {
        val base = 1_700_000_000_000L
        val sessions = buildChatSessions(
            listOf(
                user(base, "Hello"),
                model(base + 1_000, "Hi there!"),
                user(base + 60_000, "How are you?")
            )
        )
        assertEquals(1, sessions.size)
        assertEquals(3, sessions.first().messageCount)
        assertEquals("Hello", sessions.first().preview)
    }

    @Test
    fun messagesBeyondGapStartNewSession() {
        val base = 1_700_000_000_000L
        val gap = 31 * 60 * 1000L // just over 30 minutes
        val sessions = buildChatSessions(
            listOf(
                user(base, "Morning"),
                user(base + gap, "Afternoon")
            )
        )
        assertEquals(2, sessions.size)
        assertEquals(1, sessions.first().messageCount)
    }

    @Test
    fun transcriptIncludesSpeakerLabels() {
        val base = 1_700_000_000_000L
        val sessions = buildChatSessions(
            listOf(user(base, "Hi"), model(base + 1_000, "Hello"))
        )
        val transcript = sessions.first().transcript
        assertTrue(transcript.contains("User: Hi"))
        assertTrue(transcript.contains("WarmWord: Hello"))
    }

    @Test
    fun previewFallsBackWhenNoUserMessage() {
        val base = 1_700_000_000_000L
        val sessions = buildChatSessions(listOf(model(base, "Hello")))
        assertFalse(sessions.first().preview.isBlank())
    }
}