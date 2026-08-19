package com.charles.warmwords.data.model

data class JournalEntryModel(
    val id: Long = 0,
    val timestamp: Long,
    val moodScore: Int,
    val content: String,
    val tags: List<String> = emptyList()
)
