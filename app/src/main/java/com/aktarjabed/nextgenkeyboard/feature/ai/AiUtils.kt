package com.aktarjabed.nextgenkeyboard.feature.ai

import timber.log.Timber

/**
 * Utility class for rate limiting actions.
 */
class RateLimiter(
    private val limit: Int,
    private val intervalMs: Long
) {
    private var requestCount = 0
    private var lastResetTime = System.currentTimeMillis()

    @Synchronized
    fun tryAcquire(): Boolean {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastResetTime > intervalMs) {
            requestCount = 0
            lastResetTime = currentTime
        }

        if (requestCount >= limit) {
            Timber.w("Rate limit exceeded ($limit requests/${intervalMs}ms).")
            return false
        }

        requestCount++
        return true
    }

    fun getUsage(): String = "$requestCount/$limit"
}

object AiUtils {
    /**
     * Parses a raw AI text response into a list of predictions.
     * Expects comma-separated values or newlines.
     */
    fun parsePredictions(text: String, maxSuggestions: Int = 3): List<String> {
        return text.split(",", "\n")
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinct()
            .take(maxSuggestions)
    }
}
