package com.charles.warmwords.app.data.repository

import com.charles.warmwords.app.data.local.dao.JournalDao
import com.charles.warmwords.app.data.local.entity.JournalEntry
import com.charles.warmwords.app.data.model.JournalEntryModel
import com.charles.warmwords.app.data.model.toModel
import com.charles.warmwords.app.domain.repository.JournalRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class JournalRepositoryImpl @Inject constructor(
    private val journalDao: JournalDao
) : JournalRepository {

    override val allEntries: Flow<List<JournalEntryModel>> =
        journalDao.getAll().map { list -> list.map { it.toModel() } }

    override suspend fun getEntry(id: Long): JournalEntryModel? =
        journalDao.getById(id)?.toModel()

    override suspend fun addEntry(entry: JournalEntryModel) {
        journalDao.insert(
            JournalEntry(
                id = entry.id,
                timestamp = entry.timestamp,
                moodScore = entry.moodScore,
                content = entry.content,
                tags = entry.tags
            )
        )
    }

    override suspend fun updateEntry(entry: JournalEntryModel) {
        journalDao.update(
            JournalEntry(
                id = entry.id,
                timestamp = entry.timestamp,
                moodScore = entry.moodScore,
                content = entry.content,
                tags = entry.tags
            )
        )
    }

    override suspend fun deleteEntry(entry: JournalEntryModel) {
        journalDao.delete(
            JournalEntry(
                id = entry.id,
                timestamp = entry.timestamp,
                moodScore = entry.moodScore,
                content = entry.content,
                tags = entry.tags
            )
        )
    }

    override suspend fun deleteAll() = journalDao.deleteAll()

    override suspend fun getEntriesSince(startOfWeek: Long): List<JournalEntryModel> =
        journalDao.getEntriesSince(startOfWeek).map { it.toModel() }

    override suspend fun getCount(): Int = journalDao.getCount()

    override suspend fun getAllEntriesOnce(): List<JournalEntryModel> =
        journalDao.getAll().first().map { it.toModel() }
}
