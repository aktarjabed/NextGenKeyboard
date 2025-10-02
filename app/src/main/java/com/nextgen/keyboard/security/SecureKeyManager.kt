package com.nextgen.keyboard.security

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
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
        private const val KEY_DB_PASSPHRASE = "db_passphrase" // Legacy key
        private const val KEY_DB_PASSPHRASE_B64 = "db_passphrase_b64" // New key for correctly stored passphrase
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
     * Get or generate database passphrase.
     * This function is now backward-compatible.
     * - New installations will use the correctly Base64-decoded passphrase.
     * - Existing installations with the legacy key will continue to use the flawed
     *   UTF-8 conversion to avoid data loss. A proper migration would be needed
     *   to fix this for existing users, which is outside the scope of this fix.
     */
    fun getDatabasePassphrase(): ByteArray {
        return try {
            // 1. Prioritize the new, correctly stored key
            val b64Passphrase = encryptedPrefs.getString(KEY_DB_PASSPHRASE_B64, null)
            if (b64Passphrase != null) {
                Timber.d("Using new B64-decoded passphrase")
                return Base64.decode(b64Passphrase, Base64.NO_WRAP)
            }

            // 2. Fallback to the legacy key for existing users
            val legacyPassphrase = encryptedPrefs.getString(KEY_DB_PASSPHRASE, null)
            if (legacyPassphrase != null) {
                Timber.w("Using legacy passphrase with incorrect UTF-8 encoding for backward compatibility.")
                return legacyPassphrase.toByteArray(Charsets.UTF_8)
            }

            // 3. If no key exists (new user), generate a new one and store it correctly
            val newPassphrase = generateSecurePassphrase()
            encryptedPrefs.edit().putString(KEY_DB_PASSPHRASE_B64, newPassphrase).apply()
            Timber.d("Generated and stored new B64 database passphrase")
            return Base64.decode(newPassphrase, Base64.NO_WRAP)
        } catch (e: Exception) {
            Timber.e(e, "Error getting database passphrase")
            throw SecurityException("Failed to get database passphrase", e)
        }
    }

    private fun generateSecurePassphrase(): String {
        val random = java.security.SecureRandom()
        val bytes = ByteArray(32) // 256-bit key
        random.nextBytes(bytes)
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
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