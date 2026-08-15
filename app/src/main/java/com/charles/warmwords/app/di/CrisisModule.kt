package com.charles.warmwords.app.di

import com.charles.warmwords.app.data.repository.CrisisRepositoryImpl
import com.charles.warmwords.app.domain.repository.CrisisRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class CrisisModule {
    @Binds
    @Singleton
    abstract fun bindCrisisRepository(impl: CrisisRepositoryImpl): CrisisRepository
}
