package com.nextgen.keyboard.util

import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics

fun Any.logError(message: String, throwable: Throwable? = null) {
    val tag = this::class.simpleName ?: "NextGenKeyboard"
    Log.e(tag, message, throwable)

    // Report to Crashlytics
    FirebaseCrashlytics.getInstance().apply {
        log("$tag: $message")
        throwable?.let { recordException(it) }
    }
}

fun Any.logWarning(message: String) {
    val tag = this::class.simpleName ?: "NextGenKeyboard"
    Log.w(tag, message)
    FirebaseCrashlytics.getInstance().log("WARNING - $tag: $message")
}

fun Any.logInfo(message: String) {
    val tag = this::class.simpleName ?: "NextGenKeyboard"
    Log.i(tag, message)
}