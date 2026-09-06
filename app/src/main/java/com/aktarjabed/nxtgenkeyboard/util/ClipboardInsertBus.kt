package com.aktarjabed.nxtgenkeyboard.util

import java.util.concurrent.ConcurrentHashMap

/** One-shot channel so ClipboardHistoryActivity can hand a text item back to the IME for direct insertion. */
import java.util.concurrent.atomic.AtomicReference

data class PendingInsert(
    val token: String,
    val text: String
)

object ClipboardInsertBus {
    private val pending = AtomicReference<PendingInsert?>(null)
    private val listeners = ConcurrentHashMap<String, () -> Unit>()

    fun registerListener(token: String, l: () -> Unit) {
        listeners[token] = l
    }

    fun unregisterListener(token: String) {
        listeners.remove(token)
    }

    fun post(token: String, text: String) {
        pending.set(PendingInsert(token, text))
        listeners[token]?.invoke()
    }

    fun take(token: String): String? {
        while (true) {
            val current = pending.get() ?: return null
            if (current.token == token) {
                if (pending.compareAndSet(current, null)) {
                    return current.text
                }
            } else {
                return null
            }
        }
    }

    fun clear(token: String) {
        while (true) {
            val current = pending.get() ?: return
            if (current.token == token) {
                if (pending.compareAndSet(current, null)) {
                    return
                }
            } else {
                return
            }
        }
    }
}
