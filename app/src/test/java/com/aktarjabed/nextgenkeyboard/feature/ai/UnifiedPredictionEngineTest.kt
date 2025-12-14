package com.aktarjabed.nextgenkeyboard.feature.ai

import com.aktarjabed.nextgenkeyboard.data.model.Language
import com.aktarjabed.nextgenkeyboard.feature.autocorrect.AdvancedAutocorrectEngine
import com.aktarjabed.nextgenkeyboard.feature.autocorrect.AdvancedSuggestion
import com.aktarjabed.nextgenkeyboard.state.CorrectionType
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import kotlinx.coroutines.TimeoutCancellationException
import com.google.common.truth.Truth.assertThat

class UnifiedPredictionEngineTest {

    private lateinit var geminiClient: GeminiPredictionClient
    private lateinit var localEngine: AdvancedAutocorrectEngine
    private lateinit var unifiedEngine: UnifiedPredictionEngine

    @Before
    fun setup() {
        geminiClient = mockk()
        localEngine = mockk()
        unifiedEngine = UnifiedPredictionEngine(geminiClient, localEngine)
    }

    @Test
    fun `predict uses Gemini when available and returns results`() = runTest {
        // Given
        val context = "Hello world"
        val expectedPredictions = listOf("today", "is")
        coEvery { geminiClient.isAvailable() } returns true
        coEvery { geminiClient.generatePredictions(context) } returns expectedPredictions

        // When
        val result = unifiedEngine.predict(context)

        // Then
        assertEquals(expectedPredictions, result)
        coVerify(exactly = 0) { localEngine.getAdvancedSuggestions(any(), any(), any()) }
    }

    @Test
    fun `predict falls back to local engine when Gemini is unavailable`() = runTest {
        // Given
        val context = "This is a test"
        val lastWord = "test"
        coEvery { geminiClient.isAvailable() } returns false

        val localSuggestions = listOf(
            AdvancedSuggestion(lastWord, "testing", 0.9f, CorrectionType.SUGGEST_NEXT)
        )
        coEvery { localEngine.getAdvancedSuggestions(any(), any(), any()) } returns localSuggestions

        // When
        val result = unifiedEngine.predict(context)

        // Then
        assertEquals(listOf("testing"), result)
        coVerify { localEngine.getAdvancedSuggestions(eq(lastWord), any(), any()) }
    }

    @Test
    fun `predict falls back to local engine when Gemini throws exception`() = runTest {
        // Given
        val context = "Error case"
        coEvery { geminiClient.isAvailable() } returns true
        coEvery { geminiClient.generatePredictions(context) } throws RuntimeException("API Error")

        val localSuggestions = listOf(
            AdvancedSuggestion("case", "cases", 0.8f, CorrectionType.SUGGEST_NEXT)
        )
        coEvery { localEngine.getAdvancedSuggestions(any(), any(), any()) } returns localSuggestions

        // When
        val result = unifiedEngine.predict(context)

        // Then
        assertEquals(listOf("cases"), result)
    }

    @Test
    fun `predict returns empty list if both engines fail`() = runTest {
        // Given
        val context = "Total failure"
        coEvery { geminiClient.isAvailable() } returns true
        coEvery { geminiClient.generatePredictions(context) } throws RuntimeException("Gemini Fail")
        coEvery { localEngine.getAdvancedSuggestions(any(), any(), any()) } throws RuntimeException("Local Fail")

        // When
        val result = unifiedEngine.predict(context)

        // Then
        assertThat(result).isEmpty()
    }
}
