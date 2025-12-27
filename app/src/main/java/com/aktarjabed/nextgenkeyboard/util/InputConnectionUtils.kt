package com.aktarjabed.nextgenkeyboard.util

import android.os.Build
import android.view.KeyEvent
import android.view.inputmethod.InputConnection
import timber.log.Timber

/**
 * Extension functions for safer InputConnection operations.
 * Catches NullPointerExceptions and other runtime errors that can occur
 * if the InputConnection is invalidated by the system.
 */

fun InputConnection?.safeCommitText(text: CharSequence, newCursorPosition: Int): Boolean {
    if (this == null) return false
    return try {
        commitText(text, newCursorPosition)
    } catch (e: Exception) {
        Timber.e(e, "Failed to commit text")
        false
    }
}

fun InputConnection?.safeDeleteSurroundingText(beforeLength: Int, afterLength: Int): Boolean {
    if (this == null) return false
    return try {
        deleteSurroundingText(beforeLength, afterLength)
    } catch (e: Exception) {
        Timber.e(e, "Failed to delete surrounding text")
        false
    }
}

fun InputConnection?.safeSendKeyEvent(event: KeyEvent): Boolean {
    if (this == null) return false
    return try {
        sendKeyEvent(event)
    } catch (e: Exception) {
        Timber.e(e, "Failed to send key event")
        false
    }
}

fun InputConnection?.safePerformContextMenuAction(id: Int): Boolean {
    if (this == null) return false
    return try {
        performContextMenuAction(id)
    } catch (e: Exception) {
        Timber.e(e, "Failed to perform context menu action")
        false
    }
}

fun InputConnection?.safeGetSelectedText(flags: Int): CharSequence? {
    if (this == null) return null
    return try {
        getSelectedText(flags)
    } catch (e: Exception) {
        Timber.e(e, "Failed to get selected text")
        null
    }
}

fun InputConnection?.safeSetComposingText(text: CharSequence, newCursorPosition: Int): Boolean {
    if (this == null) return false
    return try {
        setComposingText(text, newCursorPosition)
    } catch (e: Exception) {
        Timber.e(e, "Failed to set composing text")
        false
    }
}

fun InputConnection?.safeFinishComposingText(): Boolean {
    if (this == null) return false
    return try {
        finishComposingText()
    } catch (e: Exception) {
        Timber.e(e, "Failed to finish composing text")
        false
    }
}
