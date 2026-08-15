package com.charles.warmwords.app.data.repository

import com.charles.warmwords.app.data.local.dao.ChatDao
import com.charles.warmwords.app.data.local.entity.ChatMessage
import com.charles.warmwords.app.data.model.ChatMessageModel
import com.charles.warmwords.app.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ChatRepositoryImpl @Inject constructor(
    private val chatDao: ChatDao
) : ChatRepository {

    override val allMessages: Flow<List<ChatMessageModel>> =
        chatDao.getAll().map { list ->
            list.map { entity ->
                if (entity.senderId == "user") {
                    ChatMessageModel.User(
                        id = entity.id,
                        timestamp = entity.timestamp,
                        content = entity.content
                    )
                } else {
                    ChatMessageModel.Model(
                        id = entity.id,
                        timestamp = entity.timestamp,
                        content = entity.content,
                        isStreaming = entity.isStreaming
                    )
                }
            }
        }

    override suspend fun saveMessage(message: ChatMessageModel): Long {
        return when (message) {
            is ChatMessageModel.User -> {
                chatDao.insert(
                    ChatMessage(
                        id = 0,
                        timestamp = message.timestamp,
                        senderId = "user",
                        content = message.content
                    )
                )
            }
            is ChatMessageModel.Model -> {
                chatDao.insert(
                    ChatMessage(
                        id = message.id,
                        timestamp = message.timestamp,
                        senderId = "model",
                        content = message.content,
                        isStreaming = message.isStreaming
                    )
                )
            }
        }
    }

    override suspend fun deleteAll() = chatDao.deleteAll()

    override suspend fun getCount(): Int = chatDao.getCount()

    override suspend fun deleteOrphanedStreamingMessages() = chatDao.deleteOrphanedStreamingMessages()
}
