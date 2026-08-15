package com.charles.warmwords.app.domain.usecase

import com.charles.warmwords.app.data.model.MoodLogModel
import com.charles.warmwords.app.domain.repository.MoodRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class MoodUseCases @Inject constructor(
    private val repository: MoodRepository
) {
    val allEntries: Flow<List<MoodLogModel>> = repository.allEntries

    suspend fun addEntry(entry: MoodLogModel) = repository.addEntry(entry)

    suspend fun deleteAll() = repository.deleteAll()

    suspend fun getEntriesSince(startOfWeek: Long): List<MoodLogModel> =
        repository.getEntriesSince(startOfWeek)

    suspend fun getAverageMood(): Float? = repository.getAverageMood()

    suspend fun getLatestEntry(): MoodLogModel? = repository.getLatestEntry()
}
