package com.aktarjabed.nextgenkeyboard.feature.swipe

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import timber.log.Timber
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SwipePathProcessor @Inject constructor() {

    // Thread-safe replacement for mutableMapOf
    private val keyPositions = ConcurrentHashMap<String, Rect>()

    fun registerKeyPosition(key: String, rect: Rect) {
        if (key.isBlank()) {
            Timber.w("Key must not be blank")
            return
        }
        // Compose Rect uses properties width/height
        if (rect.width <= 0 || rect.height <= 0) {
            Timber.w("Rect must be valid size")
            return
        }
        keyPositions[key] = rect
    }

    fun processPathToKeySequence(path: List<Offset>): String {
        // Gap #2: Input validation
        val validatedPath = validateAndFilterPath(path)
        if (validatedPath.size < 3) {
            Timber.d("Path too short or invalid: ${path.size} points")
            return ""
        }

        val filteredPath = filterByVelocity(validatedPath)
        if (filteredPath.isEmpty()) return ""

        return filteredPath
            .asSequence()
            .mapNotNull { offset -> findIntersectingKey(offset) }
            .distinctConsecutive()
            .joinToString("")
    }

    private fun validateAndFilterPath(path: List<Offset>): List<Offset> {
        return path.filter { offset ->
            offset.isValid() && // Reject NaN/Infinite (Gap #2)
            offset.x >= 0f && offset.y >= 0f && // Positive coords
            offset.x < 2000f && offset.y < 2000f // Reasonable screen bounds (prevents outliers)
        }
    }

    private fun filterByVelocity(path: List<Offset>): List<Offset> {
        if (path.size < 3) return path

        return path.windowed(3, partialWindows = true).mapNotNull { window ->
            when (window.size) {
                3 -> {
                    val (p1, p2, p3) = window
                    val velocity = (p2 - p1).getDistance() + (p3 - p2).getDistance()
                    if (velocity > 5f) p2 else null
                }
                2 -> window.last()
                else -> null
            }
        }
    }

    // Public method to find key at a specific point (used for taps)
    fun findKeyAt(offset: Offset): String? {
        return findIntersectingKey(offset)
    }

    private fun findIntersectingKey(offset: Offset): String? {
        return keyPositions.entries
            .asSequence()
            .firstOrNull { (_, rect) ->
                rect.contains(offset)
            }?.key
    }

    private fun <T> Sequence<T>.distinctConsecutive(): List<T> =
        fold(mutableListOf<T>()) { acc, item ->
            if (acc.lastOrNull() != item) acc.add(item)
            acc
        }

    // Clear positions on keyboard layout change
    fun clearKeyPositions() = keyPositions.clear()
}
