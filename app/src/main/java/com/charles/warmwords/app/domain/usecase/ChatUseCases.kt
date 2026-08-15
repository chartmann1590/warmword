package com.charles.warmwords.app.domain.usecase

import com.charles.warmwords.app.data.model.ChatMessageModel
import com.charles.warmwords.app.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ChatUseCases @Inject constructor(
    private val repository: ChatRepository
) {
    val allMessages: Flow<List<ChatMessageModel>> = repository.allMessages

    suspend fun saveMessage(message: ChatMessageModel): Long = repository.saveMessage(message)

    suspend fun deleteAll() = repository.deleteAll()

    suspend fun getCount(): Int = repository.getCount()

    suspend fun deleteOrphanedStreamingMessages() = repository.deleteOrphanedStreamingMessages()
}
