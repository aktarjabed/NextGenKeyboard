package com.aktarjabed.nextgenkeyboard.feature.autocorrect

import android.content.Context
import android.content.res.Resources
import com.aktarjabed.nextgenkeyboard.data.model.Language
import com.aktarjabed.nextgenkeyboard.data.local.LearnedWordDao
import com.aktarjabed.nextgenkeyboard.state.CorrectionType
// import com.aktarjabed.nextgenkeyboard.state.WordContext // Removed incorrect import
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
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
@Config(sdk = [30])
class AdvancedAutocorrectEngineTest {

    private lateinit var context: Context
    private lateinit var resources: Resources
    private lateinit var learnedWordDao: LearnedWordDao
    private lateinit var autocorrectEngine: AdvancedAutocorrectEngine

    @Before
    fun setup() {
        context = mockk(relaxed = true)
        resources = mockk(relaxed = true)
        learnedWordDao = mockk(relaxed = true)
        every { context.resources } returns resources

        // Mock dictionary loading
        every { resources.openRawResource(any()) } answers {
            ByteArrayInputStream("the\nand\nyou".toByteArray())
        }

        // Mock DB loading
        coEvery { learnedWordDao.getAllWordsSync() } returns emptyList()

        autocorrectEngine = AdvancedAutocorrectEngine(context, learnedWordDao)
    }

    @Test
    fun `test spelling correction works correctly`() = runTest {
        // Given
        val word = "teh"
        val context = WordContext()
        val language = Language(
            code = "en",
            name = "English",
            nativeName = "English"
        )

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
        val language = Language(
            code = "en",
            name = "English",
            nativeName = "English"
        )

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
        val language = Language(
            code = "en",
            name = "English",
            nativeName = "English"
        )

        // When
        val suggestions = autocorrectEngine.getAdvancedSuggestions(newWord, context, language)

        // Then
        // After learning, the word should be considered correct, so no spelling suggestions should be returned.
        val spellingSuggestion = suggestions.find { it.type == CorrectionType.SPELLING }
        assertThat(spellingSuggestion).isNull()
    }

    @Test
    fun `getAdvancedSuggestions returns empty list for short words`() = runTest {
        // Given
        val word = "a"
        val context = WordContext()
        val language = Language(
            code = "en",
            name = "English",
            nativeName = "English"
        )

        // When
        val suggestions = autocorrectEngine.getAdvancedSuggestions(word, context, language)

        // Then
        assertThat(suggestions).isEmpty()
    }
}
