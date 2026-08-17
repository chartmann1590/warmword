package com.charles.warmwords.app.analytics

import android.content.Context
import android.os.Bundle
import android.util.Log
import com.charles.warmwords.app.BuildConfig
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Thin, privacy-conscious wrapper around Firebase Crashlytics + Analytics.
 *
 * WarmWord is a mental health app: nothing sent here may ever include chat content, journal
 * text, or other free-form user input - only anonymous event names/counts and crash stack
 * traces. No-ops safely if Firebase hasn't been configured (see app/build.gradle.kts).
 */
@Singleton
class AnalyticsManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val enabled = BuildConfig.FIREBASE_ENABLED

    private val analytics: FirebaseAnalytics? by lazy {
        if (!enabled) return@lazy null
        runCatching { FirebaseAnalytics.getInstance(context) }.getOrNull()
    }

    private val crashlytics: FirebaseCrashlytics? by lazy {
        if (!enabled) return@lazy null
        runCatching { FirebaseCrashlytics.getInstance() }.getOrNull()
    }

    fun logEvent(name: String, params: Map<String, String> = emptyMap()) {
        if (!enabled) return
        runCatching {
            val bundle = Bundle().apply {
                params.forEach { (key, value) -> putString(key, value) }
            }
            analytics?.logEvent(name, bundle)
        }.onFailure { Log.w(TAG, "logEvent failed", it) }
    }

    fun recordNonFatal(throwable: Throwable) {
        if (!enabled) return
        runCatching { crashlytics?.recordException(throwable) }
            .onFailure { Log.w(TAG, "recordNonFatal failed", it) }
    }

    fun setBreadcrumb(message: String) {
        if (!enabled) return
        runCatching { crashlytics?.log(message) }
            .onFailure { Log.w(TAG, "setBreadcrumb failed", it) }
    }

    companion object {
        private const val TAG = "AnalyticsManager"

        // Screen view names
        const val SCREEN_CHAT = "chat"
        const val SCREEN_JOURNAL = "journal"
        const val SCREEN_INSIGHTS = "insights"
        const val SCREEN_FIND_HELP = "find_help"
        const val SCREEN_SETTINGS = "settings"

        // Event names - counts/state only, never user content
        const val EVENT_MESSAGE_SENT = "message_sent"
        const val EVENT_SESSION_STARTED = "session_started"
        const val EVENT_PERSONA_CHANGED = "persona_changed"
        const val EVENT_VOICE_CHANGED = "voice_changed"
        const val EVENT_VOICE_REPLIES_TOGGLED = "voice_replies_toggled"
        const val EVENT_JOURNAL_ENTRY_ADDED = "journal_entry_added"
        const val EVENT_REMINDER_SCHEDULED = "reminder_scheduled"
        const val EVENT_CRISIS_RESOURCE_OPENED = "crisis_resource_opened"
        const val EVENT_DATA_EXPORTED = "data_exported"
        const val EVENT_DATA_DELETED = "data_deleted"

        // Subscription / monetization events
        const val EVENT_SUBSCRIPTION_PURCHASED = "subscription_purchased"
        const val EVENT_PAYWALL_SHOWN = "paywall_shown"
        const val EVENT_PREMIUM_FEATURE_ATTEMPTED = "premium_feature_attempted"

        const val PARAM_PERSONA_ID = "persona_id"
        const val PARAM_SCREEN = "screen"
        const val PARAM_PRODUCT_ID = "product_id"
    }
}
