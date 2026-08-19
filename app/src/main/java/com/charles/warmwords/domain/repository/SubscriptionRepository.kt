package com.charles.warmwords.domain.repository

import com.charles.warmwords.billing.SubscriptionState
import kotlinx.coroutines.flow.StateFlow

/**
 * Read facade over the user's subscription entitlement, plus a hook to ask Play Billing to
 * re-sync (restore) existing purchases.
 */
interface SubscriptionRepository {
    fun observe(): StateFlow<SubscriptionState>
    suspend fun getState(): SubscriptionState
    suspend fun restore()
}
