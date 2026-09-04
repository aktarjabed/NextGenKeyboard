package com.aktarjabed.nxtgenkeyboard.util

/** One-shot channel so ClipboardHistoryActivity can hand a text item back to the IME for direct insertion. */
object ClipboardInsertBus {
    @Volatile
    private var pendingToken: String? = null
    @Volatile
    private var pendingText: String? = null

    fun post(token: String, text: String) {
        pendingToken = token
        pendingText = text
    }

    fun peek(token: String): String? {
        if (pendingToken == token) {
            return pendingText
        }
        return null
    }

    fun consume(token: String): String? {
        if (pendingToken == token) {
            val value = pendingText
            pendingToken = null
            pendingText = null
            return value
        }
        return null
    }
}