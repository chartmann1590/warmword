package com.charles.warmwords.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.charles.warmwords.data.local.entity.UserProfile
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProfileDao {
    @Insert
    suspend fun insert(profile: UserProfile)

    @Update
    suspend fun update(profile: UserProfile)

    @Query("SELECT * FROM user_profile WHERE id = 'default'")
    fun getDefaultProfile(): Flow<UserProfile?>

    @Query("SELECT * FROM user_profile WHERE id = 'default'")
    suspend fun getDefaultProfileSync(): UserProfile?

    @Query("DELETE FROM user_profile")
    suspend fun deleteAll()
}
