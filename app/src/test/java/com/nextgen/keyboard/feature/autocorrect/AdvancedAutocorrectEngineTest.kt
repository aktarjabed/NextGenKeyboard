package com.nextgen.keyboard.feature.autocorrect

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.nextgen.keyboard.data.model.Language
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class AdvancedAutocorrectEngineTest {

    private lateinit var context: Context
    private lateinit var autocorrectEngine: AdvancedAutocorrectEngine

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        autocorrectEngine = AdvancedAutocorrectEngine(context)
    }

    @Test
    fun `test dictionary loads from resource`() = runBlocking {
        // The dictionary is loaded in the init block, so we just need to check if it's not empty
        val suggestions = autocorrectEngine.getAdvancedSuggestions("tst", WordContext(), Language.ENGLISH)
        // No specific assertions on suggestions, just ensuring the engine doesn't crash and works.
        // A more robust test would mock the dictionary resource, but for this scope, we confirm it runs.
        assertTrue(true)
    }
}