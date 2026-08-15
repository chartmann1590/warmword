package com.charles.warmwords.app.domain.repository

import com.charles.warmwords.app.data.model.MoodLogModel
import kotlinx.coroutines.flow.Flow

interface MoodRepository {
    val allEntries: Flow<List<MoodLogModel>>
    suspend fun addEntry(entry: MoodLogModel)
    suspend fun deleteAll()
    suspend fun getEntriesSince(startOfWeek: Long): List<MoodLogModel>
    suspend fun getAverageMood(): Float?
    suspend fun getLatestEntry(): MoodLogModel?
}
