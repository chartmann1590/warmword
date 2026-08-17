package com.charles.warmwords.app.billing

import com.charles.warmwords.app.BuildConfig

/**
 * Central billing configuration.
 *
 * - [SUBSCRIPTION_PRODUCT_ID] must match the subscription created in the Play Console.
 * - [VERIFY_URL] is the Cloudflare Worker endpoint that verifies a purchase token against the
 *   Play Developer API. Supplied via BuildConfig (local.properties BILLING_VERIFY_URL), with a
 *   placeholder default that the developer replaces before release.
 * - [APP_ID] is sent as the `X-WarmWord-App-Id` header so the Worker accepts the request. It
 *   must match the Worker's `ALLOWED_APP_ID` secret.
 */
object BillingConfig {
    const val SUBSCRIPTION_PRODUCT_ID = "warmword_premium"
    const val APP_ID = "com.charles.warmwords.app"

    val VERIFY_URL: String
        get() = BuildConfig.BILLING_VERIFY_URL
}
