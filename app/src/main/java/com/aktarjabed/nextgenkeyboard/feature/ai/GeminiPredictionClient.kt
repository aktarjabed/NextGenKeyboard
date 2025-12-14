package com.aktarjabed.nextgenkeyboard.feature.ai

import com.aktarjabed.nextgenkeyboard.BuildConfig
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.GenerationConfig
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeminiPredictionClient @Inject constructor() : AiPredictionClient {

    private val apiKey = BuildConfig.GEMINI_API_KEY
    private var generativeModel: GenerativeModel? = null

    // Cache and Rate Limiting
    private val cache = java.util.concurrent.ConcurrentHashMap<String, List<String>>()
    private var requestCount = 0
    private var lastResetTime = System.currentTimeMillis()
    private val REQUEST_LIMIT_PER_MINUTE = 5
    private val RESET_INTERVAL_MS = 60 * 1000L

    init {
        if (isAvailable()) {
            try {
                generativeModel = GenerativeModel(
                    modelName = "gemini-1.5-flash", // Use a fast model
                    apiKey = apiKey,
                    generationConfig = GenerationConfig.builder()
                        .build()
                )
            } catch (e: Exception) {
                Timber.e(e, "Failed to initialize Gemini model")
            }
        }
    }

    override fun isAvailable(): Boolean {
        return apiKey.isNotBlank()
    }

    override suspend fun generatePredictions(prompt: String): List<String> {
        if (!isAvailable()) return emptyList()

        // Check local cache first
        if (cache.containsKey(prompt)) {
            Timber.d("Gemini: Returning cached prediction for '$prompt'")
            return cache[prompt] ?: emptyList()
        }

        // Rate Limiting Logic
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastResetTime > RESET_INTERVAL_MS) {
            requestCount = 0
            lastResetTime = currentTime
        }

        if (requestCount >= REQUEST_LIMIT_PER_MINUTE) {
            Timber.w("Gemini: Rate limit exceeded ($REQUEST_LIMIT_PER_MINUTE requests/min). Skipping API call.")
            return emptyList()
        }

        return try {
            requestCount++
            Timber.d("Gemini: Sending request ($requestCount/$REQUEST_LIMIT_PER_MINUTE)")

            val response = generativeModel?.generateContent(prompt)
            val text = response?.text ?: return emptyList()

            // Expecting a comma-separated list or newlines.
            val predictions = text.split(",", "\n")
                .map { it.trim() }
                .filter { it.isNotBlank() }
                .take(3) // Limit to 3 suggestions

            // Cache the result
            if (predictions.isNotEmpty()) {
                cache[prompt] = predictions
                // Basic LRU-like cleanup: if cache grows too big, clear it
                if (cache.size > 100) {
                    cache.clear()
                }
            }

            predictions
        } catch (e: Exception) {
            Timber.e(e, "Error generating predictions with Gemini")
            emptyList()
        }
    }
}
