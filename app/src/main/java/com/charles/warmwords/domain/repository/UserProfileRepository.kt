package com.charles.warmwords.domain.repository

import com.charles.warmwords.data.local.entity.UserProfile
import kotlinx.coroutines.flow.Flow

interface UserProfileRepository {
    val profile: Flow<UserProfile?>
    suspend fun getProfile(): UserProfile?
    suspend fun saveProfile(profile: UserProfile)
    suspend fun updateProfile(profile: UserProfile)
    suspend fun deleteAll()
}
