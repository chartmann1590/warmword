package com.charles.warmwords.di

import com.charles.warmwords.translation.DeviceTranslator
import com.charles.warmwords.translation.MlKitDeviceTranslator
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class TranslationModule {

    @Binds
    @Singleton
    abstract fun bindDeviceTranslator(impl: MlKitDeviceTranslator): DeviceTranslator
}