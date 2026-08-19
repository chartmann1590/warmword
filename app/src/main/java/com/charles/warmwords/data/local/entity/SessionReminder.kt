package com.charles.warmwords.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "session_reminders")
data class SessionReminder(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val label: String,
    val createdAt: Long = System.currentTimeMillis()
)
