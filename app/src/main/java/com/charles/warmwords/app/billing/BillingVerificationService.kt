package com.charles.warmwords.app.billing

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Calls the Cloudflare Worker `/billing/verify` endpoint, which validates the Play purchase
 * token against the Play Developer API server-side (so thePlay  service-account secret never
 * ships in the app). Returns the verified [SubscriptionState], or null on any transport/parse
 * failure.
 */
@Singleton
class BillingVerificationService @Inject constructor() {

    suspend fun verify(
        purchaseToken: String,
        productId: String,
        packageName: String
    ): SubscriptionState? = withContext(Dispatchers.IO) {
        try {
            val url = URL(BillingConfig.VERIFY_URL)
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("X-WarmWord-App-Id", BillingConfig.APP_ID)
                doOutput = true
                connectTimeout = 15_000
                readTimeout = 15_000
            }
            val body = JSONObject().apply {
                put("packageName", packageName)
                put("productId", productId)
                put("purchaseToken", purchaseToken)
            }.toString()
            conn.outputStream.use { it.write(body.toByteArray()) }

            val code = conn.responseCode
            if (code !in 200..299) {
                Log.w(TAG, "Verification HTTP $code")
                return@withContext null
            }
            val json = JSONObject(conn.inputStream.bufferedReader().readText())
            SubscriptionState(
                isPremium = json.optBoolean("isValid", false),
                productId = json.optString("productId").takeIf { it.isNotBlank() },
                expiryTimeMillis = json.optLong("expiryTimeMillis", 0L),
                isAutoRenewing = json.optBoolean("isAutoRenewing", false)
            )
        } catch (e: Exception) {
            Log.w(TAG, "Verification failed", e)
            null
        }
    }

    companion object {
        private const val TAG = "BillingVerification"
    }
}
