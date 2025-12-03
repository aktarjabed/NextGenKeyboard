package com.aktarjabed.nextgenkeyboard

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.aktarjabed.nextgenkeyboard.data.repository.PreferencesRepository
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class NextGenKeyboardApp : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory
    @Inject lateinit var preferencesRepository: PreferencesRepository

    override fun onCreate() {
        super.onCreate()

        // Initialize Logging
        initializeLogging()

        // Initialize Firebase
        initializeFirebase()

        Timber.d("NextGenKeyboardApp initialized")
    }

    private fun initializeLogging() {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }

    private fun initializeFirebase() {
        try {
            FirebaseApp.initializeApp(this)

            // Check privacy preference for Crashlytics
            // Default behavior until pref is read: DISABLED (Privacy by default)
            FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(false)

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val isEnabled = preferencesRepository.isCrashReportingEnabled.first()
                    FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(isEnabled)
                    if (isEnabled) {
                        Timber.d("✅ Crash reporting enabled by user")
                    } else {
                        Timber.d("🔒 Crash reporting disabled by default/user")
                    }
                } catch (e: Exception) {
                    Timber.e(e, "Error reading crash reporting preference")
                }
            }

        } catch (e: Exception) {
            Timber.e(e, "Failed to initialize Firebase")
        }
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .setMinimumLoggingLevel(if (BuildConfig.DEBUG) android.util.Log.DEBUG else android.util.Log.ERROR)
            .build()
}
