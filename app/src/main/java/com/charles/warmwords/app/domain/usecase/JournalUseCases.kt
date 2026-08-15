package com.charles.warmwords.app.domain.usecase

import com.charles.warmwords.app.data.model.JournalEntryModel
import com.charles.warmwords.app.domain.repository.JournalRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class JournalUseCases @Inject constructor(
    private val repository: JournalRepository
) {
    val allEntries: Flow<List<JournalEntryModel>> = repository.allEntries

    suspend fun getEntry(id: Long): JournalEntryModel? = repository.getEntry(id)

    suspend fun addEntry(entry: JournalEntryModel) = repository.addEntry(entry)

    suspend fun updateEntry(entry: JournalEntryModel) = repository.updateEntry(entry)

    suspend fun deleteEntry(entry: JournalEntryModel) = repository.deleteEntry(entry)

    suspend fun deleteAll() = repository.deleteAll()

    suspend fun getEntriesSince(startOfWeek: Long): List<JournalEntryModel> =
        repository.getEntriesSince(startOfWeek)

    suspend fun getCount(): Int = repository.getCount()

    suspend fun getAllEntriesOnce(): List<JournalEntryModel> = repository.getAllEntriesOnce()
}
