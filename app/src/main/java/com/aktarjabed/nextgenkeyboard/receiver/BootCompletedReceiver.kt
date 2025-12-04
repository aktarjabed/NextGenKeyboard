package com.aktarjabed.nextgenkeyboard.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.aktarjabed.nextgenkeyboard.data.repository.PreferencesRepository
import com.aktarjabed.nextgenkeyboard.service.NextGenKeyboardService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class BootCompletedReceiver : BroadcastReceiver() {

    @Inject
    lateinit var preferencesRepository: PreferencesRepository

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Timber.d("Device booted, restoring keyboard state")

            // In a real scenario, you might want to verify settings or pre-load data
            // Since this is a Direct Boot aware component (if configured), be careful with encrypted storage

            // For now, we just log. The service will start when the user taps a text field.
        }
    }
}
