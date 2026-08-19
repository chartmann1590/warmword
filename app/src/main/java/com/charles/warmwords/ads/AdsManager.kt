package com.charles.warmwords.ads

import android.app.Activity
import android.os.Bundle
import com.charles.warmwords.BuildConfig
import com.charles.warmwords.billing.EntitlementStore
import com.charles.warmwords.ui.navigation.Screen
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import android.content.Context

/**
 * Central AdMob controller for WarmWord.
 *
 * Privacy-conscious defaults for a mental-health app:
 *  - Ads are capped to G-rated content.
 *  - Non-personalized ads are requested unless the user opts in (see [AdsPreferences]).
 *  - Interstitials are never shown on or around the Chat or Find Help / crisis screens, and are
 *    throttled by a cooldown so the experience stays calm and non-intrusive.
 */
@Singleton
class AdsManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val adsPreferences: AdsPreferences,
    private val entitlementStore: EntitlementStore
) {
    private var interstitial: InterstitialAd? = null
    private var lastInterstitialShownMs = 0L

    /** Minimum gap between two interstitial impressions (2 minutes). */
    private val interstitialCooldownMs = 2 * 60 * 1000L

    /** Routes where an interstitial must never appear (therapeutic conversation & crisis help). */
    private val protectedRoutes = setOf(
        Screen.Onboarding.route,
        Screen.Chat.route,
        Screen.FindHelp.route
    )

    fun initialize() {
        val config = RequestConfiguration.Builder()
            .setMaxAdContentRating(RequestConfiguration.MAX_AD_CONTENT_RATING_G)
            .setTagForChildDirectedTreatment(RequestConfiguration.TAG_FOR_CHILD_DIRECTED_TREATMENT_UNSPECIFIED)
            .setTagForUnderAgeOfConsent(RequestConfiguration.TAG_FOR_UNDER_AGE_OF_CONSENT_UNSPECIFIED)
            .build()
        MobileAds.setRequestConfiguration(config)
        MobileAds.initialize(context)
        loadInterstitial()
    }

    /**
     * Builds an [AdRequest]. When personalized ads are disabled (the default) we attach the
     * AdMob "npa" extra so Google serves non-personalized ads only.
     */
    fun buildAdRequest(): AdRequest {
        val builder = AdRequest.Builder()
        if (!adsPreferences.personalizedAdsEnabled) {
            builder.addNetworkExtrasBundle(
                AdMobAdapter::class.java,
                Bundle().apply { putString("npa", "1") }
            )
        }
        return builder.build()
    }

    private fun loadInterstitial() {
        InterstitialAd.load(
            context,
            BuildConfig.ADMOB_INTERSTITIAL_ID,
            buildAdRequest(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitial = ad
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    interstitial = null
                }
            }
        )
    }

    /**
     * Shows a pre-loaded interstitial if allowed. Returns true if one was shown.
     * Interstitials are suppressed around protected (chat / crisis) routes and while the
     * cooldown window is active.
     */
    fun maybeShowInterstitial(
        activity: Activity,
        targetRoute: String,
        currentRoute: String
    ): Boolean {
        if (entitlementStore.getState().isPremium) return false
        if (targetRoute in protectedRoutes || currentRoute in protectedRoutes) return false
        if (System.currentTimeMillis() - lastInterstitialShownMs < interstitialCooldownMs) return false

        val ad = interstitial ?: return false
        lastInterstitialShownMs = System.currentTimeMillis()
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                interstitial = null
                loadInterstitial()
            }

            override fun onAdFailedToShowFullScreenContent(adError: com.google.android.gms.ads.AdError) {
                interstitial = null
                loadInterstitial()
            }
        }
        ad.show(activity)
        return true
    }
}
