package com.charles.warmwords.app.domain.repository

import com.charles.warmwords.app.data.model.SessionReminderModel
import kotlinx.coroutines.flow.Flow

interface SessionReminderRepository {
    val allReminders: Flow<List<SessionReminderModel>>
    suspend fun addReminder(reminder: SessionReminderModel): Long
    suspend fun deleteReminder(reminder: SessionReminderModel)
    suspend fun getUpcoming(): List<SessionReminderModel>
    suspend fun deletePast()
}
