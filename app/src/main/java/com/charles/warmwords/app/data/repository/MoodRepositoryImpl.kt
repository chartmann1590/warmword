package com.charles.warmwords.app.data.repository

import com.charles.warmwords.app.data.local.dao.MoodDao
import com.charles.warmwords.app.data.local.entity.MoodLog
import com.charles.warmwords.app.data.model.MoodLogModel
import com.charles.warmwords.app.data.model.toModel
import com.charles.warmwords.app.domain.repository.MoodRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class MoodRepositoryImpl @Inject constructor(
    private val moodDao: MoodDao
) : MoodRepository {

    override val allEntries: Flow<List<MoodLogModel>> =
        moodDao.getAll().map { list -> list.map { it.toModel() } }

    override suspend fun addEntry(entry: MoodLogModel) {
        moodDao.insert(
            MoodLog(
                id = entry.id,
                timestamp = entry.timestamp,
                score = entry.score,
                note = entry.note
            )
        )
    }

    override suspend fun deleteAll() = moodDao.deleteAll()

    override suspend fun getEntriesSince(startOfWeek: Long): List<MoodLogModel> =
        moodDao.getEntriesSince(startOfWeek).map { it.toModel() }

    override suspend fun getAverageMood(): Float? = moodDao.getAverageMood()

    override suspend fun getLatestEntry(): MoodLogModel? =
        moodDao.getLatestEntry()?.toModel()
}
