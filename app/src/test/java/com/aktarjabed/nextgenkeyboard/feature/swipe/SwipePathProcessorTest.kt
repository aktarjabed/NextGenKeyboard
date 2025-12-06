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
    }

    @Test
    fun registerKeyPosition_storesKey() {
        processor.registerKeyPosition("A", Rect(0f, 0f, 10f, 10f))
        val key = processor.findKeyAt(Offset(5f, 5f))
        assertThat(key).isEqualTo("A")
    }

    @Test
    fun registerKeyPosition_overwritesIfKeyIdIsSame() {
        processor.registerKeyPosition("A", Rect(0f, 0f, 10f, 10f))
        processor.registerKeyPosition("A", Rect(20f, 20f, 30f, 30f))

        val keyAtOld = processor.findKeyAt(Offset(5f, 5f))
        val keyAtNew = processor.findKeyAt(Offset(25f, 25f))

        assertThat(keyAtOld).isNull()
        assertThat(keyAtNew).isEqualTo("A")
    }

    @Test
    fun clearKeys_removesAllKeys() {
        processor.registerKeyPosition("A", Rect(0f, 0f, 10f, 10f))
        processor.clearKeys()

        val key = processor.findKeyAt(Offset(5f, 5f))
        assertThat(key).isNull()
    }

    @Test
    fun layoutChange_withClearKeys_resolvesStaleKeys() {
        // Layout 1: Key "A" at (0,0)-(10,10)
        processor.registerKeyPosition("A", Rect(0f, 0f, 10f, 10f))
        assertThat(processor.findKeyAt(Offset(5f, 5f))).isEqualTo("A")

        // Simulate Layout Change:
        // 1. Clear keys (this is what MainKeyboardView now does)
        processor.clearKeys()

        // 2. Register new keys for Layout 2: Key "B" at same position
        processor.registerKeyPosition("B", Rect(0f, 0f, 10f, 10f))

        // Assert that we find "B", not "A"
        val key = processor.findKeyAt(Offset(5f, 5f))
        assertThat(key).isEqualTo("B")
    }
}
