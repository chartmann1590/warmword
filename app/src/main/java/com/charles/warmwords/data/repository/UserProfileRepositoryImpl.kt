package com.charles.warmwords.data.repository

import com.charles.warmwords.data.local.dao.UserProfileDao
import com.charles.warmwords.data.local.entity.UserProfile
import com.charles.warmwords.domain.repository.UserProfileRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class UserProfileRepositoryImpl @Inject constructor(
    private val userProfileDao: UserProfileDao
) : UserProfileRepository {

    override val profile: Flow<UserProfile?> = userProfileDao.getDefaultProfile()

    override suspend fun getProfile(): UserProfile? = userProfileDao.getDefaultProfileSync()

    override suspend fun saveProfile(profile: UserProfile) {
        val existing = userProfileDao.getDefaultProfileSync()
        if (existing != null) {
            userProfileDao.update(profile)
        } else {
            userProfileDao.insert(profile)
        }
    }

    override suspend fun updateProfile(profile: UserProfile) {
        userProfileDao.update(profile)
    }

    override suspend fun deleteAll() = userProfileDao.deleteAll()
}
