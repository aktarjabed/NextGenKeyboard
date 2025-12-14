package com.aktarjabed.nextgenkeyboard.feature.autocorrect

import android.content.Context
import androidx.collection.LruCache
import com.aktarjabed.nextgenkeyboard.R
import com.aktarjabed.nextgenkeyboard.data.model.Language
import com.aktarjabed.nextgenkeyboard.state.CorrectionType
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import timber.log.Timber
import java.io.BufferedReader
import java.util.Collections
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.min

/**
 * Advanced Autocorrect Engine
 * - Handles dictionary loading
 * - Provides suggestions based on context and spelling
 * - Manages learned words
 *
 * Rewritten to remove duplicated code and ensure thread safety.
 */
@Singleton
class AdvancedAutocorrectEngine @Inject constructor(
    @ApplicationContext private val context: Context
) {
    // Thread-safe collections
    private val dictionaries = ConcurrentHashMap<String, Set<String>>()
    private val learnedWords = Collections.synchronizedSet(mutableSetOf<String>())

    // Background scope for heavy operations
    private val engineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    // Cache for frequently used suggestions
    private val suggestionCache = LruCache<String, List<AdvancedSuggestion>>(100)

    init {
        // Load dictionaries asynchronously on init
        engineScope.launch {
            loadDictionaries()
        }
    }

    /**
     * Load dictionaries for supported languages.
     * Tries to load from raw resources, falls back to basic list if failed.
     */
    private suspend fun loadDictionaries() = withContext(Dispatchers.IO) {
        try {
            // Load English dictionary
            val enDict = loadDictionaryFromRes(R.raw.en_dict)
            if (enDict.isNotEmpty()) {
                dictionaries["en"] = enDict
            } else {
                dictionaries["en"] = loadFallbackDictionary()
            }

            // Placeholder for other languages (files not guaranteed to exist yet)
            // dictionaries["es"] = loadDictionaryFromRes(R.raw.es_dict)

            Timber.d("✅ Loaded dictionaries for: ${dictionaries.keys}")
        } catch (e: Exception) {
            Timber.e(e, "Error loading dictionaries")
            dictionaries["en"] = loadFallbackDictionary()
        }
    }

    private fun loadDictionaryFromRes(resId: Int): Set<String> {
        return try {
            context.resources.openRawResource(resId).bufferedReader().use { reader ->
                reader.lineSequence()
                    .filter { it.isNotBlank() && it.length >= 2 }
                    .map { it.trim().lowercase() }
                    .toSet()
            }
        } catch (e: Exception) {
            Timber.w("Dictionary resource not found: $resId")
            emptySet()
        }
    }

    private fun loadFallbackDictionary(): Set<String> {
        return setOf(
            "the", "and", "you", "that", "was", "for", "are", "with", "his", "they",
            "have", "this", "will", "your", "from", "they", "know", "want", "been",
            "good", "much", "some", "time", "very", "when", "come", "here", "just",
            "like", "long", "make", "many", "over", "such", "take", "than", "them",
            "well", "were", "what", "would", "about", "could", "there", "think",
            "where", "being", "every", "first", "might", "never", "other", "right",
            "should", "their", "these", "those", "under", "while", "write", "after",
            "again", "before", "going", "great", "little", "still", "through",
            "world", "years", "people", "because", "without", "information", "computer",
            "keyboard", "mobile", "phone", "application", "software", "internet"
        )
    }

    /**
     * Get advanced suggestions with context awareness
     */
    suspend fun getAdvancedSuggestions(
        word: String,
        context: WordContext,
        language: Language
    ): List<AdvancedSuggestion> = withContext(Dispatchers.Default) {

        if (word.length < 2) return@withContext emptyList()

        // Check cache first
        val cacheKey = "${word}_${language.code}_${context.previousWord}"
        suggestionCache.get(cacheKey)?.let { return@withContext it }

        val suggestions = mutableListOf<AdvancedSuggestion>()

        try {
            val lowerWord = word.lowercase(Locale.getDefault())
            val languageCode = language.code.take(2).lowercase()
            val dictionary = getDictionaryForLanguage(languageCode)

            if (dictionary.isEmpty()) {
                return@withContext emptyList()
            }

            // 1. Exact Match
            if (dictionary.contains(lowerWord)) {
                // Word is correct, but we might want to suggest capitalization or next words
                // For now, just return empty list or valid confirmation?
                // Typically we still suggest corrections if it's a common typo that results in a valid word
                // but here we just accept it.
            } else {
                // 2. Spelling corrections
            // First check common typos for O(1) correction
            val commonTypo = processInput(lowerWord)
            if (commonTypo != lowerWord) {
                suggestions.add(
                    AdvancedSuggestion(
                        original = word,
                        suggestion = matchCase(commonTypo, word),
                        confidence = 1.0f,
                        type = CorrectionType.AUTO_CORRECT,
                        reasoning = "Common typo"
                    )
                )
            }

            // Then do expensive edit distance search
                val corrections = findOptimizedSpellingCorrections(lowerWord, dictionary)
                suggestions.addAll(corrections)
            }

            // 3. Capitalization
            if (context.isStartOfSentence && word.firstOrNull()?.isLowerCase() == true) {
                 suggestions.add(
                    AdvancedSuggestion(
                        original = word,
                        suggestion = word.replaceFirstChar { it.uppercase() },
                        confidence = 0.95f,
                        type = CorrectionType.CAPITALIZATION,
                        reasoning = "Start of sentence"
                    )
                )
            }

            val finalSuggestions = suggestions
                .sortedByDescending { it.confidence }
                .distinctBy { it.suggestion.lowercase() }
                .take(3)

            suggestionCache.put(cacheKey, finalSuggestions)
            finalSuggestions

        } catch (e: Exception) {
            Timber.e(e, "Error generating suggestions")
            emptyList()
        }
    }

    private fun getDictionaryForLanguage(languageCode: String): Set<String> {
        return (dictionaries[languageCode] ?: dictionaries["en"] ?: emptySet()) + learnedWords
    }

    private fun findOptimizedSpellingCorrections(
        word: String,
        dictionary: Set<String>
    ): List<AdvancedSuggestion> {
        val corrections = mutableListOf<AdvancedSuggestion>()
        val maxDistance = if (word.length <= 4) 1 else 2

        // Simple filtering for performance
        val candidates = dictionary.asSequence()
            .filter {
                kotlin.math.abs(it.length - word.length) <= maxDistance
            }
            .take(1000) // Limit search space

        for (dictWord in candidates) {
            val distance = levenshteinDistance(word, dictWord)
            if (distance <= maxDistance && distance > 0) {
                 val confidence = 1f - (distance.toFloat() / maxOf(word.length, dictWord.length))
                 corrections.add(
                    AdvancedSuggestion(
                        original = word,
                        suggestion = matchCase(dictWord, word),
                        confidence = confidence,
                        type = CorrectionType.SPELLING,
                        reasoning = "Edit distance: $distance"
                    )
                )
            }
        }
        return corrections
    }

    private fun levenshteinDistance(s1: String, s2: String): Int {
        val len1 = s1.length
        val len2 = s2.length
        val dist = Array(len1 + 1) { IntArray(len2 + 1) }

        for (i in 0..len1) dist[i][0] = i
        for (j in 0..len2) dist[0][j] = j

        for (i in 1..len1) {
            for (j in 1..len2) {
                val cost = if (s1[i - 1] == s2[j - 1]) 0 else 1
                dist[i][j] = minOf(
                    dist[i - 1][j] + 1,      // deletion
                    dist[i][j - 1] + 1,      // insertion
                    dist[i - 1][j - 1] + cost // substitution
                )
            }
        }
        return dist[len1][len2]
    }

    private fun matchCase(correction: String, original: String): String {
        return when {
            original.all { it.isUpperCase() } -> correction.uppercase()
            original.first().isUpperCase() -> correction.replaceFirstChar { it.uppercase() }
            else -> correction
        }
    }

    fun learnWord(word: String) {
        if (word.length >= 3 && word.all { it.isLetter() }) {
            learnedWords.add(word.lowercase())
            Timber.d("✅ Learned new word: $word")
        }
    }

    /**
     * Process input text with auto-correction
     * @param text The input text to process
     * @return Corrected text or original if no correction needed
     */
    fun processInput(text: String): String {
        if (text.isBlank()) return text
        
        return try {
            // Common typo replacements
            val corrections = mapOf(
                "teh" to "the",
                "adn" to "and",
                "taht" to "that",
                "thsi" to "this",
                "whcih" to "which",
                "recieve" to "receive",
                "seperate" to "separate",
                "definately" to "definitely",
                "occured" to "occurred",
                "untill" to "until"
            )
            
            corrections[text.lowercase()] ?: text
        } catch (e: Exception) {
            Timber.e(e, "Error processing input text")
            text // Return original on error
        }
    }

    /**
     * Clean up resources when engine is no longer needed
     */
    fun cleanup() {
        try {
            Timber.d("Cleaning up AdvancedAutocorrectEngine")
            engineScope.cancel()
            suggestionCache.evictAll()
            dictionaries.clear()
            learnedWords.clear()
        } catch (e: Exception) {
            Timber.e(e, "Error during cleanup")
        }
    }

    /**
     * Check if engine is ready for use
     */
    fun isReady(): Boolean {
        return dictionaries.isNotEmpty()
    }

    /**
     * Get statistics about loaded dictionaries
     */
    fun getStats(): Map<String, Int> {
        return try {
            mapOf(
                "languages" to dictionaries.size,
                "learned_words" to learnedWords.size,
                "cache_size" to suggestionCache.size()
            )
        } catch (e: Exception) {
            Timber.e(e, "Error getting stats")
            emptyMap()
        }
    }
}

// Data Classes
data class AdvancedSuggestion(
    val original: String,
    val suggestion: String,
    val confidence: Float,
    val type: CorrectionType,
    val reasoning: String = ""
)

data class WordContext(
    val previousWord: String = "",
    val nextWord: String = "",
    val sentencePosition: Int = 0,
    val isStartOfSentence: Boolean = false,
    val isAfterPunctuation: Boolean = false
)
