package com.aktarjabed.nextgenkeyboard.util

import android.content.Context
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

private var isCrashlyticsAvailable = true

// ================== LOGGING EXTENSIONS ==================

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

// ================== CONTEXT EXTENSIONS ==================

fun Context.showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}

fun Context.hideKeyboard(view: View) {
    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
    imm?.hideSoftInputFromWindow(view.windowToken, 0)
}

// ================== FLOW EXTENSIONS ==================

suspend fun <T> Flow<T>.firstOrNull(): T? {
    return try {
        first()
    } catch (e: NoSuchElementException) {
        null
    }
}

// ================== STRING EXTENSIONS ==================

fun String.truncate(maxLength: Int): String {
    return if (length <= maxLength) this else substring(0, maxLength) + "..."
}

fun String.isValidClipContent(): Boolean {
    return isNotBlank() && length <= 10000 // Inline max length instead of Constants
}

// ================== VIEW EXTENSIONS ==================

fun View.visible() {
    visibility = View.VISIBLE
}

fun View.invisible() {
    visibility = View.INVISIBLE
}

fun View.gone() {
    visibility = View.GONE
}
