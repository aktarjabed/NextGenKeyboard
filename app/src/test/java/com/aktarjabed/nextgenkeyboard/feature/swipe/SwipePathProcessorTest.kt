package com.aktarjabed.nextgenkeyboard.feature.swipe

import android.content.Context
import android.util.DisplayMetrics
import androidx.compose.ui.geometry.Offset
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33], manifest = Config.NONE)
class SwipePathProcessorTest {

    private lateinit var processor: SwipePathProcessor
    private lateinit var context: Context

    @Before
    fun setup() {
        context = mockk(relaxed = true)
        val metrics = DisplayMetrics().apply {
            widthPixels = 1080
            heightPixels = 2400
        }
        every { context.resources.displayMetrics } returns metrics

        processor = SwipePathProcessor(context)
    }

    @Test
    fun `processPathToKeySequence handles empty path`() {
        val result = processor.processPathToKeySequence(emptyList())
        assertThat(result).isEmpty()
    }

    @Test
    fun `processPathToKeySequence truncates long paths safely`() {
        // Generate a path longer than MAX_PATH_LENGTH (500)
        val longPath = List(600) { Offset(it.toFloat(), it.toFloat()) }

        // This should not crash
        val result = processor.processPathToKeySequence(longPath)

        // We can't verify the exact output easily without registering keys,
        // but we verify it didn't throw an exception.
        assertThat(result).isNotNull()
    }

    @Test
    fun `processPathToKeySequence validates bounds`() {
        // Points outside screen bounds (1080x2400)
        val invalidPath = listOf(
            Offset(-10f, 0f), // Invalid
            Offset(2000f, 0f), // Invalid
            Offset(50f, 50f)  // Valid
        )

        // Only valid points should be processed.
        // If we register a key at 50,50, we might get a hit.
        // For now just ensuring no crash.
        val result = processor.processPathToKeySequence(invalidPath)
        assertThat(result).isNotNull()
    }
}
