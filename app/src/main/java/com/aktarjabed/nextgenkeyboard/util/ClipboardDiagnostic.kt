package com.aktarjabed.nextgenkeyboard.util

import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import timber.log.Timber

class ClipboardDiagnostic(private val context: Context) {

    fun runFullDiagnostic(): String {
        return try {
            val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                ?: return "Clipboard Manager not available"

            val status = StringBuilder()
            status.append("Clipboard Diagnostic:\n")

            try {
                status.append("Has Primary Clip: ${clipboardManager.hasPrimaryClip()}\n")

                if (clipboardManager.hasPrimaryClip()) {
                     status.append("Primary Clip Description: ${clipboardManager.primaryClipDescription}\n")
                     status.append("Primary Clip Item Count: ${clipboardManager.primaryClip?.itemCount ?: 0}\n")
                }
            } catch (e: Exception) {
                status.append("Error accessing primary clip: ${e.message}\n")
                Timber.e(e, "Error accessing clipboard in diagnostic")
            }

            status.append("Read Permission: ${hasPermission("android.permission.READ_CLIPBOARD")}\n")
            // Writing usually doesn't need runtime permission for same app or standard IME, but keeping log
            // status.append("Write Permission: ${hasPermission("android.permission.WRITE_CLIPBOARD")}\n")

            status.toString()
        } catch (e: Exception) {
            Timber.e(e, "Error running clipboard diagnostic")
            "Clipboard Diagnostic Failed: ${e.message}"
        }
    }

    private fun hasPermission(permission: String): Boolean {
        return try {
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        } catch (e: Exception) {
            false
        }
    }
}
