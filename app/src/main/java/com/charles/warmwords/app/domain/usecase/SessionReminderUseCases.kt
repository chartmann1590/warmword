package com.charles.warmwords.app.domain.usecase

import com.charles.warmwords.app.data.model.SessionReminderModel
import com.charles.warmwords.app.domain.repository.SessionReminderRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SessionReminderUseCases @Inject constructor(
    private val repository: SessionReminderRepository
) {
    val allReminders: Flow<List<SessionReminderModel>> = repository.allReminders

    suspend fun addReminder(reminder: SessionReminderModel): Long = repository.addReminder(reminder)

    suspend fun deleteReminder(reminder: SessionReminderModel) = repository.deleteReminder(reminder)

    suspend fun getUpcoming(): List<SessionReminderModel> = repository.getUpcoming()

    suspend fun deletePast() = repository.deletePast()
}
