package com.charles.warmwords.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: String = "default",
    val name: String = "",
    val pronouns: String = "",
    val onboardingComplete: Boolean = false,
    val modelDownloaded: Boolean = false,
    val modelDownloadComplete: Boolean = false,
    val lowRamWarningShown: Boolean = false,
    val voiceRepliesEnabled: Boolean = false,
    val selectedPersonaId: String = "warm_companion",
    val selectedVoiceName: String? = null
)
