package com.charles.warmwords.domain.usecase

import com.charles.warmwords.data.local.entity.UserProfile
import com.charles.warmwords.domain.repository.UserProfileRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class UserProfileUseCases @Inject constructor(
    private val repository: UserProfileRepository
) {
    val profile: Flow<UserProfile?> = repository.profile

    suspend fun getProfile(): UserProfile? = repository.getProfile()

    suspend fun saveProfile(profile: UserProfile) = repository.saveProfile(profile)

    suspend fun updateProfile(profile: UserProfile) = repository.updateProfile(profile)

    suspend fun deleteAll() = repository.deleteAll()
}
