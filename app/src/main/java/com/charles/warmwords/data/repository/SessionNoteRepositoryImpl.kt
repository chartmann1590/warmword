package com.charles.warmwords.data.repository

import com.charles.warmwords.data.local.dao.SessionNoteDao
import com.charles.warmwords.data.local.entity.SessionNote
import com.charles.warmwords.data.model.SessionNoteModel
import com.charles.warmwords.domain.repository.SessionNoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SessionNoteRepositoryImpl @Inject constructor(
    private val dao: SessionNoteDao
) : SessionNoteRepository {

    override val allNotes: Flow<List<SessionNoteModel>> =
        dao.getAll().map { list -> list.map { SessionNoteModel(it.sessionStartTimestamp, it.note) } }

    override suspend fun saveNote(note: SessionNoteModel) {
        dao.insert(SessionNote(sessionStartTimestamp = note.sessionStartTimestamp, note = note.note))
    }

    override suspend fun deleteAll() = dao.deleteAll()
}
