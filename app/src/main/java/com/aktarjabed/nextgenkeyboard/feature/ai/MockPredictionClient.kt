package com.aktarjabed.nextgenkeyboard.feature.ai

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MockPredictionClient @Inject constructor() : AiPredictionClient {

    override fun isAvailable(): Boolean = false // Always false for mock? Or true to simulate?
    // In this context, if we fallback to Mock, it means Real is unavailable,
    // but Mock can still return "fake" predictions for testing/demo if needed.
    // However, the prompt says "fall back to a no-op/mock implementation".

    override suspend fun generatePredictions(prompt: String): List<String> {
        // Return empty list or basic static suggestions for debug
        return emptyList()
    }
}
