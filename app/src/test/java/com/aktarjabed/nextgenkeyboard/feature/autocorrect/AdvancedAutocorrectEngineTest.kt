package com.aktarjabed.nextgenkeyboard.feature.autocorrect

import android.content.Context
import android.content.res.Resources
import com.google.common.truth.Truth.assertThat
import com.aktarjabed.nextgenkeyboard.data.models.Language
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.ByteArrayInputStream

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class AdvancedAutocorrectEngineTest {

    private lateinit var context: Context
    private lateinit var resources: Resources
    private lateinit var autocorrectEngine: AdvancedAutocorrectEngine

    @Before
    fun setup() {
        context = mockk(relaxed = true)
        resources = mockk(relaxed = true)
        every { context.resources } returns resources

        // Mock Android's Base64 class if it's used internally by any components
        mockkStatic(android.util.Base64::class)
        every { android.util.Base64.encodeToString(any(), any()) } returns "encoded_string"
        every { android.util.Base64.decode(any<String>(), any()) } returns ByteArray(0)

        // Mock dictionary loading
        val dictionaryStream = ByteArrayInputStream("the\nand\nyou".toByteArray())
        every { resources.openRawResource(any()) } returns dictionaryStream

        autocorrectEngine = AdvancedAutocorrectEngine(context)
    }

    @Test
    fun `test spelling correction works correctly`() = runTest {
        // Given
        val word = "teh"
        val context = WordContext()
        val language = Language("en", "English")

        // When
        val suggestions = autocorrectEngine.getAdvancedSuggestions(word, context, language)

        // Then
        assertThat(suggestions).isNotEmpty()
        assertThat(suggestions.first().suggestion).isEqualTo("the")
    }

    @Test
    fun `test context-aware suggestions for capitalization`() = runTest {
        // Given
        val word = "hello"
        val context = WordContext(isStartOfSentence = true)
        val language = Language("en", "English")

        // When
        val suggestions = autocorrectEngine.getAdvancedSuggestions(word, context, language)

        // Then
        assertThat(suggestions).isNotEmpty()
        val capitalizationSuggestion = suggestions.find { it.type == CorrectionType.CAPITALIZATION }
        assertThat(capitalizationSuggestion).isNotNull()
        assertThat(capitalizationSuggestion?.suggestion).isEqualTo("Hello")
    }

    @Test
    fun `test learns new words correctly`() = runTest {
        // Given
        val newWord = "testword"
        autocorrectEngine.learnWord(newWord)
        val context = WordContext()
        val language = Language("en", "English")

        // When
        val suggestions = autocorrectEngine.getAdvancedSuggestions(newWord, context, language)

        // Then
        // After learning, the word should be considered correct, so no spelling suggestions should be returned.
        // Contextual suggestions might still appear.
        val spellingSuggestion = suggestions.find { it.type == CorrectionType.SPELLING }
        assertThat(spellingSuggestion).isNull()
    }

    @Test
    fun `getAdvancedSuggestions returns empty list for short words`() = runTest {
        // Given
        val word = "a"
        val context = WordContext()
        val language = Language("en", "English")

        // When
        val suggestions = autocorrectEngine.getAdvancedSuggestions(word, context, language)

        // Then
        assertThat(suggestions).isEmpty()
    }
}