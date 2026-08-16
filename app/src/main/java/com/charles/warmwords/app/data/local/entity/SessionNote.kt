package com.charles.warmwords.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * An AI-generated note summarizing one derived chat session (see ChatSessionSummary), keyed by
 * that session's start timestamp since sessions aren't a persisted concept of their own - they're
 * derived on the fly by grouping chat_messages with >30 min gaps between them.
 */
@Entity(tableName = "session_notes")
data class SessionNote(
    @PrimaryKey val sessionStartTimestamp: Long,
    val note: String,
    val generatedAt: Long = System.currentTimeMillis()
)
