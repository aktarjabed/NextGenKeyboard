package com.aktarjabed.nxtgenkeyboard.util

/** One-shot channel so ClipboardHistoryActivity can hand a text item back to the IME for direct insertion. */
object ClipboardInsertBus {
    @Volatile
    private var pending: String? = null

    fun post(text: String) {
        pending = text
    }

    fun consume(): String? {
        val value = pending ?: return null
        pending = null
        return value
    }
}