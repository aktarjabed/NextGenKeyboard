package com.aktarjabed.nextgenkeyboard.feature.swipe

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SwipePathProcessor @Inject constructor() {

    private val keyPositions = mutableMapOf<String, Rect>()

    fun registerKeyPosition(key: String, rect: Rect) {
        keyPositions[key] = rect
    }

    fun findKeyAt(offset: Offset): String? {
        return keyPositions.entries.find { (_, rect) -> rect.contains(offset) }?.key
    }

    fun processPathToKeySequence(path: List<Offset>): String {
        // Filter out very short paths (accidental touches)
        if (path.size < 3) return ""

        val filteredPath = filterByVelocity(path)

        // Map path points to keys
        return filteredPath
            .mapNotNull { offset ->
                keyPositions.entries.find { (_, rect) -> rect.contains(offset) }?.key
            }
            .distinctConsecutive() // Remove consecutive duplicates
            .joinToString("")
    }

    private fun filterByVelocity(path: List<Offset>): List<Offset> {
        if (path.size < 3) return path

        // Keep the first point (start of swipe)
        val first = path.first()
        // Keep the last point (end of swipe)
        val last = path.last()

        // Filter the middle points
        val middle = path.windowed(3, partialWindows = true).mapNotNull { window ->
            when (window.size) {
                3 -> {
                    val (p1, p2, p3) = window
                    val velocity = (p2 - p1).getDistance() + (p3 - p2).getDistance()
                    // Threshold of 5.0f is arbitrary but reasonable for pixel distance
                    if (velocity > 5f) p2 else null
                }
                2 -> window.last() // Keep last point if window is incomplete
                else -> null
            }
        }

        // Reconstruct the path ensuring start and end are present
        val result = mutableListOf<Offset>()
        result.add(first)
        result.addAll(middle)
        if (result.last() != last) {
            result.add(last)
        }

        return result
    }

    private fun <T> List<T>.distinctConsecutive(): List<T> =
        fold(mutableListOf<T>()) { acc, item ->
            if (acc.isEmpty() || acc.last() != item) {
                acc.add(item)
            }
            acc
        }
}
