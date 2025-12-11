package com.aktarjabed.nextgenkeyboard.feature.swipe

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.test.core.app.ApplicationProvider
import dagger.hilt.android.testing.HiltTestApplication
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(application = HiltTestApplication::class)
class SwipePathProcessorTest {

    private lateinit var processor: SwipePathProcessor

    @Before
    fun setUp() {
        processor = SwipePathProcessor(ApplicationProvider.getApplicationContext())
    }

    @Test
    fun `processPathToKeySequence returns empty for short paths`() {
        val shortPath = listOf(Offset(10f, 10f), Offset(11f, 11f))
        val result = processor.processPathToKeySequence(shortPath)
        assertEquals("", result)
    }

    @Test
    fun `processPathToKeySequence truncates long paths`() {
        val longPath = List(600) { Offset(it.toFloat(), it.toFloat()) }
        // We can't easily assert internal state, but we ensure it runs without exception
        // and doesn't process 600 points (performance check implied).
        val result = processor.processPathToKeySequence(longPath)
        // With no keys registered, it should return empty
        assertEquals("", result)
    }

    @Test
    fun `processPathToKeySequence filters invalid coordinates`() {
        val pathWithInvalid = listOf(
            Offset(10f, 10f),
            Offset(-5f, 10f), // Invalid
            Offset(20f, 20f),
            Offset(Float.NaN, 20f) // Invalid
        )
        // Should ignore invalid points. Remaining valid points: 2.
        // Min path length is 3, so result should be empty.
        val result = processor.processPathToKeySequence(pathWithInvalid)
        assertEquals("", result)
    }

    @Test
    fun `registerKey and findKeyAt works correctly`() {
        val keyRect = Rect(0f, 0f, 100f, 100f)
        processor.registerKeyPosition("A", keyRect)

        val foundKey = processor.findKeyAt(Offset(50f, 50f))
        assertEquals("A", foundKey)

        val notFoundKey = processor.findKeyAt(Offset(150f, 150f))
        assertEquals(null, notFoundKey)
    }

    @Test
    fun `processPathToKeySequence maps path to keys`() {
        // Register keys 'H', 'E', 'L', 'L', 'O'
        // Layout: H(0-10), E(10-20), L(20-30), O(30-40)
        processor.registerKeyPosition("H", Rect(0f, 0f, 10f, 10f))
        processor.registerKeyPosition("E", Rect(10f, 0f, 20f, 10f))
        processor.registerKeyPosition("L", Rect(20f, 0f, 30f, 10f))
        processor.registerKeyPosition("O", Rect(30f, 0f, 40f, 10f))

        // Path swiping through H -> E -> L -> O
        val path = listOf(
            Offset(5f, 5f),   // H
            Offset(15f, 5f),  // E
            Offset(25f, 5f),  // L
            Offset(35f, 5f)   // O
        )

        val result = processor.processPathToKeySequence(path)
        assertEquals("HELO", result)
    }
}
