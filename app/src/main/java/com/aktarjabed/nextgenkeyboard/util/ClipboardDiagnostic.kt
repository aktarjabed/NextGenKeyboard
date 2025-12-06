package com.aktarjabed.nextgenkeyboard.util

import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

class ClipboardDiagnostic(private val context: Context) {

    fun runFullDiagnostic(): String {
        val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

        val status = StringBuilder()
        status.append("Clipboard Diagnostic:\n")
        status.append("Has Primary Clip: ${clipboardManager.hasPrimaryClip()}\n")

        if (clipboardManager.hasPrimaryClip()) {
             status.append("Primary Clip Description: ${clipboardManager.primaryClipDescription}\n")
             status.append("Primary Clip Item Count: ${clipboardManager.primaryClip?.itemCount ?: 0}\n")
        }

        status.append("Read Permission: ${hasPermission("android.permission.READ_CLIPBOARD")}\n")
        status.append("Write Permission: ${hasPermission("android.permission.WRITE_CLIPBOARD")}\n") // Not a real permission but checking for symmetry? No, usually unnecessary.

        return status.toString()
    }

    private fun hasPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }
}
