package com.charles.warmwords.app.domain.repository

import com.charles.warmwords.app.data.model.ChatMessageModel
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    val allMessages: Flow<List<ChatMessageModel>>
    suspend fun saveMessage(message: ChatMessageModel): Long
    suspend fun deleteAll()
    suspend fun getCount(): Int
    suspend fun deleteOrphanedStreamingMessages()
}
