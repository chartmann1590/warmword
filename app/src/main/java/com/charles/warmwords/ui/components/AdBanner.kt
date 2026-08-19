package com.charles.warmwords.ui.components

import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.charles.warmwords.BuildConfig
import com.charles.warmwords.ads.AdsEntryPoint
import com.charles.warmwords.ads.AdsManager
import com.charles.warmwords.domain.repository.SubscriptionRepository
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import dagger.hilt.android.EntryPointAccessors
import androidx.compose.ui.platform.LocalContext

/**
 * Renders a standard AdMob banner. The view collapses (GONE) when no ad is available so it
 * never leaves an empty placeholder box on screen.
 */
@Composable
fun AdBanner(
    modifier: Modifier = Modifier,
    adUnitId: String = BuildConfig.ADMOB_BANNER_ID
) {
    val context = LocalContext.current
    val adsManager = remember {
        EntryPointAccessors.fromApplication(
            context.applicationContext,
            AdsEntryPoint::class.java
        ).adsManager()
    }
    val subscriptionRepository = remember {
        EntryPointAccessors.fromApplication(
            context.applicationContext,
            AdsEntryPoint::class.java
        ).subscriptionRepository()
    }
    val subscriptionState by subscriptionRepository.observe().collectAsState()

    if (subscriptionState.isPremium) return

    AndroidView(
        modifier = modifier.fillMaxWidth(),
        factory = { ctx ->
            AdView(ctx).apply {
                setAdSize(AdSize.BANNER)
                this.adUnitId = adUnitId
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                adListener = object : AdListener() {
                    override fun onAdFailedToLoad(error: LoadAdError) {
                        visibility = View.GONE
                    }
                }
                loadAd(adsManager.buildAdRequest())
            }
        }
    )
}
