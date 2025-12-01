package com.nextgen.keyboard.security

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.security.SecureRandom
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SecureKeyManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val PREFS_NAME = "secure_keyboard_prefs"
        private const val KEY_DB_PASSPHRASE_V2 = "db_passphrase_v2"
        private const val KEY_MIGRATION_COMPLETED = "migration_completed"
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
                        .setUserAuthenticationRequired(false)
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

    suspend fun getDatabasePassphrase(): ByteArray = withContext(Dispatchers.IO) {
        try {
            // Check if migration is needed
            if (!encryptedPrefs.getBoolean(KEY_MIGRATION_COMPLETED, false)) {
                performSecureMigration()
            }

            // Get the properly stored passphrase
            val b64Passphrase = encryptedPrefs.getString(KEY_DB_PASSPHRASE_V2, null)
            if (b64Passphrase != null) {
                Timber.d("Using secure passphrase")
                return@withContext Base64.decode(b64Passphrase, Base64.NO_WRAP)
            }

            // Generate new passphrase for new installations
            val newPassphrase = generateSecurePassphrase()
            encryptedPrefs.edit()
                .putString(KEY_DB_PASSPHRASE_V2, newPassphrase)
                .putBoolean(KEY_MIGRATION_COMPLETED, true)
                .apply()

            Timber.d("Generated new secure passphrase")
            Base64.decode(newPassphrase, Base64.NO_WRAP)
        } catch (e: Exception) {
            Timber.e(e, "Error getting database passphrase")
            throw SecurityException("Failed to get database passphrase", e)
        }
    }

    private suspend fun performSecureMigration() = withContext(Dispatchers.IO) {
        try {
            // For existing users, generate a completely new database
            // This is the safest approach to fix the encoding issue
            Timber.w("Performing security migration - old data will be cleared")

            // Clear old insecure keys
            encryptedPrefs.edit()
                .remove("db_passphrase")
                .remove("db_passphrase_b64")
                .putBoolean(KEY_MIGRATION_COMPLETED, true)
                .apply()

            // Clear existing database to force recreation with proper passphrase
            val dbFile = context.getDatabasePath("keyboard_database")
            if (dbFile.exists()) {
                dbFile.delete()
                Timber.d("Cleared old database for security migration")
            }
        } catch (e: Exception) {
            Timber.e(e, "Error during security migration")
        }
    }

    private fun generateSecurePassphrase(): String {
        val random = SecureRandom()
        val bytes = ByteArray(32) // 256-bit key
        random.nextBytes(bytes)
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }
}