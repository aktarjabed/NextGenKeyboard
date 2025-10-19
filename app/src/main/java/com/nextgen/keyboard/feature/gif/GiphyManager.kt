package com.nextgen.keyboard.feature.gif

import android.content.Context
import com.giphy.sdk.core.Giphy
import com.giphy.sdk.core.models.Media
import com.giphy.sdk.core.network.api.CompletionHandler
import com.giphy.sdk.core.network.api.GPHApi
import com.giphy.sdk.core.network.response.ListMediaResponse
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GiphyManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var giphyApi: GPHApi? = null

    fun initialize(apiKey: String) {
        if (apiKey.isBlank() || apiKey == "YOUR_GIPHY_API_KEY") {
            Timber.w("Giphy API key is not set. GIF feature will be disabled.")
            return
        }
        Giphy.configure(context, apiKey)
        giphyApi = Giphy.getCore()
    }

    fun searchGifs(query: String, completion: (List<Media>) -> Unit) {
        giphyApi?.search(query, null, null, null, null, null, object : CompletionHandler<ListMediaResponse> {
            override fun onComplete(result: ListMediaResponse?, e: Throwable?) {
                if (e != null) {
                    Timber.e(e, "Error searching for GIFs")
                    completion(emptyList())
                    return
                }
                completion(result?.data ?: emptyList())
            }
        }) ?: completion(emptyList())
    }

    fun trendingGifs(completion: (List<Media>) -> Unit) {
        giphyApi?.trending(null, null, null, object : CompletionHandler<ListMediaResponse> {
            override fun onComplete(result: ListMediaResponse?, e: Throwable?) {
                if (e != null) {
                    Timber.e(e, "Error fetching trending GIFs")
                    completion(emptyList())
                    return
                }
                completion(result?.data ?: emptyList())
            }
        }) ?: completion(emptyList())
    }
}