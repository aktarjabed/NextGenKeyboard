package com.aktarjabed.nextgenkeyboard.feature.ai

import com.aktarjabed.nextgenkeyboard.data.model.Language
import com.aktarjabed.nextgenkeyboard.feature.autocorrect.AdvancedAutocorrectEngine
import com.aktarjabed.nextgenkeyboard.feature.autocorrect.WordContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.TimeoutCancellationException

@Singleton
class UnifiedPredictionEngine @Inject constructor(
    private val geminiClient: GeminiPredictionClient,
    private val localEngine: AdvancedAutocorrectEngine
) : PredictionEngine {

    // Default timeout for Gemini to ensure UI responsiveness
    private val GEMINI_TIMEOUT_MS = 1000L

    override suspend fun predict(context: String): List<String> {
        if (context.isBlank()) return emptyList()

        // 1. Try Gemini (Cloud) first
        try {
            if (geminiClient.isAvailable()) {
                val predictions = withTimeout(GEMINI_TIMEOUT_MS) {
                    geminiClient.generatePredictions(context)
                }
                if (predictions.isNotEmpty()) {
                    return predictions
                }
            }
        } catch (e: TimeoutCancellationException) {
            Timber.w("Gemini prediction timed out, falling back to local engine.")
        } catch (e: Exception) {
            Timber.e(e, "Gemini prediction failed, falling back to local engine.")
        }

        // 2. Fallback to Local Engine
        return try {
            // Extract last word for local engine context
            val lastWord = context.trim().split("\\s+".toRegex()).lastOrNull() ?: ""
            // Mocking a default language (EN) for now as context doesn't carry language info yet.
            // In a real scenario, PredictionEngine should probably accept language param or fetch from repo.
            val mockLanguage = Language(
                 code = "en", name = "English", nativeName = "English", flagIcon = "🇺🇸",
                 layouts = listOf(com.aktarjabed.nextgenkeyboard.data.model.LanguageKeyboardDatabase.getLayout("en").toLanguageLayout())
            )

            // Build a WordContext
            val wordContext = WordContext(
                previousWord = context.trim().substringBeforeLast(lastWord).trim().split("\\s+".toRegex()).lastOrNull() ?: "",
                isStartOfSentence = context.trim().endsWith(".")
            )

            val suggestions = localEngine.getAdvancedSuggestions(lastWord, wordContext, mockLanguage)
            suggestions.map { it.suggestion }
        } catch (e: Exception) {
            Timber.e(e, "Local prediction failed.")
            emptyList()
        }
    }
}
