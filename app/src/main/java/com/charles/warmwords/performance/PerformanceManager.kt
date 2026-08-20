package com.charles.warmwords.performance

import android.util.Log
import com.charles.warmwords.BuildConfig
import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.perf.metrics.Trace
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Thin wrapper around Firebase Performance Monitoring.
 *
 * Tracks timing only (download duration, latency) - never user content. No-ops
 * safely when Firebase hasn't been configured (see app/build.gradle.kts).
 */
@Singleton
class PerformanceManager @Inject constructor() {

    private val firebasePerformance: FirebasePerformance? by lazy {
        if (!BuildConfig.FIREBASE_ENABLED) return@lazy null
        runCatching { FirebasePerformance.getInstance() }.getOrNull()
    }

    private val active = HashMap<String, Trace>()

    /** Measures [block] under a custom trace named [name]. Never throws from tracing. */
    fun <T> trace(name: String, block: () -> T): T {
        val t = begin(name)
        return try {
            block()
        } finally {
            stop(t)
        }
    }

    /** Coroutine variant of [trace]. */
    suspend fun <T> traceSuspend(name: String, block: suspend () -> T): T {
        val t = begin(name)
        return try {
            block()
        } finally {
            stop(t)
        }
    }

    private fun begin(name: String): Trace? {
        val perf = firebasePerformance ?: return null
        return try {
            synchronized(active) {
                if (active.containsKey(name)) {
                    null // a trace with this name is already in-flight
                } else {
                    perf.newTrace(name).also { it.start() }.also { active[name] = it }
                }
            }
        } catch (t: Throwable) {
            Log.w(TAG, "begin trace failed: $name", t)
            null
        }
    }

    private fun stop(trace: Trace?) {
        if (trace == null) return
        try {
            trace.stop()
        } catch (t: Throwable) {
            Log.w(TAG, "stop trace failed", t)
        } finally {
            synchronized(active) { active.remove(trace.name) }
        }
    }

    companion object {
        private const val TAG = "PerformanceManager"

        // Custom trace names - timing data only, no user content
        const val TRACE_TRANSLATION_DOWNLOAD = "translation_model_download"
        const val TRACE_TRANSLATE_CALL = "mlkit_translate_call"
        const val TRACE_AI_INIT = "ai_model_init"
        const val TRACE_AI_STREAM = "ai_message_stream"
    }
}