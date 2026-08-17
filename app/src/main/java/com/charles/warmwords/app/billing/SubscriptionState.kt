package com.charles.warmwords.app.billing

/**
 * Local mirror of the subscription entitlement, sourced from the Play Billing purchase after
 * it has been verified by the backend (Cloudflare Worker -> Play Developer API).
 *
 * `isPremium` is the single gate consumed by ad-free and premium-persona logic.
 */
data class SubscriptionState(
    val isPremium: Boolean = false,
    val productId: String? = null,
    val expiryTimeMillis: Long = 0L,
    val isAutoRenewing: Boolean = false
)
