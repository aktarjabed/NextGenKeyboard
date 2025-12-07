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

    init {
        if (isAvailable()) {
            try {
                generativeModel = GenerativeModel(
                    modelName = "gemini-1.5-flash", // Use a fast model
                    apiKey = apiKey,
                    generationConfig = GenerationConfig.builder()
                        .temperature(0.2f) // Low temperature for deterministic/focused predictions
                        .topK(3)
                        .topP(0.8f)
                        .maxOutputTokens(20) // We only need a few words
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

        return try {
            val response = generativeModel?.generateContent(prompt)
            val text = response?.text ?: return emptyList()

            // Expecting a comma-separated list or newlines.
            // The prompt should enforce this format.
            text.split(",", "\n")
                .map { it.trim() }
                .filter { it.isNotBlank() }
                .take(3) // Limit to 3 suggestions
        } catch (e: Exception) {
            Timber.e(e, "Error generating predictions with Gemini")
            emptyList()
        }
    }
}
