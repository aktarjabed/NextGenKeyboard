package com.aktarjabed.nextgenkeyboard.feature.gif

import android.content.Context
import com.giphy.sdk.core.GiphyCore
import com.giphy.sdk.core.models.Media
import com.giphy.sdk.core.network.api.GPHApi
import com.giphy.sdk.core.network.api.GPHApiClient
import com.giphy.sdk.core.network.response.MediaResponse
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GiphyManager @Inject constructor(
    private val context: Context
) {
    private var apiClient: GPHApi? = null
    private var isInitialized = false

    fun initialize(apiKey: String) {
        if (apiKey.isBlank()) {
            Timber.w("Giphy API key is blank, GIF functionality disabled")
            return
        }

        try {
            GiphyCore.configure(context, apiKey)
            apiClient = GPHApiClient(apiKey)
            isInitialized = true
            Timber.d("✅ GiphyManager initialized successfully")
        } catch (e: Exception) {
            Timber.e(e, "Failed to initialize GiphyManager")
            isInitialized = false
        }
    }

    fun trendingGifs(callback: (List<Media>) -> Unit) {
        if (!isInitialized) {
            callback(emptyList())
            return
        }

        apiClient?.trending { mediaResponse: MediaResponse?, throwable: Throwable? ->
            throwable?.let {
                Timber.e(it, "Error fetching trending GIFs")
                callback(emptyList())
                return@trending
            }

            callback(mediaResponse?.data?.take(20) ?: emptyList())
        } ?: callback(emptyList())
    }

    fun searchGifs(query: String, callback: (List<Media>) -> Unit) {
        if (!isInitialized || query.isBlank()) {
            callback(emptyList())
            return
        }

        apiClient?.search(query) { mediaResponse: MediaResponse?, throwable: Throwable? ->
            throwable?.let {
                Timber.e(it, "Error searching GIFs")
                callback(emptyList())
                return@search
            }

            callback(mediaResponse?.data?.take(20) ?: emptyList())
        } ?: callback(emptyList())
    }

    fun isReady(): Boolean = isInitialized
}