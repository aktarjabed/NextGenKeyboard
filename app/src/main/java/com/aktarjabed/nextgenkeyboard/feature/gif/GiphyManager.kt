package com.aktarjabed.nextgenkeyboard.feature.gif

import android.content.Context
import com.giphy.sdk.core.models.Media
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GiphyManager @Inject constructor(
    private val context: Context
) {
    // private var apiClient: GPHApi? = null
    private var isInitialized = false

    fun initialize(apiKey: String) {
        // Stub implementation
        isInitialized = true
    }

    fun trendingGifs(callback: (List<Media>) -> Unit) {
        // Stub implementation
        callback(emptyList())
    }

    fun searchGifs(query: String, callback: (List<Media>) -> Unit) {
        // Stub implementation
        callback(emptyList())
    }

    fun isReady(): Boolean = isInitialized
}
