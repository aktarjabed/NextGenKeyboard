package com.aktarjabed.nextgenkeyboard.feature.swipe

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class SwipePathProcessorTest {

    private lateinit var processor: SwipePathProcessor

    @Before
    fun setup() {
        processor = SwipePathProcessor()
        // Register test keys
        processor.registerKeyPosition("h", Rect(0f, 0f, 50f, 50f))
        processor.registerKeyPosition("e", Rect(60f, 0f, 110f, 50f))
        processor.registerKeyPosition("l", Rect(120f, 0f, 170f, 50f))
        processor.registerKeyPosition("o", Rect(180f, 0f, 230f, 50f))
    }

    @Test
    fun processPathToKeySequence_returnsEmpty_forPathTooShort() {
        val shortPath = listOf(Offset(10f, 10f), Offset(20f, 20f))
        val result = processor.processPathToKeySequence(shortPath)
        assertThat(result).isEmpty()
    }

    @Test
    fun processPathToKeySequence_returnsEmpty_forEmptyPath() {
        val result = processor.processPathToKeySequence(emptyList())
        assertThat(result).isEmpty()
    }

    @Test
    fun processPathToKeySequence_filtersOutNaNCoordinates() {
        val pathWithNaN = listOf(
            Offset(10f, 10f), // h
            Offset(Float.NaN, 20f), // Invalid
            Offset(125f, 10f), // l
            Offset(190f, 10f)  // o
        )
        // With velocity filter, we need enough points.
        // The NaN is filtered out. Remaining: h, l, o.
        // 3 points is enough.
        val result = processor.processPathToKeySequence(pathWithNaN)
        assertThat(result).isEqualTo("hlo")
    }

    @Test
    fun processPathToKeySequence_filtersOutInfiniteCoordinates() {
        val pathWithInf = listOf(
            Offset(10f, 10f), // h
            Offset(Float.POSITIVE_INFINITY, 20f), // Invalid
            Offset(125f, 10f), // l
            Offset(190f, 10f)  // o
        )
        val result = processor.processPathToKeySequence(pathWithInf)
        assertThat(result).isEqualTo("hlo")
    }

    @Test
    fun processPathToKeySequence_filtersOutNegativeCoordinates() {
        val pathWithNegative = listOf(
            Offset(10f, 10f), // h
            Offset(-5f, 20f), // Invalid
            Offset(125f, 10f), // l
            Offset(190f, 10f)  // o
        )
        val result = processor.processPathToKeySequence(pathWithNegative)
        assertThat(result).isEqualTo("hlo")
    }

    @Test
    fun processPathToKeySequence_returnsKeySequence_forValidPath() {
        val validPath = listOf(
            Offset(10f, 10f),  // h
            Offset(75f, 10f),  // e
            Offset(145f, 10f), // l
            Offset(200f, 10f)  // o
        )
        val result = processor.processPathToKeySequence(validPath)
        assertThat(result).isEqualTo("helo")
    }

    @Test
    fun registerKeyPosition_storesKey() {
        processor.registerKeyPosition("A", Rect(0f, 0f, 10f, 10f))
        val key = processor.findKeyAt(Offset(5f, 5f))
        assertThat(key).isEqualTo("A")
    }

    @Test
    fun clearKeys_removesAllKeys() {
        processor.registerKeyPosition("A", Rect(0f, 0f, 10f, 10f))
        processor.clearKeys()

        val key = processor.findKeyAt(Offset(5f, 5f))
        assertThat(key).isNull()
    }

    @Test
    fun registerKeyPosition_ignoresInvalidBounds() {
        // Invalid width (0)
        processor.registerKeyPosition("Invalid", Rect(0f, 0f, 0f, 10f))
        assertThat(processor.findKeyAt(Offset(0f, 5f))).isNull()
    }

    @Test
    fun registerKeyPosition_isThreadSafe() {
        val thread1 = Thread {
            repeat(100) {
                processor.registerKeyPosition("t$it", Rect(it.toFloat(), it.toFloat(), it + 50f, it + 50f))
            }
        }

        val thread2 = Thread {
            repeat(100) {
                processor.registerKeyPosition("s$it", Rect(it + 200f, it.toFloat(), it + 250f, it + 50f))
            }
        }

        thread1.start()
        thread2.start()
        thread1.join()
        thread2.join()

        // No crash means success
    }
}
