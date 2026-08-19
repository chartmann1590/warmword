package com.charles.warmwords.data.model

import com.charles.warmwords.data.local.entity.JournalEntry
import com.charles.warmwords.data.local.entity.MoodLog

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
