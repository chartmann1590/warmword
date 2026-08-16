package com.charles.warmwords.app.domain.usecase

import com.charles.warmwords.app.data.model.SessionNoteModel
import com.charles.warmwords.app.domain.repository.SessionNoteRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SessionNoteUseCases @Inject constructor(
    private val repository: SessionNoteRepository
) {
    val allNotes: Flow<List<SessionNoteModel>> = repository.allNotes

    suspend fun saveNote(note: SessionNoteModel) = repository.saveNote(note)

    suspend fun deleteAll() = repository.deleteAll()
}
