package com.charles.warmwords.ads

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Stores ad-related user preferences.
 *
 * For a mental-health app, privacy is paramount. Personalized advertising is therefore
 * OFF by default: ads are only ever requested as non-personalized unless the user explicitly
 * opts in from Settings.
 */
@Singleton
class AdsPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("warmword_ads", Context.MODE_PRIVATE)

    val personalizedAdsEnabled: Boolean
        get() = prefs.getBoolean(KEY_PERSONALIZED_ADS, false)

    fun setPersonalizedAdsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_PERSONALIZED_ADS, enabled).apply()
    }

    companion object {
        private const val KEY_PERSONALIZED_ADS = "personalized_ads_enabled"
    }
}
