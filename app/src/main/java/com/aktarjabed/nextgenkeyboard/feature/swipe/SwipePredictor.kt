package com.aktarjabed.nextgenkeyboard.feature.swipe

import timber.log.Timber
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SwipePredictor @Inject constructor() {

    private val dictionary = setOf(
        "the", "and", "for", "are", "but", "not", "you", "all", "can", "her",
        "was", "one", "our", "out", "day", "get", "has", "him", "his", "how",
        "man", "new", "now", "old", "see", "two", "way", "who", "boy", "did",
        "its", "let", "put", "say", "she", "too", "use", "hello", "world", "test"
    )
    // ✅ Trie node for efficient prefix search
    private class TrieNode {
        val children = mutableMapOf<Char, TrieNode>()
        var isEndOfWord = false
        var frequency = 0
    }

    private val root = TrieNode()

    init {
        // Initialize with common words
        initializeDictionary()
    }

    private fun initializeDictionary() {
        try {
            // Common English words with frequency
            val commonWords = mapOf(
                "the" to 100, "and" to 95, "for" to 90, "are" to 85, "but" to 80,
                "not" to 75, "you" to 95, "all" to 70, "can" to 85, "her" to 65,
                "was" to 80, "one" to 75, "our" to 60, "out" to 70, "day" to 65,
                "get" to 75, "has" to 70, "him" to 60, "his" to 75, "how" to 80,
                "man" to 55, "new" to 70, "now" to 85, "old" to 60, "see" to 75,
                "two" to 65, "way" to 70, "who" to 75, "boy" to 50, "did" to 70,
                "its" to 65, "let" to 60, "put" to 55, "say" to 70, "she" to 80,
                "too" to 65, "use" to 60, "hello" to 70, "world" to 65, "good" to 75,
                "this" to 90, "that" to 85, "with" to 80, "have" to 85, "from" to 75,
                "they" to 80, "will" to 75, "been" to 70, "more" to 75, "when" to 70,
                "time" to 75, "very" to 65, "just" to 80, "know" to 85, "take" to 70,
                "make" to 75, "come" to 70, "look" to 65, "want" to 80, "give" to 70,
                "work" to 75, "feel" to 65, "think" to 85, "would" to 80, "could" to 75,
                "should" to 70, "about" to 80, "after" to 75, "before" to 70, "because" to 75
            )

            commonWords.forEach { (word, frequency) ->
                insertWord(word, frequency)
            }

            Timber.d("✅ Dictionary initialized with ${commonWords.size} words")
        } catch (e: Exception) {
            Timber.e(e, "Error initializing dictionary")
        }
    }

    private fun insertWord(word: String, frequency: Int = 1) {
        var node = root
        word.lowercase(Locale.getDefault()).forEach { char ->
            node = node.children.getOrPut(char) { TrieNode() }
        }
        node.isEndOfWord = true
        node.frequency = frequency
    }

    fun predictWord(keySequence: String): String {
        if (keySequence.length < 2) return ""

        val matches = dictionary.filter { it.startsWith(keySequence.lowercase()) }
        return matches.firstOrNull() ?: keySequence
        try {
            val suggestions = getSuggestions(keySequence, limit = 1)
            return suggestions.firstOrNull() ?: keySequence
        } catch (e: Exception) {
            Timber.e(e, "Error predicting word")
            return keySequence
        }
    }

    fun getSuggestions(keySequence: String, limit: Int = 3): List<String> {
        if (keySequence.length < 2) return emptyList()

        return dictionary
            .filter { it.startsWith(keySequence.lowercase()) }
            .take(limit)
        try {
            val prefix = keySequence.lowercase(Locale.getDefault())
            val suggestions = mutableListOf<Pair<String, Int>>() // word to frequency

            // Find prefix node
            var node = root
            for (char in prefix) {
                node = node.children[char] ?: return emptyList()
            }

            // DFS to collect all words with this prefix
            collectWords(node, prefix, suggestions)

            // Sort by frequency (descending) and take top N
            return suggestions
                .sortedByDescending { it.second }
                .take(limit)
                .map { it.first }
        } catch (e: Exception) {
            Timber.e(e, "Error getting suggestions")
            return emptyList()
        }
    }

    private fun collectWords(
        node: TrieNode,
        currentWord: String,
        results: MutableList<Pair<String, Int>>
    ) {
        if (node.isEndOfWord) {
            results.add(currentWord to node.frequency)
        }

        node.children.forEach { (char, childNode) ->
            collectWords(childNode, currentWord + char, results)
        }
    }

    // ✅ Add word dynamically (user learning)
    fun learnWord(word: String) {
        if (word.length >= 3) {
            try {
                insertWord(word, frequency = 1)
                Timber.d("Learned new word: $word")
            } catch (e: Exception) {
                Timber.e(e, "Error learning word")
            }
        }
    }
}