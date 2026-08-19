package com.charles.warmwords.billing

import com.charles.warmwords.domain.repository.SubscriptionRepository
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SubscriptionRepositoryImpl @Inject constructor(
    private val entitlementStore: EntitlementStore,
    private val billingManager: BillingManager
) : SubscriptionRepository {
    override fun observe(): StateFlow<SubscriptionState> = entitlementStore.observe()
    override suspend fun getState(): SubscriptionState = entitlementStore.getState()
    override suspend fun restore() = billingManager.restorePurchases()
}
