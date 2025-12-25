package com.aktarjabed.nextgenkeyboard.feature.suggestions

import com.aktarjabed.nextgenkeyboard.data.model.Language
import com.aktarjabed.nextgenkeyboard.feature.ai.SmartPredictionUseCase
import com.aktarjabed.nextgenkeyboard.feature.autocorrect.AdvancedAutocorrectEngine
import com.aktarjabed.nextgenkeyboard.feature.autocorrect.AdvancedSuggestion
import com.aktarjabed.nextgenkeyboard.feature.autocorrect.WordContext
import com.aktarjabed.nextgenkeyboard.state.CorrectionType
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class CompositeSuggestionProviderTest {

    private lateinit var provider: CompositeSuggestionProvider
    private lateinit var localEngine: AdvancedAutocorrectEngine
    private lateinit var aiUseCase: SmartPredictionUseCase

    @Before
    fun setup() {
        localEngine = mockk()
        aiUseCase = mockk()
        provider = CompositeSuggestionProvider(localEngine, aiUseCase)
    }

    @Test
    fun `getMergedSuggestions prioritizes high confidence local suggestion`() = runTest {
        // Mock Local
        val localSugg = AdvancedSuggestion("teh", "the", 0.9f, CorrectionType.AUTO_CORRECT)
        coEvery { localEngine.getAdvancedSuggestions(any(), any(), any()) } returns listOf(localSugg)

        // Mock AI
        coEvery { aiUseCase.getPredictions(any()) } returns listOf("apple", "banana")

        val result = provider.getMergedSuggestions("teh", "context", "en")

        // Expect "the" (local high conf) first, then AI suggestions
        assertThat(result).hasSize(3)
        assertThat(result[0]).isEqualTo("the")
        assertThat(result).contains("apple")
    }

    @Test
    fun `getMergedSuggestions includes medium confidence top suggestion`() = runTest {
        // Mock Local with medium confidence (below 0.8)
        val localSugg = AdvancedSuggestion("wro", "word", 0.6f, CorrectionType.SPELLING)
        coEvery { localEngine.getAdvancedSuggestions(any(), any(), any()) } returns listOf(localSugg)

        // Mock AI
        coEvery { aiUseCase.getPredictions(any()) } returns listOf("apple")

        val result = provider.getMergedSuggestions("wro", "context", "en")

        // It should NOT be skipped
        assertThat(result).contains("word")
    }

    @Test
    fun `getMergedSuggestions handles empty AI suggestions gracefully`() = runTest {
        // Mock Local
        val localSugg1 = AdvancedSuggestion("helo", "hello", 0.7f, CorrectionType.SPELLING)
        val localSugg2 = AdvancedSuggestion("helo", "help", 0.6f, CorrectionType.SPELLING)
        coEvery { localEngine.getAdvancedSuggestions(any(), any(), any()) } returns listOf(localSugg1, localSugg2)

        // Mock AI (Empty or Error)
        coEvery { aiUseCase.getPredictions(any()) } returns emptyList()

        val result = provider.getMergedSuggestions("helo", "context", "en")

        // Should return only local suggestions
        assertThat(result).contains("hello")
        assertThat(result).contains("help")
    }

    @Test
    fun `getMergedSuggestions dedupes suggestions`() = runTest {
        // Mock Local returning "hello"
        val localSugg = AdvancedSuggestion("hell", "hello", 0.5f, CorrectionType.SUGGEST_NEXT)
        coEvery { localEngine.getAdvancedSuggestions(any(), any(), any()) } returns listOf(localSugg)

        // Mock AI returning "hello" (duplicate) and "world"
        coEvery { aiUseCase.getPredictions(any()) } returns listOf("hello", "world")

        val result = provider.getMergedSuggestions("hell", "context", "en")

        // "hello" should appear only once
        assertThat(result).containsNoDuplicates()
        assertThat(result).contains("world")
    }
}
