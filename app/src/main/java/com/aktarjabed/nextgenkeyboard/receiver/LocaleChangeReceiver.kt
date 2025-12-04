package com.aktarjabed.nextgenkeyboard.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.aktarjabed.nextgenkeyboard.service.NextGenKeyboardService
import timber.log.Timber

class LocaleChangeReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_LOCALE_CHANGED) {
            Timber.d("Locale changed detected")

            // Notify the service if it's running (this is a simplified approach)
            // Ideally, the service observes configuration changes directly,
            // but this receiver can be used for background adjustments if needed.
        }
    }
}
