package com.charles.warmwords.app.data.model

import com.charles.warmwords.app.data.local.entity.JournalEntry
import com.charles.warmwords.app.data.local.entity.MoodLog

fun JournalEntry.toModel() = JournalEntryModel(
    id = id,
    timestamp = timestamp,
    moodScore = moodScore,
    content = content,
    tags = tags
)

fun MoodLog.toModel() = MoodLogModel(
    id = id,
    timestamp = timestamp,
    score = score,
    note = note
)
