package com.charles.warmwords.app.billing

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.charles.warmwords.app.domain.repository.SubscriptionRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Durable, encrypted store for the verified subscription entitlement.
 *
 * We deliberately keep entitlement out of the Room database (which uses
 * `fallbackToDestructiveMigration(true)`) so a schema bump can't wipe a user's premium state.
 * EncryptedSharedPreferences keeps the purchase token/entitlement private at rest.
 */
@Singleton
class EntitlementStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs = EncryptedSharedPreferences.create(
        context,
        PREFS_NAME,
        MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    private val _state = MutableStateFlow(read())

    fun observe(): StateFlow<SubscriptionState> = _state.asStateFlow()
    fun getState(): SubscriptionState = _state.value

    fun save(state: SubscriptionState) {
        prefs.edit().apply {
            putBoolean(KEY_PREMIUM, state.isPremium)
            putString(KEY_PRODUCT, state.productId)
            putLong(KEY_EXPIRY, state.expiryTimeMillis)
            putBoolean(KEY_RENEWING, state.isAutoRenewing)
        }.apply()
        _state.value = state
    }

    fun clear() {
        prefs.edit().clear().apply()
        _state.value = SubscriptionState()
    }

    private fun read(): SubscriptionState = SubscriptionState(
        isPremium = prefs.getBoolean(KEY_PREMIUM, false),
        productId = prefs.getString(KEY_PRODUCT, null),
        expiryTimeMillis = prefs.getLong(KEY_EXPIRY, 0L),
        isAutoRenewing = prefs.getBoolean(KEY_RENEWING, false)
    )

    companion object {
        private const val PREFS_NAME = "warmword_subscription"
        private const val KEY_PREMIUM = "is_premium"
        private const val KEY_PRODUCT = "product_id"
        private const val KEY_EXPIRY = "expiry_time_millis"
        private const val KEY_RENEWING = "is_auto_renewing"
    }
}
