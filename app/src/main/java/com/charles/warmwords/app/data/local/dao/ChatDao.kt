package com.charles.warmwords.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.charles.warmwords.app.data.local.entity.ChatMessage
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(message: ChatMessage): Long

    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    fun getAll(): Flow<List<ChatMessage>>

    @Query("DELETE FROM chat_messages")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM chat_messages")
    suspend fun getCount(): Int

    @Query("DELETE FROM chat_messages WHERE senderId = 'model' AND (isStreaming = 1 OR content = '')")
    suspend fun deleteOrphanedStreamingMessages()
}
