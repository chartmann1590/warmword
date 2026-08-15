package com.charles.warmwords.app.domain.usecase

import com.charles.warmwords.app.data.local.entity.CrisisResource
import com.charles.warmwords.app.domain.repository.CrisisRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class CrisisUseCases @Inject constructor(
    private val repository: CrisisRepository
) {
    fun getLocalResources(): Flow<List<CrisisResource>> = repository.getLocalCrisisResources()

    suspend fun insert(resource: CrisisResource) = repository.insert(resource)

    suspend fun insertAll(resources: List<CrisisResource>) = repository.insertAll(resources)

    suspend fun deleteAll() = repository.deleteAll()
}
