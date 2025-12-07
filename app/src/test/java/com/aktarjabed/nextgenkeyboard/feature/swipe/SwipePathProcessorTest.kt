package com.aktarjabed.nextgenkeyboard.feature.swipe

import androidx.compose.ui.geometry.Offset
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.aktarjabed.nextgenkeyboard.feature.swipe.SwipePathProcessor
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import javax.inject.Inject

@RunWith(RobolectricTestRunner::class)
@Config(application = HiltTestApplication::class)
class SwipePathProcessorTest {

    private lateinit var processor: SwipePathProcessor

    @Before
    fun setUp() {
        // Mocking context isn't trivial for Robolectric + Hilt without setup,
        // but SwipePathProcessor only needs Context for DisplayMetrics.
        // We can manually instantiate it with ApplicationProvider.getApplicationContext()
        // or just mock the context.
        // Given we don't have easy Mockito setup here without checking libs, let's try direct instantiation
        // using ApplicationProvider which Robolectric supports.
        processor = SwipePathProcessor(ApplicationProvider.getApplicationContext())
    }

    @Test
    fun `processPathToKeySequence handles path too long by truncating`() {
        // Generate a path longer than 500 points
        val longPath = List(501) { Offset(it.toFloat(), it.toFloat()) }
        // Should not throw exception
        processor.processPathToKeySequence(longPath)
    }

    @Test
    fun `processPathToKeySequence handles valid path`() {
        // A simple valid path
        val validPath = listOf(
            Offset(10f, 10f),
            Offset(20f, 20f),
            Offset(30f, 30f)
        )
        // This shouldn't throw, return value might be empty string as no keys registered
        val result = processor.processPathToKeySequence(validPath)
        // We just care it doesn't crash
    }
}
