package com.charles.warmwords.data.repository

import com.charles.warmwords.data.local.dao.SessionReminderDao
import com.charles.warmwords.data.local.entity.SessionReminder
import com.charles.warmwords.data.model.SessionReminderModel
import com.charles.warmwords.domain.repository.SessionReminderRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SessionReminderRepositoryImpl @Inject constructor(
    private val dao: SessionReminderDao
) : SessionReminderRepository {

    override val allReminders: Flow<List<SessionReminderModel>> =
        dao.getAll().map { list -> list.map { it.toModel() } }

    override suspend fun addReminder(reminder: SessionReminderModel): Long {
        return dao.insert(
            SessionReminder(
                timestamp = reminder.timestamp,
                label = reminder.label
            )
        )
    }

    override suspend fun deleteReminder(reminder: SessionReminderModel) {
        dao.delete(SessionReminder(id = reminder.id, timestamp = reminder.timestamp, label = reminder.label))
    }

    override suspend fun getUpcoming(): List<SessionReminderModel> =
        dao.getUpcoming(System.currentTimeMillis()).map { it.toModel() }

    override suspend fun deletePast() = dao.deletePast(System.currentTimeMillis())

    private fun SessionReminder.toModel() = SessionReminderModel(id = id, timestamp = timestamp, label = label)
}
