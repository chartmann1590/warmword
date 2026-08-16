package com.charles.warmwords.app.data.repository

import com.charles.warmwords.app.data.local.dao.SessionNoteDao
import com.charles.warmwords.app.data.local.entity.SessionNote
import com.charles.warmwords.app.data.model.SessionNoteModel
import com.charles.warmwords.app.domain.repository.SessionNoteRepository
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
