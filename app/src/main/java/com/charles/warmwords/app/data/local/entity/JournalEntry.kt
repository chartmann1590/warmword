package com.charles.warmwords.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "journal_entries")
data class JournalEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val moodScore: Int,
    val content: String,
    val tags: List<String> = emptyList()
)
