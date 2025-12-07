package com.aktarjabed.nextgenkeyboard.feature.ai

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AiContextManager @Inject constructor() {

    // Shallow in-memory history: keeps last few committed texts
    private val _committedHistory = ArrayDeque<String>(5)

    fun addCommittedText(text: String) {
        if (text.isBlank()) return

        // Basic normalization: trim
        val normalized = text.trim()

        // Add to history
        synchronized(_committedHistory) {
            if (_committedHistory.size >= 5) {
                _committedHistory.removeFirst()
            }
            _committedHistory.addLast(normalized)
        }
    }

    fun getContext(currentInput: String): String {
        val history: String
        synchronized(_committedHistory) {
            history = _committedHistory.joinToString(" ")
        }

        // Combine history with current input
        // "History... CurrentInput"
        return if (history.isNotBlank()) {
            "$history $currentInput"
        } else {
            currentInput
        }
    }

    fun clearHistory() {
        synchronized(_committedHistory) {
            _committedHistory.clear()
        }
    }
}
