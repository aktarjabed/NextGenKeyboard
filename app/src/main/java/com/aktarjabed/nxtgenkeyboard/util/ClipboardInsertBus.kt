package com.aktarjabed.nxtgenkeyboard.util

/** One-shot channel so ClipboardHistoryActivity can hand a text item back to the IME for direct insertion. */
object ClipboardInsertBus {
    @Volatile
    private var pendingToken: String? = null
    @Volatile
    private var pendingText: String? = null

    private var listener: (() -> Unit)? = null

    fun registerListener(l: () -> Unit) {
        listener = l
    }

    fun unregisterListener() {
        listener = null
    }

    fun post(token: String, text: String) {
        pendingToken = token
        pendingText = text
        listener?.invoke()
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