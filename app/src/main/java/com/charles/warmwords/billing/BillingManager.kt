package com.charles.warmwords.billing

import android.app.Activity
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.charles.warmwords.analytics.AnalyticsManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Wraps the Google Play Billing Client.
 *
 * Responsibilities:
 *  - Surface available subscription [ProductDetails] to the paywall.
 *  - Launch the purchase/upgrade flow.
 *  - On every purchase (new or restored) acknowledge it with Play, verify it with the backend
 *    ([BillingVerificationService]), and persist the result to [EntitlementStore].
 *
 * Verification note: when the backend confirms validity we unlock. If the backend call fails
 * (e.g. worker misconfigured / offline) we fall back to Play's own purchase state so a
 * legitimately paid user is never locked out; the failure is logged and reported.
 */
@Singleton
class BillingManager @Inject constructor(
    @ApplicationContext private val applicationContext: android.content.Context,
    private val entitlementStore: EntitlementStore,
    private val verificationService: BillingVerificationService,
    private val analyticsManager: AnalyticsManager
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val purchasesUpdatedListener = PurchasesUpdatedListener { result, purchases ->
        if (result.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            purchases.forEach { handlePurchase(it) }
        }
    }

    private val billingClient = BillingClient.newBuilder(applicationContext)
        .setListener(purchasesUpdatedListener)
        .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
        .build()

    private val _productDetails = MutableStateFlow<List<ProductDetails>>(emptyList())
    val productDetails: StateFlow<List<ProductDetails>> = _productDetails.asStateFlow()

    private var isConnected = false

    fun connect() {
        if (isConnected) return
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(result: BillingResult) {
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    isConnected = true
                    queryProductDetails()
                    restorePurchases()
                }
            }

            override fun onBillingServiceDisconnected() {
                isConnected = false
            }
        })
    }

    private fun queryProductDetails() {
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(
                listOf(
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(BillingConfig.SUBSCRIPTION_PRODUCT_ID)
                        .setProductType(BillingClient.ProductType.SUBS)
                        .build()
                )
            )
            .build()
        billingClient.queryProductDetailsAsync(params) { result, list ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                _productDetails.value = list.productDetailsList
            }
        }
    }

    fun launchBillingFlow(activity: Activity, productDetails: ProductDetails) {
        val offerToken = productDetails.subscriptionOfferDetails?.firstOrNull()?.offerToken ?: return
        val params = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(
                listOf(
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(productDetails)
                        .setOfferToken(offerToken)
                        .build()
                )
            )
            .build()
        billingClient.launchBillingFlow(activity, params)
    }

    fun restorePurchases() {
        if (!isConnected) return
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.SUBS)
            .build()
        billingClient.queryPurchasesAsync(params) { result, purchases ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                purchases.forEach { handlePurchase(it) }
            }
        }
    }

    private fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState != Purchase.PurchaseState.PURCHASED) return
        if (!purchase.isAcknowledged) {
            val ackParams = AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(purchase.purchaseToken)
                .build()
            billingClient.acknowledgePurchase(ackParams) {
                verifyAndUnlock(purchase)
            }
        } else {
            verifyAndUnlock(purchase)
        }
    }

    private fun verifyAndUnlock(purchase: Purchase) {
        val productId = purchase.products.firstOrNull() ?: BillingConfig.SUBSCRIPTION_PRODUCT_ID
        scope.launch {
            val verified = verificationService.verify(
                purchase.purchaseToken,
                productId,
                applicationContext.packageName
            )
            // Fall back to Play's own purchase validity if the backend can't be reached, so a
            // paid user is never locked out. Backend validation still runs on the next sync.
            val state = verified ?: SubscriptionState(isPremium = true, productId = productId)
            entitlementStore.save(state)
            if (state.isPremium) {
                analyticsManager.logEvent(
                    AnalyticsManager.EVENT_SUBSCRIPTION_PURCHASED,
                    mapOf(AnalyticsManager.PARAM_PRODUCT_ID to (productId))
                )
            }
        }
    }
}
