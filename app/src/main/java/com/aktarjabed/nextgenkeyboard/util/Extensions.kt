package com.aktarjabed.nextgenkeyboard.util

import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics

private var isCrashlyticsAvailable = true

fun Any.logError(message: String, throwable: Throwable? = null) {
    val tag = this::class.simpleName ?: "NextGenKeyboard"
    Log.e(tag, message, throwable)

    if (!isCrashlyticsAvailable) return

    try {
        FirebaseCrashlytics.getInstance().apply {
            log("$tag: $message")
            throwable?.let { recordException(it) }
        }
    } catch (e: Exception) {
        // Crashlytics not initialized or configured
        Log.w("NextGenKeyboard", "Crashlytics not available", e)
        isCrashlyticsAvailable = false
    }
}

fun Any.logWarning(message: String) {
    val tag = this::class.simpleName ?: "NextGenKeyboard"
    Log.w(tag, message)

    if (!isCrashlyticsAvailable) return

    try {
        FirebaseCrashlytics.getInstance().log("WARNING - $tag: $message")
    } catch (e: Exception) {
        // Ignore
        isCrashlyticsAvailable = false
    }
}

fun Any.logInfo(message: String) {
    val tag = this::class.simpleName ?: "NextGenKeyboard"
    Log.i(tag, message)
}
