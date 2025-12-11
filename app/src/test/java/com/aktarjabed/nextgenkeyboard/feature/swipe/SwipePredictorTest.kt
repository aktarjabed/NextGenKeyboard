package com.aktarjabed.nextgenkeyboard.feature.swipe

import androidx.test.core.app.ApplicationProvider
import dagger.hilt.android.testing.HiltTestApplication
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@RunWith(RobolectricTestRunner::class)
@Config(application = HiltTestApplication::class)
class SwipePredictorTest {

    private lateinit var predictor: SwipePredictor

    @Before
    fun setUp() {
        predictor = SwipePredictor(ApplicationProvider.getApplicationContext())
        // Wait for async init (Robolectric main thread usually handles this if we yield,
        // but since it uses IO dispatcher, we might need a small sleep or rely on eventual consistency)
        // For testing purposes, we might just test 'learnWord' which is synchronous-ish regarding memory
        // but the dictionary load is async.
        // Let's manually trigger a word learn to ensure Trie has something.
        predictor.learnWord("hello")
        predictor.learnWord("world")
    }

    @Test
    fun `predictWord returns exact match`() {
        // Since dictionary load is async, we rely on learned words or sleep
        Thread.sleep(100)
        val suggestions = predictor.getSuggestions("hel")
        println("Suggestions for 'hel': $suggestions")
        val result = predictor.predictWord("hel")
        // Should find "hello"
        assertEquals("hello", result)
    }

    @Test
    fun `predictWord returns input if no match`() {
        val result = predictor.predictWord("xyz")
        assertEquals("xyz", result)
    }

    @Test
    fun `getSuggestions returns multiple options`() {
        predictor.learnWord("test")
        predictor.learnWord("testing")
        predictor.learnWord("tester")

        val suggestions = predictor.getSuggestions("test")
        assertTrue(suggestions.contains("test"))
        assertTrue(suggestions.contains("testing"))
        assertTrue(suggestions.contains("tester"))
    }
}
