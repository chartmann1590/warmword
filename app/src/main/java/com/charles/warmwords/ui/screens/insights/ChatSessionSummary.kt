package com.charles.warmwords.ui.screens.insights

import com.charles.warmwords.data.model.ChatMessageModel

data class ChatSessionSummary(
    val startTimestamp: Long,
    val endTimestamp: Long,
    val messageCount: Int,
    val preview: String,
    val transcript: String,
    val isLikelyOngoing: Boolean
)

private const val SESSION_GAP_MS = 30 * 60 * 1000L // messages more than 30 min apart start a new session

private val ChatMessageModel.timestamp: Long
    get() = when (this) {
        is ChatMessageModel.User -> timestamp
        is ChatMessageModel.Model -> timestamp
    }

/**
 * WarmWord doesn't track an explicit "session" concept in the database - it groups
 * consecutive chat messages into sessions whenever there's a >30 minute gap between them,
 * which is a reasonable proxy for "the user put the phone down and came back later."
 */
fun buildChatSessions(messages: List<ChatMessageModel>): List<ChatSessionSummary> {
    if (messages.isEmpty()) return emptyList()

    val sorted = messages.sortedBy { it.timestamp }
    val sessions = mutableListOf<MutableList<ChatMessageModel>>(mutableListOf(sorted.first()))

    for (i in 1 until sorted.size) {
        val prev = sorted[i - 1]
        val current = sorted[i]
        if (current.timestamp - prev.timestamp > SESSION_GAP_MS) {
            sessions.add(mutableListOf(current))
        } else {
            sessions.last().add(current)
        }
    }

    val now = System.currentTimeMillis()

    return sessions.map { group ->
        val firstUserMessage = group.filterIsInstance<ChatMessageModel.User>().firstOrNull()
        val preview = firstUserMessage?.content?.take(80)?.trim().orEmpty()
        val transcript = group.joinToString("\n") { message ->
            when (message) {
                is ChatMessageModel.User -> "User: ${message.content}"
                is ChatMessageModel.Model -> "WarmWord: ${message.content}"
            }
        }
        val endTimestamp = group.last().timestamp
        ChatSessionSummary(
            startTimestamp = group.first().timestamp,
            endTimestamp = endTimestamp,
            messageCount = group.size,
            preview = preview.ifBlank { "Session with no messages saved" },
            transcript = transcript,
            // A session within the gap window of "now" might still get more messages appended
            // to it, so its note (and its DB rows) aren't final yet - don't summarize it until
            // it's had a chance to actually end.
            isLikelyOngoing = now - endTimestamp <= SESSION_GAP_MS
        )
    }.sortedByDescending { it.startTimestamp }
}
