package com.charles.warmwords.data.model

sealed class ChatMessageModel {
    data class User(
        val id: Long = 0,
        val timestamp: Long,
        val content: String
    ) : ChatMessageModel()

    data class Model(
        val id: Long = 0,
        val timestamp: Long,
        var content: String,
        val isStreaming: Boolean = false
    ) : ChatMessageModel()
}
