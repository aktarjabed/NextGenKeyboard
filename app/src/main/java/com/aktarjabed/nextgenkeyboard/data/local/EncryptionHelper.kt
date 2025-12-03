package com.aktarjabed.nextgenkeyboard.data.local

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import timber.log.Timber

/**
 * Encryption helper template using AndroidX Security (MasterKey + AES-GCM).
 * Use these helpers to store secrets or per-field encrypted values.
 *
 * NOTE:
 * - This demonstrates using EncryptedSharedPreferences for storing a DB key or short secrets.
 * - For large content (clipboard bodies) consider encrypting fields manually with Cipher+Keystore or use SQLCipher.
 */

object EncryptionHelper {

    fun createMasterKey(context: Context): MasterKey {
        return MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
    }

    fun encryptedPrefsName(): String = "nextgen_keyboard_encrypted_prefs"

    fun getEncryptedSharedPrefs(context: Context): EncryptedSharedPreferences {
        val masterKey = createMasterKey(context)
        return EncryptedSharedPreferences.create(
            context,
            encryptedPrefsName(),
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        ) as EncryptedSharedPreferences
    }

    fun storeSecret(context: Context, key: String, value: String) {
        try {
            val prefs = getEncryptedSharedPrefs(context)
            prefs.edit().putString(key, value).apply()
        } catch (e: Exception) {
            Timber.e(e, "Failed to store secret")
        }
    }

    fun readSecret(context: Context, key: String): String? {
        return try {
            val prefs = getEncryptedSharedPrefs(context)
            prefs.getString(key, null)
        } catch (e: Exception) {
            Timber.e(e, "Failed to read secret")
            null
        }
    }
}