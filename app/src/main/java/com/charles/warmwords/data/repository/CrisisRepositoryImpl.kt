package com.charles.warmwords.data.repository

import android.content.Context
import com.charles.warmwords.data.local.dao.CrisisResourceDao
import com.charles.warmwords.data.local.entity.CrisisResource
import com.charles.warmwords.domain.repository.CrisisRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CrisisRepositoryImpl @Inject constructor(
    private val crisisResourceDao: CrisisResourceDao,
    @ApplicationContext private val context: Context
) : CrisisRepository {

    override fun getLocalCrisisResources(): Flow<List<CrisisResource>> =
        crisisResourceDao.getByCountry("US")

    override suspend fun insert(resource: CrisisResource) {
        crisisResourceDao.insert(resource)
    }

    override suspend fun insertAll(resources: List<CrisisResource>) {
        if (crisisResourceDao.getCount() == 0) {
            crisisResourceDao.insertAll(resources)
        }
    }

    override suspend fun deleteAll() = crisisResourceDao.deleteAll()

    companion object {
        fun getDefaultResources(): List<CrisisResource> = listOf(
            CrisisResource(
                name = "988 Suicide & Crisis Lifeline",
                phoneNumber = "988",
                website = "https://988lifeline.org",
                description = "Free, 24/7 support for people in distress",
                countryCode = "US"
            ),
            CrisisResource(
                name = "Crisis Text Line",
                phoneNumber = "741741",
                website = "https://www.crisistext.org",
                description = "Free, 24/7 crisis support via SMS",
                countryCode = "US"
            ),
            CrisisResource(
                name = "National Domestic Violence Hotline",
                phoneNumber = "1-800-799-7233",
                website = "https://www.thehotline.org",
                description = "24/7 support for domestic violence survivors",
                countryCode = "US"
            ),
            CrisisResource(
                name = "SAMHSA National Helpline",
                phoneNumber = "1-800-662-4357",
                website = "https://www.samhsa.gov/find-help/national-helpline",
                description = "Free, confidential treatment referral and information service",
                countryCode = "US"
            ),
            CrisisResource(
                name = "Veterans Crisis Line",
                phoneNumber = "988",
                website = "https://www.veteranscrisisline.net",
                description = "Crisis support for veterans and their families",
                countryCode = "US"
            )
        )
    }
}
