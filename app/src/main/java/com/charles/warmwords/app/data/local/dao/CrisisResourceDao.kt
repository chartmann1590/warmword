package com.charles.warmwords.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.charles.warmwords.app.data.local.entity.CrisisResource
import kotlinx.coroutines.flow.Flow

@Dao
interface CrisisResourceDao {
    @Insert
    suspend fun insert(resource: CrisisResource): Long

    @Insert
    suspend fun insertAll(resources: List<CrisisResource>)

    @Query("SELECT * FROM crisis_resources WHERE countryCode = :countryCode")
    fun getByCountry(countryCode: String): Flow<List<CrisisResource>>

    @Query("SELECT * FROM crisis_resources WHERE countryCode = 'US'")
    suspend fun getDefaultUSResources(): List<CrisisResource>

    @Query("DELETE FROM crisis_resources")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM crisis_resources")
    suspend fun getCount(): Int
}
