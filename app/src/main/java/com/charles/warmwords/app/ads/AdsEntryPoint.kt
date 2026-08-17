package com.charles.warmwords.app.ads

import com.charles.warmwords.app.ads.AdsManager
import com.charles.warmwords.app.domain.repository.SubscriptionRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface AdsEntryPoint {
    fun adsManager(): AdsManager
    fun subscriptionRepository(): SubscriptionRepository
}
