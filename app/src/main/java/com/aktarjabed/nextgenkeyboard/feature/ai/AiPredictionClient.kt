package com.aktarjabed.nextgenkeyboard.feature.ai

interface AiPredictionClient {
    suspend fun generatePredictions(prompt: String): List<String>
    fun isAvailable(): Boolean
}
