package com.charles.warmwords.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "crisis_resources")
data class CrisisResource(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val phoneNumber: String,
    val website: String = "",
    val description: String = "",
    val countryCode: String = "US"
)
