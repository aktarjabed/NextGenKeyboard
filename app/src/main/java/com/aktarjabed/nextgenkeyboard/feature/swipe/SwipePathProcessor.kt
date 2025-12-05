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

    fun processPathToKeySequence(path: List<Offset>): String {
        // Filter out very short paths (accidental touches)
        if (path.size < 3) return ""

        // Map path points to keys
        return path
            .mapNotNull { offset ->
                keyPositions.entries.find { (_, rect) -> rect.contains(offset) }?.key
            }
            .distinctConsecutive() // Remove consecutive duplicates
            .joinToString("")
    }

    private fun <T> List<T>.distinctConsecutive(): List<T> =
        fold(mutableListOf<T>()) { acc, item ->
            if (acc.isEmpty() || acc.last() != item) {
                acc.add(item)
            }
            acc
        }
}
