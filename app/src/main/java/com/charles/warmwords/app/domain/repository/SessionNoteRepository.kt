package com.charles.warmwords.app.domain.repository

import com.charles.warmwords.app.data.model.SessionNoteModel
import kotlinx.coroutines.flow.Flow

interface SessionNoteRepository {
    val allNotes: Flow<List<SessionNoteModel>>
    suspend fun saveNote(note: SessionNoteModel)
    suspend fun deleteAll()
}
