package com.charles.warmwords.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.charles.warmwords.data.local.entity.SessionNote
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionNoteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(note: SessionNote)

    @Query("SELECT * FROM session_notes")
    fun getAll(): Flow<List<SessionNote>>

    @Query("DELETE FROM session_notes")
    suspend fun deleteAll()
}
