package com.charles.warmwords.domain.repository

import com.charles.warmwords.data.local.entity.CrisisResource
import kotlinx.coroutines.flow.Flow

interface CrisisRepository {
    fun getLocalCrisisResources(): Flow<List<CrisisResource>>
    suspend fun insert(resource: CrisisResource)
    suspend fun insertAll(resources: List<CrisisResource>)
    suspend fun deleteAll()
}
