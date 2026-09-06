package com.aktarjabed.nxtgenkeyboard.suggestion

import android.content.Context
import java.io.BufferedReader
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.abs

class SuggestionEngine(private val context: Context) {

    private val dictionaries = ConcurrentHashMap<String, MutableSet<String>>()
    private val loaded = ConcurrentHashMap.newKeySet<String>()

    fun loadAll() {
        listOf("en", "hi", "bn").forEach(::load)
    }

    @Synchronized
    fun load(lang: String) {
        if (loaded.contains(lang)) return
        val words = ConcurrentHashMap.newKeySet<String>()
        try {
            context.assets.open("dict/$lang.txt").use { stream ->
                BufferedReader(stream.reader(Charsets.UTF_8)).useLines { lines ->
                    lines.map { it.trim().lowercase(java.util.Locale.ROOT) }
                        .filter { it.isNotEmpty() }
                        .forEach(words::add)
                }
            }
            loaded.add(lang)
        } catch (e: Exception) {
            // Empty dictionary is a valid degraded state.
            android.util.Log.e("NxtGenIME", "Failed to load dictionary asset for lang: $lang", e)
        }
        dictionaries.getOrPut(lang) { ConcurrentHashMap.newKeySet() }.addAll(words)
    }

    fun isLoaded(lang: String): Boolean = loaded.contains(lang)

    fun contains(lang: String, word: String): Boolean =
        dictionaries[lang]?.contains(word.lowercase(java.util.Locale.ROOT)) == true

    fun addUserWord(lang: String, word: String) {
        val value = word.trim().lowercase(java.util.Locale.ROOT)
        if (value.isNotEmpty()) {
            dictionaries.getOrPut(lang) { ConcurrentHashMap.newKeySet() }.add(value)
        }
    }

    fun addUserWords(lang: String, words: List<String>) {
        val set = dictionaries.getOrPut(lang) { ConcurrentHashMap.newKeySet() }
        words.map { it.trim().lowercase(java.util.Locale.ROOT) }
            .filter { it.isNotEmpty() }
            .forEach(set::add)
    }

    fun suggest(lang: String, word: String, max: Int, maxDistance: Int = 2): List<String> {
        val dict = dictionaries[lang] ?: return emptyList()
        val target = word.lowercase(java.util.Locale.ROOT)
        if (dict.isEmpty() || target.isEmpty() || max <= 0 || maxDistance < 0) return emptyList()

        return dict.asSequence()
            .filter { abs(it.length - target.length) <= maxDistance }
            .map { it to boundedDamerauDistance(target, it, maxDistance) }
            .filter { it.second in 0..maxDistance }
            .sortedWith(compareBy<Pair<String, Int>> { it.second }.thenBy { it.first })
            .take(max)
            .map { it.first }
            .toList()
    }

    /**
     * Bounded Damerau-Levenshtein (Optimal String Alignment):
     * substitutions, insertions, deletions cost 1; adjacent transpositions cost 1
     * ("teh" -> "the" = 1, which plain Levenshtein scores as 2).
     * Returns limit + 1 as soon as distance provably exceeds [limit].
     */
    private fun boundedDamerauDistance(a: String, b: String, limit: Int): Int {
        if (a == b) return 0
        if (abs(a.length - b.length) > limit) return limit + 1

        val la = a.length
        val lb = b.length
        var prev2: IntArray? = null
        var prev = IntArray(lb + 1) { it }
        var cur = IntArray(lb + 1)

        for (i in 1..la) {
            cur[0] = i
            var rowMin = cur[0]
            for (j in 1..lb) {
                val cost = if (a[i - 1] == b[j - 1]) 0 else 1
                var v = minOf(cur[j - 1] + 1, prev[j] + 1, prev[j - 1] + cost)
                val p2 = prev2
                if (i > 1 && j > 1 && p2 != null &&
                    a[i - 1] == b[j - 2] && a[i - 2] == b[j - 1]
                ) {
                    v = minOf(v, p2[j - 2] + 1)
                }
                cur[j] = v
                if (v < rowMin) rowMin = v
            }
            if (rowMin > limit) return limit + 1
            prev2 = prev
            prev = cur
            cur = IntArray(lb + 1)
        }
        return prev[lb]
    }
}