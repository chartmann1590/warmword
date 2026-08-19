package com.charles.warmwords.domain.repository

import com.charles.warmwords.data.model.SessionNoteModel
import kotlinx.coroutines.flow.Flow

interface SessionNoteRepository {
    val allNotes: Flow<List<SessionNoteModel>>
    suspend fun saveNote(note: SessionNoteModel)
    suspend fun deleteAll()
}
