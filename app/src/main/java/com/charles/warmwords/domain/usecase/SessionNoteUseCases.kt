package com.charles.warmwords.domain.usecase

import com.charles.warmwords.data.model.SessionNoteModel
import com.charles.warmwords.domain.repository.SessionNoteRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SessionNoteUseCases @Inject constructor(
    private val repository: SessionNoteRepository
) {
    val allNotes: Flow<List<SessionNoteModel>> = repository.allNotes

    suspend fun saveNote(note: SessionNoteModel) = repository.saveNote(note)

    suspend fun deleteAll() = repository.deleteAll()
}
