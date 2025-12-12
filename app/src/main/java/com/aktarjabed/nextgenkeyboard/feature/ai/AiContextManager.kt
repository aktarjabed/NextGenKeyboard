package com.aktarjabed.nextgenkeyboard.feature.ai

import com.aktarjabed.nextgenkeyboard.util.SecurityUtils
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AiContextManager @Inject constructor() {

    companion object {
        private const val MAX_HISTORY_SIZE = 5
    }

    // Shallow in-memory history: keeps last few committed texts
    private val _committedHistory = ArrayDeque<String>(MAX_HISTORY_SIZE)

    fun addCommittedText(text: String) {
        if (text.isBlank()) return

        // Basic normalization: trim
        val normalized = text.trim()

        // Privacy check: Do not store sensitive content in context
        if (SecurityUtils.isSensitiveContent(normalized)) {
            return
        }

        // Add to history
        synchronized(_committedHistory) {
            if (_committedHistory.size >= MAX_HISTORY_SIZE) {
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
