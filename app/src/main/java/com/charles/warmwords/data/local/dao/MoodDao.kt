package com.charles.warmwords.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.charles.warmwords.data.local.entity.MoodLog
import kotlinx.coroutines.flow.Flow

@Dao
interface MoodDao {
    @Insert
    suspend fun insert(moodLog: MoodLog): Long

    @Query("SELECT * FROM mood_logs ORDER BY timestamp DESC")
    fun getAll(): Flow<List<MoodLog>>

    @Query("SELECT * FROM mood_logs WHERE timestamp >= :startOfWeek ORDER BY timestamp ASC")
    suspend fun getEntriesSince(startOfWeek: Long): List<MoodLog>

    @Query("DELETE FROM mood_logs")
    suspend fun deleteAll()

    @Query("SELECT AVG(score) FROM mood_logs")
    suspend fun getAverageMood(): Float?

    @Query("SELECT * FROM mood_logs ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestEntry(): MoodLog?
}
