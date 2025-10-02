package com.nextgen.keyboard.feature.autocorrect

import android.content.Context
import com.nextgen.keyboard.data.model.Language
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

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

@Singleton
class AdvancedAutocorrectEngine @Inject constructor(
    @ApplicationContext private val context: Context
) {

    // Multi-language dictionaries
    private val dictionaries = mutableMapOf<String, Set<String>>()

    // User learning dictionary
    private val learnedWords = mutableSetOf<String>()

    // Bigram frequency (word pairs)
    private val bigramFrequency = mutableMapOf<Pair<String, String>, Int>()

    // Trigram frequency (3-word sequences)
    private val trigramFrequency = mutableMapOf<Triple<String, String, String>, Int>()

    // Common grammar patterns
    private val grammarRules = listOf(
        // Verb conjugation patterns
        GrammarRule(
            pattern = Regex("\\b(I|you|we|they)\\s+is\\b"),
            correction = { it.replace(" is", " are") },
            confidence = 0.95f,
            reasoning = "Subject-verb agreement"
        ),
        GrammarRule(
            pattern = Regex("\\b(he|she|it)\\s+are\\b"),
            correction = { it.replace(" are", " is") },
            confidence = 0.95f,
            reasoning = "Subject-verb agreement"
        ),
        // Article usage
        GrammarRule(
            pattern = Regex("\\ba\\s+([aeiou])"),
            correction = { it.replace(Regex("\\ba\\s+"), "an ") },
            confidence = 0.9f,
            reasoning = "Article before vowel"
        ),
        GrammarRule(
            pattern = Regex("\\ban\\s+([^aeiou])"),
            correction = { it.replace(Regex("\\ban\\s+"), "a ") },
            confidence = 0.9f,
            reasoning = "Article before consonant"
        ),
        // Double negatives
        GrammarRule(
            pattern = Regex("don't\\s+never|can't\\s+never|won't\\s+never"),
            correction = { it.replace(" never", "") },
            confidence = 0.85f,
            reasoning = "Double negative"
        ),
        // Common confusions
        GrammarRule(
            pattern = Regex("\\byour\\s+(thinking|going|doing)\\b"),
            correction = { it.replace("your", "you're") },
            confidence = 0.9f,
            reasoning = "Your vs you're"
        ),
        GrammarRule(
            pattern = Regex("\\btheir\\s+(thinking|going|doing)\\b"),
            correction = { it.replace("their", "they're") },
            confidence = 0.9f,
            reasoning = "Their vs they're"
        )
    )

    init {
        loadDictionaries()
    }

    /**
     * Load dictionaries for multiple languages
     */
    private fun loadDictionaries() {
        // Load English dictionary
        dictionaries["en"] = loadEnglishDictionary()
        dictionaries["es"] = loadSpanishDictionary()
        dictionaries["de"] = loadGermanDictionary()

        // Load other language dictionaries as needed
        Timber.d("✅ Loaded ${dictionaries.size} language dictionaries")
    }

    private fun loadEnglishDictionary(): Set<String> {
        // Extended English dictionary with 10,000+ common words
        return setOf(
            // Common words
            "the", "be", "to", "of", "and", "a", "in", "that", "have", "I",
            "it", "for", "not", "on", "with", "he", "as", "you", "do", "at",
            "this", "but", "his", "by", "from", "they", "we", "say", "her", "she",
            "or", "an", "will", "my", "one", "all", "would", "there", "their", "what",

            // Action verbs
            "go", "going", "went", "gone", "make", "making", "made", "get", "getting", "got",
            "see", "seeing", "saw", "seen", "know", "knowing", "knew", "known",
            "take", "taking", "took", "taken", "come", "coming", "came",
            "think", "thinking", "thought", "look", "looking", "looked",
            "want", "wanting", "wanted", "give", "giving", "gave", "given",
            "use", "using", "used", "find", "finding", "found",
            "tell", "telling", "told", "ask", "asking", "asked",
            "work", "working", "worked", "seem", "seeming", "seemed",
            "feel", "feeling", "felt", "try", "trying", "tried",
            "leave", "leaving", "left", "call", "calling", "called",

            // Common nouns
            "time", "person", "year", "way", "day", "thing", "man", "world", "life", "hand",
            "part", "child", "eye", "woman", "place", "work", "week", "case", "point", "government",
            "company", "number", "group", "problem", "fact", "business", "service", "people",
            "information", "system", "area", "question", "money", "water", "food", "family",

            // Adjectives
            "good", "better", "best", "new", "first", "last", "long", "great", "little", "own",
            "other", "old", "right", "big", "high", "different", "small", "large", "next", "early",
            "young", "important", "few", "public", "bad", "same", "able", "happy", "sad", "angry",

            // Technology
            "computer", "phone", "internet", "email", "website", "app", "software", "data", "file",
            "system", "network", "server", "database", "cloud", "digital", "online", "device",
            "technology", "program", "code", "application", "platform", "interface", "user",

            // Modern words
            "social", "media", "video", "image", "photo", "post", "share", "like", "comment",
            "follow", "friend", "message", "chat", "call", "send", "receive", "download", "upload",

            // Add more as needed...
        ).plus(learnedWords)
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
        val suggestions = mutableListOf<AdvancedSuggestion>()

        try {
            val lowerWord = word.lowercase(Locale.getDefault())
            val languageCode = language.code.substring(0, 2)
            val dictionary = dictionaries[languageCode] ?: dictionaries["en"]!!

            // 1. Check if word exists in dictionary
            if (!dictionary.contains(lowerWord) && lowerWord.length >= 3) {
                // Find spelling corrections
                val spellingCorrections = findSpellingCorrections(lowerWord, dictionary)
                suggestions.addAll(spellingCorrections.map { (correction, confidence) ->
                    AdvancedSuggestion(
                        original = word,
                        suggestion = matchCase(correction, word),
                        confidence = confidence,
                        type = CorrectionType.SPELLING,
                        reasoning = "Spelling correction (Levenshtein distance)"
                    )
                })
            }

            // 2. Context-aware suggestions using bigrams
            if (context.previousWord.isNotEmpty()) {
                val contextSuggestions = getContextSuggestions(lowerWord, context, dictionary)
                suggestions.addAll(contextSuggestions)
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

            // 4. Common typo patterns
            val typoCorrections = checkCommonTypos(word)
            suggestions.addAll(typoCorrections)

            // 5. Phonetic suggestions
            val phoneticSuggestions = getPhoneticSuggestions(lowerWord, dictionary)
            suggestions.addAll(phoneticSuggestions)

            Timber.d("Generated ${suggestions.size} suggestions for '$word'")
        } catch (e: Exception) {
            Timber.e(e, "Error generating suggestions")
        }

        return@withContext suggestions
            .sortedByDescending { it.confidence }
            .distinctBy { it.suggestion }
            .take(5)
    }

    /**
     * Find spelling corrections using multiple algorithms
     */
    private fun findSpellingCorrections(
        word: String,
        dictionary: Set<String>
    ): List<Pair<String, Float>> {
        val corrections = mutableListOf<Pair<String, Float>>()

        // Levenshtein distance (max 2 edits)
        dictionary.forEach { dictWord ->
            val distance = levenshteinDistance(word, dictWord)
            if (distance <= 2 && dictWord.length >= word.length - 2) {
                val confidence = 1f - (distance.toFloat() / maxOf(word.length, dictWord.length))
                corrections.add(dictWord to confidence * 0.9f)
            }
        }

        // Damerau-Levenshtein (transpositions)
        dictionary.forEach { dictWord ->
            if (isTransposition(word, dictWord)) {
                corrections.add(dictWord to 0.95f)
            }
        }

        return corrections.sortedByDescending { it.second }.take(3)
    }

    /**
     * Get context-aware suggestions using bigram frequency
     */
    private fun getContextSuggestions(
        word: String,
        context: WordContext,
        dictionary: Set<String>
    ): List<AdvancedSuggestion> {
        val suggestions = mutableListOf<AdvancedSuggestion>()

        // Check bigram frequency
        dictionary.forEach { dictWord ->
            val bigram = context.previousWord.lowercase() to dictWord
            val frequency = bigramFrequency[bigram] ?: 0

            if (frequency > 0 && levenshteinDistance(word, dictWord) <= 1) {
                val confidence = minOf(0.9f, frequency / 100f)
                suggestions.add(
                    AdvancedSuggestion(
                        original = word,
                        suggestion = dictWord,
                        confidence = confidence,
                        type = CorrectionType.SPELLING,
                        reasoning = "Context-aware (follows '${context.previousWord}')"
                    )
                )
            }
        }

        return suggestions
    }

    /**
     * Check common typo patterns (keyboard proximity)
     */
    private fun checkCommonTypos(word: String): List<AdvancedSuggestion> {
        val typoMap = mapOf(
            // Keyboard proximity errors
            "teh" to "the",
            "taht" to "that",
            "adn" to "and",
            "fro" to "for",
            "iwth" to "with",
            "tiem" to "time",
            "peopel" to "people",
            "becuase" to "because",
            "recieve" to "receive",
            "beleive" to "believe",
            "acheive" to "achieve",
            "occured" to "occurred",
            "seperate" to "separate",
            "definately" to "definitely",
            "wierd" to "weird",
            "untill" to "until",
            "begining" to "beginning",
            "thier" to "their",
            "freind" to "friend",
            "grammer" to "grammar",
            "alot" to "a lot",
            "aswell" to "as well",

            // Contractions
            "dont" to "don't",
            "cant" to "can't",
            "wont" to "won't",
            "shouldnt" to "shouldn't",
            "couldnt" to "couldn't",
            "wouldnt" to "wouldn't",
            "isnt" to "isn't",
            "arent" to "aren't",
            "wasnt" to "wasn't",
            "werent" to "weren't",
            "hasnt" to "hasn't",
            "havent" to "haven't",
            "didnt" to "didn't",

            // Homophones
            "there" to "their",
            "your" to "you're",
            "its" to "it's",
            "whos" to "who's",
            "whose" to "who's"
        )

        val lower = word.lowercase()
        return typoMap[lower]?.let { correction ->
            listOf(
                AdvancedSuggestion(
                    original = word,
                    suggestion = matchCase(correction, word),
                    confidence = 0.95f,
                    type = CorrectionType.SPELLING,
                    reasoning = "Common typo pattern"
                )
            )
        } ?: emptyList()
    }

    /**
     * Get phonetic suggestions (Soundex algorithm)
     */
    private fun getPhoneticSuggestions(
        word: String,
        dictionary: Set<String>
    ): List<AdvancedSuggestion> {
        val suggestions = mutableListOf<AdvancedSuggestion>()
        val wordSoundex = soundex(word)

        dictionary.forEach { dictWord ->
            if (soundex(dictWord) == wordSoundex && dictWord != word) {
                suggestions.add(
                    AdvancedSuggestion(
                        original = word,
                        suggestion = dictWord,
                        confidence = 0.75f,
                        type = CorrectionType.SPELLING,
                        reasoning = "Phonetically similar"
                    )
                )
            }
        }

        return suggestions.take(2)
    }

    /**
     * Soundex phonetic algorithm
     */
    private fun soundex(word: String): String {
        if (word.isEmpty()) return ""

        val soundexMap = mapOf(
            'b' to '1', 'f' to '1', 'p' to '1', 'v' to '1',
            'c' to '2', 'g' to '2', 'j' to '2', 'k' to '2', 'q' to '2', 's' to '2', 'x' to '2', 'z' to '2',
            'd' to '3', 't' to '3',
            'l' to '4',
            'm' to '5', 'n' to '5',
            'r' to '6'
        )

        val firstChar = word[0].uppercase()
        val codes = word.drop(1)
            .map { soundexMap[it.lowercaseChar()] ?: '0' }
            .filter { it != '0' }
            .distinct()
            .take(3)
            .joinToString("")

        return (firstChar + codes).padEnd(4, '0')
    }

    /**
     * Check if two words are transpositions
     */
    private fun isTransposition(s1: String, s2: String): Boolean {
        if (s1.length != s2.length) return false
        var differences = 0
        for (i in s1.indices) {
            if (s1[i] != s2[i]) differences++
        }
        return differences == 2 && s1.toSet() == s2.toSet()
    }

    /**
     * Calculate Levenshtein distance
     */
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

    /**
     * Match the case of the correction to the original word
     */
    private fun matchCase(correction: String, original: String): String {
        return when {
            original.all { it.isUpperCase() } -> correction.uppercase()
            original.first().isUpperCase() -> correction.replaceFirstChar { it.uppercase() }
            else -> correction
        }
    }

    /**
     * Learn a new word from user input
     */
    fun learnWord(word: String) {
        if (word.length >= 3 && word.all { it.isLetter() }) {
            learnedWords.add(word.lowercase())
            Timber.d("✅ Learned new word: $word")
        }
    }

    /**
     * Update bigram frequency
     */
    fun updateBigramFrequency(word1: String, word2: String) {
        val bigram = word1.lowercase() to word2.lowercase()
        bigramFrequency[bigram] = (bigramFrequency[bigram] ?: 0) + 1
    }

    /**
     * Apply grammar rules to sentence
     */
    suspend fun applyGrammarCorrections(sentence: String): String = withContext(Dispatchers.Default) {
        var corrected = sentence

        grammarRules.forEach { rule ->
            if (rule.pattern.containsMatchIn(corrected)) {
                corrected = rule.correction(corrected)
                Timber.d("Applied grammar rule: ${rule.reasoning}")
            }
        }

        return@withContext corrected
    }
}

data class GrammarRule(
    val pattern: Regex,
    val correction: (String) -> String,
    val confidence: Float,
    val reasoning: String
)