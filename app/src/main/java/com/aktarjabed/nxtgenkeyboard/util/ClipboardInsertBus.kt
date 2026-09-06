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
        val listener = listeners[token] ?: return
        pending.set(PendingInsert(token, text))
        listener.invoke()
    }

    fun consume(token: String, action: (String) -> Boolean): Boolean {
        while (true) {
            val current = pending.get() ?: return false
            if (current.token != token) return false
            if (pending.compareAndSet(current, null)) {
                if (action(current.text)) {
                    return true
                } else {
                    // Try to restore if no new item has been set
                    pending.compareAndSet(null, current)
                    return false
                }
            }
        }
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
