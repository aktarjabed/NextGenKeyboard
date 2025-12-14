package com.aktarjabed.nextgenkeyboard.feature.ai

interface PredictionEngine {
    /**
     * Generates a list of next-word predictions based on the context.
     * @param context The text preceding the cursor.
     * @return A list of predicted words.
     */
    suspend fun predict(context: String): List<String>
}
