package com.charles.warmwords.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val senderId: String,
    val content: String,
    val isStreaming: Boolean = false
)
