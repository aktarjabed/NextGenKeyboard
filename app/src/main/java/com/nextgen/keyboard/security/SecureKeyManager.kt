package com.nextgen.keyboard.security

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SecureKeyManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val PREFS_NAME = "secure_keyboard_prefs"
        private const val KEY_DB_PASSPHRASE = "db_passphrase"
    }

    private val masterKey: MasterKey by lazy {
        try {
            MasterKey.Builder(context)
                .setKeyGenParameterSpec(
                    KeyGenParameterSpec.Builder(
                        MasterKey.DEFAULT_MASTER_KEY_ALIAS,
                        KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                    )
                        .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                        .setKeySize(256)
                        .build()
                )
                .build()
        } catch (e: Exception) {
            Timber.e(e, "Error creating master key")
            throw SecurityException("Failed to create master key", e)
        }
    }

    private val encryptedPrefs by lazy {
        try {
            EncryptedSharedPreferences.create(
                context,
                PREFS_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            Timber.e(e, "Error creating encrypted preferences")
            throw SecurityException("Failed to create encrypted preferences", e)
        }
    }

    /**
     * Get or generate database passphrase
     * Stored securely in EncryptedSharedPreferences backed by Android Keystore
     */
    fun getDatabasePassphrase(): ByteArray {
        return try {
            var passphrase = encryptedPrefs.getString(KEY_DB_PASSPHRASE, null)

            if (passphrase == null) {
                // Generate new secure passphrase
                passphrase = generateSecurePassphrase()
                encryptedPrefs.edit().putString(KEY_DB_PASSPHRASE, passphrase).apply()
                Timber.d("Generated new database passphrase")
            }

            passphrase.toByteArray(Charsets.UTF_8)
        } catch (e: Exception) {
            Timber.e(e, "Error getting database passphrase")
            throw SecurityException("Failed to get database passphrase", e)
        }
    }

    private fun generateSecurePassphrase(): String {
        val random = java.security.SecureRandom()
        val bytes = ByteArray(32) // 256-bit key
        random.nextBytes(bytes)
        return android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)
    }

    /**
     * Clear all secure data (useful for app reset)
     */
    fun clearSecureData() {
        try {
            encryptedPrefs.edit().clear().apply()
            Timber.d("Cleared all secure data")
        } catch (e: Exception) {
            Timber.e(e, "Error clearing secure data")
        }
    }
}