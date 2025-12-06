package com.aktarjabed.nextgenkeyboard.feature.swipe

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import timber.log.Timber
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SwipePathProcessor @Inject constructor() {

    companion object {
        private const val MAX_PATH_POINTS = 500
        private const val MIN_PATH_LENGTH = 3
        private const val TAG = "SwipePathProcessor"
    }

    // Use ConcurrentHashMap for thread safety
    private val keyPositions = ConcurrentHashMap<String, Rect>()

    fun registerKeyPosition(key: String, rect: Rect) {
        // Validate bounds before storing
        if (rect.width <= 0 || rect.height <= 0) {
            Timber.tag(TAG).w("Invalid key bounds for '$key': width=${rect.width}, height=${rect.height}")
            return
        }

        keyPositions[key] = rect
    }

    fun clearKeys() {
        keyPositions.clear()
    }

    fun findKeyAt(offset: Offset): String? {
        return findIntersectingKey(offset)
    }

    fun processPathToKeySequence(path: List<Offset>): String {
        if (path.isEmpty()) {
            return ""
        }

        // Validate all offsets are finite and non-negative
        val validPath = path.filter { offset ->
            val isValid = offset.x.isFinite() &&
                         offset.y.isFinite() &&
                         offset.x >= 0 &&
                         offset.y >= 0

            if (!isValid) {
                Timber.tag(TAG).w("Invalid offset detected: x=${offset.x}, y=${offset.y}")
            }
            isValid
        }

        // Size check
        if (validPath.size < MIN_PATH_LENGTH) {
            return ""
        }

        // Safety check for excessively long paths
        val finalPath = if (validPath.size > MAX_PATH_POINTS) {
            validPath.take(MAX_PATH_POINTS)
        } else {
            validPath
        }

        val filteredPath = filterByVelocity(finalPath)

        // Map path points to keys
        return filteredPath
            .mapNotNull { offset -> findIntersectingKey(offset) }
            .distinctConsecutive()
            .joinToString("")
    }

    private fun findIntersectingKey(offset: Offset): String? {
        return keyPositions.entries.find { (_, rect) -> rect.contains(offset) }?.key
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
