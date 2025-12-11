package com.aktarjabed.nextgenkeyboard.feature.swipe

import android.content.Context
import com.aktarjabed.nextgenkeyboard.R
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SwipePredictor @Inject constructor(
    @ApplicationContext private val context: Context
) {

    // ✅ Trie node for efficient prefix search
    private class TrieNode {
        val children = mutableMapOf<Char, TrieNode>()
        var isEndOfWord = false
        var frequency = 0
    }

    private val root = TrieNode()
    private val lock = Any()
    @Volatile private var isDictionaryLoaded = false

    init {
        // Initialize with common words asynchronously
        CoroutineScope(Dispatchers.IO).launch {
            initializeDictionary()
        }
    }

    private fun initializeDictionary() {
        try {
            val inputStream = context.resources.openRawResource(R.raw.en_dict)
            val reader = BufferedReader(InputStreamReader(inputStream))

            var word = reader.readLine()
            var count = 0
            while (word != null) {
                // Assign a default frequency based on position (assuming list is sorted or just basic)
                // Or just use 1. If we have a frequency list, we'd parse it.
                // The provided en_dict.txt is just a list of words.
                insertWord(word.trim(), frequency = 100) // Default high freq for dictionary words
                word = reader.readLine()
                count++
            }
            reader.close()

            isDictionaryLoaded = true
            Timber.d("✅ Dictionary initialized with $count words from file")
        } catch (e: Exception) {
            Timber.e(e, "Error initializing dictionary from file")
            // Do not reset isDictionaryLoaded to false here, as learnWord might have already enabled it.
        }
    }

    private fun insertWord(word: String, frequency: Int = 1) {
        if (word.isBlank()) return
        synchronized(lock) {
            var node = root
            word.lowercase(Locale.getDefault()).forEach { char ->
                node = node.children.getOrPut(char) { TrieNode() }
            }
            node.isEndOfWord = true
            // Increment frequency if already exists, or set it
            node.frequency = maxOf(node.frequency, frequency)
        }
    }

    fun predictWord(keySequence: String): String {
        // If not loaded yet, just return sequence
        if (!isDictionaryLoaded) {
            // Timber.w("Dictionary not loaded, skipping prediction")
            return keySequence
        }
        if (keySequence.length < 2) return ""
        try {
            val suggestions = getSuggestions(keySequence, limit = 1)
            return suggestions.firstOrNull() ?: keySequence
        } catch (e: Exception) {
            Timber.e(e, "Error predicting word")
            return keySequence
        }
    }

    fun getSuggestions(keySequence: String, limit: Int = 3): List<String> {
        if (!isDictionaryLoaded) return emptyList()
        if (keySequence.length < 2) return emptyList()

        try {
            val prefix = keySequence.lowercase(Locale.getDefault())
            val suggestions = mutableListOf<Pair<String, Int>>() // word to frequency

            synchronized(lock) {
                // Find prefix node
                var node = root
                for (char in prefix) {
                    node = node.children[char] ?: return emptyList()
                }

                // DFS to collect all words with this prefix
                collectWords(node, prefix, suggestions)
            }

            // Sort by frequency (descending) and length (shorter first prefers exact matches)
            return suggestions
                .sortedWith(compareByDescending<Pair<String, Int>> { it.second }.thenBy { it.first.length })
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
                // Boost frequency for learned words
                insertWord(word, frequency = 200)
                // Ensure predictor is active if we have learned words, even if initial load failed or is pending
                isDictionaryLoaded = true
                Timber.d("Learned new word: $word")
            } catch (e: Exception) {
                Timber.e(e, "Error learning word")
            }
        }
    }
}
