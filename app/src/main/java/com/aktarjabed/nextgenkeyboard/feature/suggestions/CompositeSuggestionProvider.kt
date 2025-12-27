package com.aktarjabed.nextgenkeyboard.feature.suggestions

import com.aktarjabed.nextgenkeyboard.data.model.Language
import com.aktarjabed.nextgenkeyboard.feature.ai.SmartPredictionUseCase
import com.aktarjabed.nextgenkeyboard.feature.autocorrect.AdvancedAutocorrectEngine
import com.aktarjabed.nextgenkeyboard.feature.autocorrect.WordContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * CompositeSuggestionProvider
 * Merges suggestions from Local Autocorrect Engine (fast, typo-focused)
 * and AI Prediction Engine (slow, context-aware).
 */
@Singleton
class CompositeSuggestionProvider @Inject constructor(
    private val localEngine: AdvancedAutocorrectEngine,
    private val aiUseCase: SmartPredictionUseCase
) {

    suspend fun getMergedSuggestions(
        currentWord: String,
        contextText: String,
        languageCode: String
    ): List<String> = withContext(Dispatchers.Default) {

        // 1. Get Local Suggestions (Fast)
        val localDeferred = try {
            val lang = Language(code = languageCode, name = "", nativeName = "", layouts = emptyList(), isRTL = false)
            val context = WordContext(previousWord = contextText.substringAfterLast(" "))
            localEngine.getAdvancedSuggestions(currentWord, context, lang)
        } catch (e: Exception) {
            Timber.e(e, "Error getting local suggestions")
            emptyList()
        }

        // 2. Get AI Predictions (Slow - only if context is sufficient)
        // We only ask AI if we are finishing a word or have a sentence context
        val aiSuggestions = if (currentWord.isBlank() || contextText.length > 10) {
            try {
                aiUseCase.getPredictions(contextText)
            } catch (e: Exception) {
                Timber.e(e, "Error getting AI predictions")
                emptyList()
            }
        } else {
            emptyList()
        }

        // 3. Merge Logic
        val merged = mutableListOf<String>()

        // A. Priority: High confidence local correction (e.g., "teh" -> "the")
        val topLocal = localDeferred.firstOrNull()
        if (topLocal != null && topLocal.confidence > 0.8f) {
            merged.add(topLocal.suggestion)
        }

        // B. Add AI predictions (Next word / completion)
        aiSuggestions.forEach { aiSugg ->
            if (aiSugg.isNotBlank() && !containsIgnoreCase(merged, aiSugg)) {
                merged.add(aiSugg)
            }
        }

        // C. Fill remaining slots with local suggestions (including the top one if it wasn't added in Step A)
        localDeferred.forEach { local ->
            if (merged.size < 3 && !containsIgnoreCase(merged, local.suggestion)) {
                merged.add(local.suggestion)
            }
        }

        return@withContext merged.take(3)
    }

    // Helper: case-insensitive contains
    private fun containsIgnoreCase(list: List<String>, item: String): Boolean {
        return list.any { it.equals(item, ignoreCase = true) }
    }
}
