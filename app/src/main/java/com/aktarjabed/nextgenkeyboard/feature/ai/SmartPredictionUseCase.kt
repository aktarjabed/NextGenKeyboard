package com.aktarjabed.nextgenkeyboard.feature.ai

import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withTimeout
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SmartPredictionUseCase @Inject constructor(
    private val predictionEngine: PredictionEngine,
    private val promptBuilder: PromptBuilder
) {

    companion object {
        private const val PREDICTION_TIMEOUT_MS = 5000L
        private const val MAX_CONTEXT_LENGTH = 500
        private const val MIN_CONTEXT_LENGTH = 3
    }

    /**
     * Get AI-powered predictions with robust error handling
     * @param contextText The text context for predictions
     * @return List of prediction suggestions, empty list on error
     */
    suspend fun getPredictions(contextText: String): List<String> = withContext(Dispatchers.IO) {
        try {
            // Validate input
            if (contextText.isBlank() || contextText.length < MIN_CONTEXT_LENGTH) {
                Timber.d("Context too short for predictions: ${contextText.length} chars")
                return@withContext emptyList()
            }

            // Sanitize and limit context length for safety
            val safeContext = sanitizeContext(contextText.takeLast(MAX_CONTEXT_LENGTH))
            
            if (safeContext.isBlank()) {
                Timber.w("Context sanitization resulted in empty text")
                return@withContext emptyList()
            }

            // Build prompt with error handling
            val prompt = try {
                promptBuilder.buildPredictionPrompt(safeContext)
            } catch (e: Exception) {
                Timber.e(e, "Error building prediction prompt")
                return@withContext emptyList()
            }

            // Generate predictions with timeout
            // Note: UnifiedPredictionEngine now handles timeout internally for Gemini,
            // but we keep this outer timeout for safety.
            val predictions = withTimeout(PREDICTION_TIMEOUT_MS) {
                try {
                    predictionEngine.predict(prompt)
                } catch (e: Exception) {
                    Timber.e(e, "Error generating predictions from engine")
                    emptyList()
                }
            }

            // Validate and filter predictions
            predictions
                .filter { it.isNotBlank() && it.length <= 50 }
                .take(5)
                .also {
                    if (it.isNotEmpty()) {
                        Timber.d("Generated ${it.size} predictions")
                    }
                }

        } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
            Timber.w("Prediction generation timed out after ${PREDICTION_TIMEOUT_MS}ms")
            emptyList()
        } catch (e: Exception) {
            Timber.e(e, "Unexpected error during prediction generation")
            emptyList()
        }
    }

    /**
     * Sanitize context text to prevent injection or malformed input
     */
    private fun sanitizeContext(text: String): String {
        return text
            .trim()
            .replace(Regex("\\s+"), " ") // Normalize whitespace
            .take(MAX_CONTEXT_LENGTH)
    }
}
