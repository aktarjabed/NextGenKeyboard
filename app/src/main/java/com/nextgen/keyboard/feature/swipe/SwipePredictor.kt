package com.nextgen.keyboard.feature.swipe

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

    fun predictWord(keySequence: String): String {
        if (keySequence.length < 2) return ""

        val matches = dictionary.filter { it.startsWith(keySequence.lowercase()) }
        return matches.firstOrNull() ?: keySequence
    }

    fun getSuggestions(keySequence: String, limit: Int = 3): List<String> {
        if (keySequence.length < 2) return emptyList()

        return dictionary
            .filter { it.startsWith(keySequence.lowercase()) }
            .take(limit)
    }
}