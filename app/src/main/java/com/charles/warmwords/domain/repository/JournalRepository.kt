package com.charles.warmwords.domain.repository

import com.charles.warmwords.data.model.JournalEntryModel
import kotlinx.coroutines.flow.Flow

interface JournalRepository {
    val allEntries: Flow<List<JournalEntryModel>>
    suspend fun getEntry(id: Long): JournalEntryModel?
    suspend fun addEntry(entry: JournalEntryModel)
    suspend fun updateEntry(entry: JournalEntryModel)
    suspend fun deleteEntry(entry: JournalEntryModel)
    suspend fun deleteAll()
    suspend fun getEntriesSince(startOfWeek: Long): List<JournalEntryModel>
    suspend fun getCount(): Int
    suspend fun getAllEntriesOnce(): List<JournalEntryModel>
}
