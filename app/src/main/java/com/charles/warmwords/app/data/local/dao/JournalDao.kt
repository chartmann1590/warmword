package com.charles.warmwords.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.charles.warmwords.app.data.local.entity.JournalEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface JournalDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: JournalEntry): Long

    @Update
    suspend fun update(entry: JournalEntry)

    @Delete
    suspend fun delete(entry: JournalEntry)

    @Query("SELECT * FROM journal_entries ORDER BY timestamp DESC")
    fun getAll(): Flow<List<JournalEntry>>

    @Query("SELECT * FROM journal_entries WHERE id = :id")
    suspend fun getById(id: Long): JournalEntry?

    @Query("DELETE FROM journal_entries")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM journal_entries")
    suspend fun getCount(): Int

    @Query("SELECT * FROM journal_entries WHERE timestamp >= :startOfWeek ORDER BY timestamp DESC")
    suspend fun getEntriesSince(startOfWeek: Long): List<JournalEntry>
}
