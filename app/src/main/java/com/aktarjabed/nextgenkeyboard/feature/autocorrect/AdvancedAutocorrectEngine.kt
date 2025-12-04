package com.aktarjabed.nextgenkeyboard.feature.autocorrect
import com.aktarjabed.nextgenkeyboard.state.CorrectionType

import android.content.Context
import androidx.collection.LruCache
import com.aktarjabed.nextgenkeyboard.R
import com.aktarjabed.nextgenkeyboard.data.model.Language
import com.aktarjabed.nextgenkeyboard.data.model.Language
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.Collections
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdvancedAutocorrectEngine @Inject constructor(
    @ApplicationContext private val context: Context
) {
    // Use thread-safe collections
    private val dictionaries = ConcurrentHashMap<String, Set<String>>()
    private val learnedWords = Collections.synchronizedSet(mutableSetOf<String>())
    private val bigramFrequency = ConcurrentHashMap<Pair<String, String>, Int>()

    // Background scope for heavy operations
    private val engineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    // Cache for frequently used suggestions
    private val suggestionCache = LruCache<String, List<AdvancedSuggestion>>(100)

    init {
        // Load dictionaries asynchronously
        engineScope.launch {
            loadDictionaries()
        }
    }

    /**
     * Load dictionaries for multiple languages
     */
    private fun loadDictionaries() {
        // Load English dictionary
        dictionaries["en"] = loadEnglishDictionary()
        dictionaries["es"] = loadSpanishDictionary()
        dictionaries["de"] = loadGermanDictionary()
    private suspend fun loadDictionaries() = withContext(Dispatchers.IO) {
        try {
            // Load English dictionary
            dictionaries["en"] = loadDictionaryFromRes(R.raw.en_dict)

            // Load additional languages
            // dictionaries["es"] = loadDictionaryFromRes(R.raw.es_dict)
            // dictionaries["fr"] = loadDictionaryFromRes(R.raw.fr_dict)

            Timber.d("✅ Loaded ${dictionaries.size} language dictionaries")
        } catch (e: Exception) {
            Timber.e(e, "Error loading dictionaries")
            // Fallback to embedded basic dictionary
            loadFallbackDictionary()
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
            Timber.w(e, "Dictionary resource not found: $resId")
            emptySet()
        }
    }

    private fun loadFallbackDictionary(): Set<String> {
        // Basic English word list
        val basicWords = setOf(
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
        dictionaries["en"] = basicWords
        return basicWords
    }

    private fun loadSpanishDictionary(): Set<String> {
        return setOf(
            "el", "la", "de", "y", "a", "en", "que", "es", "por", "un",
            "con", "no", "una", "su", "para", "como", "más", "pero", "las", "le",
            "ser", "estar", "haber", "tener", "hacer", "decir", "ir", "ver", "dar",
            "saber", "querer", "llegar", "pasar", "deber", "poner", "parecer", "quedar",
            "creer", "hablar", "llevar", "dejar", "seguir", "encontrar", "llamar", "venir",
            "hola", "gracias", "adiós", "por favor", "lo siento", "buenos días", "buenas tardes", "buenas noches"
        )
    }

    private fun loadGermanDictionary(): Set<String> {
        return setOf(
            "der", "die", "das", "und", "in", "den", "von", "zu", "mit", "sich",
            "auf", "für", "ist", "im", "dass", "nicht", "ein", "eine", "als", "auch",
            "sein", "haben", "werden", "können", "müssen", "sagen", "geben", "machen", "kommen",
            "sollen", "wollen", "gehen", "wissen", "sehen", "lassen", "stehen", "finden",
            "hallo", "danke", "tschüss", "bitte", "entschuldigung", "guten morgen", "guten tag", "guten abend"
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
            val languageCode = language.code.substring(0, 2)
            val dictionary = getDictionaryForLanguage(languageCode)

            if (dictionary.isEmpty()) {
                Timber.w("No dictionary available for language: $languageCode")
                return@withContext emptyList()
            }

            // 1. Quick exact match check
            if (dictionary.contains(lowerWord)) {
                // Word exists, but still provide alternatives for context
                addContextualSuggestions(suggestions, lowerWord, context, dictionary)
            } else {
                // 2. Spelling corrections (optimized)
                val spellingCorrections = findOptimizedSpellingCorrections(lowerWord, dictionary)
                suggestions.addAll(spellingCorrections)

                // 3. Context-aware suggestions
                if (context.previousWord.isNotEmpty()) {
                    val contextSuggestions = getContextSuggestions(lowerWord, context, dictionary)
                    suggestions.addAll(contextSuggestions)
                }

                // 4. Common typos (fast lookup)
                val typoCorrections = checkCommonTypos(word)
                suggestions.addAll(typoCorrections)
            }

            // 5. Capitalization fixes
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
                .take(3) // Limit to top 3 for performance

            // Cache the result
            suggestionCache.put(cacheKey, finalSuggestions)

            Timber.d("Generated ${finalSuggestions.size} suggestions for '$word'")
            finalSuggestions

        } catch (e: Exception) {
            Timber.e(e, "Error generating suggestions for '$word'")
            emptyList()
        }
    }

    private fun getDictionaryForLanguage(languageCode: String): Set<String> {
        return (dictionaries[languageCode] ?: dictionaries["en"] ?: emptySet())
            .plus(learnedWords)
    }

    private fun findOptimizedSpellingCorrections(
        word: String,
        dictionary: Set<String>
    ): List<AdvancedSuggestion> {
        val corrections = mutableListOf<AdvancedSuggestion>()
        val maxDistance = if (word.length <= 4) 1 else 2

        // Use parallel processing for large dictionaries
        val candidateWords = dictionary
            .filter { dictWord ->
                // Quick pre-filter by length
                kotlin.math.abs(dictWord.length - word.length) <= maxDistance &&
                // Quick pre-filter by first character
                (dictWord.firstOrNull()?.equals(word.firstOrNull(), true) == true ||
                 maxDistance >= 2)
            }
            .take(500) // Limit candidates for performance

        candidateWords.forEach { dictWord ->
            val distance = levenshteinDistance(word, dictWord)
            if (distance <= maxDistance && distance > 0) {
                val confidence = 1f - (distance.toFloat() / maxOf(word.length, dictWord.length))
                corrections.add(
                    AdvancedSuggestion(
                        original = word,
                        suggestion = matchCase(dictWord, word),
                        confidence = confidence * 0.8f,
                        type = CorrectionType.SPELLING,
                        reasoning = "Edit distance: $distance"
                    )
                )
            }
        }

        return corrections.sortedByDescending { it.confidence }.take(3)
    }

    // Add resource cleanup
    fun cleanup() {
        engineScope.cancel()
        suggestionCache.evictAll()
        Timber.d("Autocorrect engine cleaned up")
    }

    private fun addContextualSuggestions(
        suggestions: MutableList<AdvancedSuggestion>,
        lowerWord: String,
        context: WordContext,
        dictionary: Set<String>
    ) {
        // Implementation for contextual suggestions
    }

    private fun getContextSuggestions(
        lowerWord: String,
        context: WordContext,
        dictionary: Set<String>
    ): List<AdvancedSuggestion> {
        // Implementation for context suggestions
        return emptyList()
    }

    private fun checkCommonTypos(word: String): List<AdvancedSuggestion> {
        // Implementation for common typos
        return emptyList()
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

    fun processInput(text: String): String {
        // Dummy implementation
        return text
    }
}

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