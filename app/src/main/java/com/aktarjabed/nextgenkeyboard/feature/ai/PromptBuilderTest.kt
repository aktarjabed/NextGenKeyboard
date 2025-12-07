package com.aktarjabed.nextgenkeyboard.feature.ai

import org.junit.Assert.assertEquals
import org.junit.Test

class PromptBuilderTest {

    private val promptBuilder = PromptBuilder()

    @Test
    fun `buildPredictionPrompt contains context`() {
        val context = "Hello world"
        val prompt = promptBuilder.buildPredictionPrompt(context)

        assert(prompt.contains(context))
        assert(prompt.contains("Predict the next 3 most likely words"))
    }
}
