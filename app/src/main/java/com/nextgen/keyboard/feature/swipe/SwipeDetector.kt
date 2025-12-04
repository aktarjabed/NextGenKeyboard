package com.nextgen.keyboard.feature.swipe

import timber.log.Timber
import kotlin.math.sqrt

class SwipeDetector(
    private val onWordPredicted: (String) -> Unit,
    private val onSwipeProgress: (Float) -> Unit
) {
    private var isSwipeActive = false
    private val swipePoints = mutableListOf<Pair<Float, Float>>()
    private val keyTrail = mutableListOf<String>()
    private var lastX = 0f
    private var lastY = 0f

    fun startSwipe(x: Float, y: Float) {
        isSwipeActive = true
        swipePoints.clear()
        keyTrail.clear()
        swipePoints.add(Pair(x, y))
        lastX = x
        lastY = y
        Timber.d("Swipe started at ($x, $y)")
    }

    fun addSwipePoint(x: Float, y: Float, key: String? = null) {
        if (!isSwipeActive) return

        val distance = sqrt((x - lastX) * (x - lastX) + (y - lastY) * (y - lastY))
        if (distance > 20f) {
            swipePoints.add(Pair(x, y))
            lastX = x
            lastY = y

            val progress = (swipePoints.size.toFloat() / 20f).coerceAtMost(1f)
            onSwipeProgress(progress)
        }

        key?.let { addKeyToTrail(it) }
    }

    fun addKeyToTrail(key: String) {
        if (keyTrail.isEmpty() || keyTrail.last() != key) {
            keyTrail.add(key)
            Timber.d("Key trail: ${keyTrail.joinToString("")}")
        }
    }

    fun endSwipe() {
        if (!isSwipeActive) return

        isSwipeActive = false
        val word = predictWord()
        if (word.isNotEmpty()) {
            onWordPredicted(word)
            Timber.d("Predicted word: $word")
        }

        swipePoints.clear()
        keyTrail.clear()
    }

    private fun predictWord(): String {
        if (keyTrail.size < 2) return ""

        return keyTrail.joinToString("").lowercase()
    }

    fun isActive(): Boolean = isSwipeActive
}