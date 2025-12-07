package com.aktarjabed.nextgenkeyboard.feature.ai

import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SmartPredictionUseCase @Inject constructor(
    private val client: AiPredictionClient,
    private val promptBuilder: PromptBuilder
) {

    suspend fun getPredictions(contextText: String): List<String> = withContext(Dispatchers.IO) {
        if (contextText.isBlank()) return@withContext emptyList()

        // Safety check: Don't send too long context
        val safeContext = contextText.takeLast(500)

        val prompt = promptBuilder.buildPredictionPrompt(safeContext)
        client.generatePredictions(prompt)
    }
}
