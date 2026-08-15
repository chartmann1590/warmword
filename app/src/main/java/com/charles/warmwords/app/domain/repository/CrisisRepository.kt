package com.charles.warmwords.app.domain.repository

import com.charles.warmwords.app.data.local.entity.CrisisResource
import kotlinx.coroutines.flow.Flow

interface CrisisRepository {
    fun getLocalCrisisResources(): Flow<List<CrisisResource>>
    suspend fun insert(resource: CrisisResource)
    suspend fun insertAll(resources: List<CrisisResource>)
    suspend fun deleteAll()
}
