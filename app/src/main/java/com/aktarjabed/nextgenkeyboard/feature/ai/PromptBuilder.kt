package com.aktarjabed.nextgenkeyboard.feature.ai

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PromptBuilder @Inject constructor() {

    fun buildPredictionPrompt(contextText: String): String {
        // Construct a prompt that asks for 3 next-word predictions.
        // It should be concise to save tokens.
        return """
            Task: Predict the next 3 most likely words or short phrases to complete the text.
            Input: "$contextText"

            Output format: Comma-separated list of words. No explanations.
            Example: "How are", Output: "you, they, we"
        """.trimIndent()
    }
}
