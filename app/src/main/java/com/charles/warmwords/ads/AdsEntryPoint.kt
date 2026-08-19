package com.charles.warmwords.ads

import com.charles.warmwords.ads.AdsManager
import com.charles.warmwords.domain.repository.SubscriptionRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface AdsEntryPoint {
    fun adsManager(): AdsManager
    fun subscriptionRepository(): SubscriptionRepository
}
