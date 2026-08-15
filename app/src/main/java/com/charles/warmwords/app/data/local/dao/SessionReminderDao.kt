package com.charles.warmwords.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.charles.warmwords.app.data.local.entity.SessionReminder
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionReminderDao {
    @Insert
    suspend fun insert(reminder: SessionReminder): Long

    @Delete
    suspend fun delete(reminder: SessionReminder)

    @Query("SELECT * FROM session_reminders ORDER BY timestamp ASC")
    fun getAll(): Flow<List<SessionReminder>>

    @Query("SELECT * FROM session_reminders WHERE timestamp > :now ORDER BY timestamp ASC")
    suspend fun getUpcoming(now: Long): List<SessionReminder>

    @Query("DELETE FROM session_reminders WHERE timestamp <= :now")
    suspend fun deletePast(now: Long)
}
