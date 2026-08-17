package com.charles.warmwords.app.ui.screens.paywall

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.billingclient.api.ProductDetails
import com.charles.warmwords.app.analytics.AnalyticsManager
import com.charles.warmwords.app.billing.BillingManager
import com.charles.warmwords.app.billing.SubscriptionState
import com.charles.warmwords.app.domain.repository.SubscriptionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PaywallViewModel @Inject constructor(
    private val billingManager: BillingManager,
    private val subscriptionRepository: SubscriptionRepository,
    private val analyticsManager: AnalyticsManager
) : ViewModel() {

    val productDetails: StateFlow<List<ProductDetails>> = billingManager.productDetails
    val subscription: StateFlow<SubscriptionState> = subscriptionRepository.observe()

    init {
        analyticsManager.logEvent(AnalyticsManager.EVENT_PAYWALL_SHOWN)
    }

    fun purchase(activity: Activity, productDetails: ProductDetails) {
        billingManager.launchBillingFlow(activity, productDetails)
    }

    fun restore() {
        viewModelScope.launch { subscriptionRepository.restore() }
    }
}
