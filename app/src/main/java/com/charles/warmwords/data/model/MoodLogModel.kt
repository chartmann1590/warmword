package com.charles.warmwords.data.model

data class MoodLogModel(
    val id: Long = 0,
    val timestamp: Long,
    val score: Int,
    val note: String = ""
)
