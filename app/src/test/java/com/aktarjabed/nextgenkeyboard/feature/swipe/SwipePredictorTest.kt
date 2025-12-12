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
        // Use a polling approach instead of sleep
        awaitUntilDictionaryLoaded(timeoutMs = 2000)

        val suggestions = predictor.getSuggestions("hel")
        println("Suggestions for 'hel': $suggestions")
        val result = predictor.predictWord("hel")
        // Should find "hello"
        assertEquals("hello", result)
    }

    private fun awaitUntilDictionaryLoaded(timeoutMs: Long) {
        val startTime = System.currentTimeMillis()
        // We can check if getSuggestions returns something as a proxy for loaded state
        // because isDictionaryLoaded is private, but learnWord sets it to true.
        // Or we can assume learnWord worked and we just need to wait for the thread visibility?
        // Actually, with the synchronized fix, learnWord should be immediate.
        // But the original test used sleep(100) and failed.
        // Let's poll for a short while.

        while (System.currentTimeMillis() - startTime < timeoutMs) {
            val suggestions = predictor.getSuggestions("hel")
            if (suggestions.isNotEmpty()) {
                return
            }
            Thread.sleep(10)
        }
        // If we timeout, we proceed and likely fail assertion, or we can fail here.
        // But learnWord("hello") should have populated it.
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
