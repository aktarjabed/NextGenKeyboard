package com.aktarjabed.nxtgenkeyboard.suggestion

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class SuggestionEngineTest {

    private val context: Context = ApplicationProvider.getApplicationContext()
    private val engine = SuggestionEngine(context)

    @Test
    fun `suggest returns close matches`() {
        engine.load("en")
        val suggestions = engine.suggest("en", "teh", max = 3, maxDistance = 1)
        assertTrue(suggestions.contains("the"))
    }

    @Test
    fun `suggest respects maxDistance`() {
        engine.load("en")
        val suggestions = engine.suggest("en", "teh", max = 5, maxDistance = 0)
        assertTrue(suggestions.isEmpty())
    }
}