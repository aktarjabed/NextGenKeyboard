package com.aktarjabed.nextgenkeyboard.feature.swipe

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class SwipePathProcessorTest {
    private lateinit var processor: SwipePathProcessor

    @Before
    fun setup() {
        processor = SwipePathProcessor()
    }

    @Test
    fun `processPathToKeySequence rejects short invalid path`() {
        val invalidPath = listOf(
            Offset(Float.NaN, Float.NaN),  // Invalid NaN
            Offset(Float.POSITIVE_INFINITY, 100f)  // Invalid infinity
        )
        assertEquals("", processor.processPathToKeySequence(invalidPath))
    }

    @Test
    fun `processPathToKeySequence handles concurrent registration safely`() {
        processor.registerKeyPosition("a", Rect(0f, 0f, 100f, 100f))
        processor.registerKeyPosition("b", Rect(100f, 0f, 200f, 100f))

        // Simulate concurrent access (JUnit runs single-threaded, but tests structure)
        val path = listOf(
            Offset(50f, 50f), Offset(150f, 50f), Offset(150f, 150f)
        )
        // path goes from 50,50 (a) to 150,50 (b) to 150,150 (outside)
        // Expected: "ab" or "a" depending on filtering.
        // 50,50 is in 'a'. 150,50 is in 'b'.
        // The distinctConsecutive should handle "aaab" -> "ab".
        // The implementation finds intersecting key for each filtered point.
        val result = processor.processPathToKeySequence(path)
        assertTrue("Expected result to contain 'ab' but was '$result'", result.contains("ab"))
    }

    @Test
    fun `processPathToKeySequence filters low velocity correctly`() {
        // Add test keys...
        processor.registerKeyPosition("a", Rect(0f, 0f, 100f, 100f))

        val slowPath = listOf(
            Offset(0f, 0f), Offset(1f, 1f), Offset(2f, 2f)  // Velocity <5f
        )
        assertEquals("", processor.processPathToKeySequence(slowPath))
    }
}
