package com.nextgen.keyboard.security

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class SecureKeyManagerTest {

    private lateinit var context: Context
    private lateinit var secureKeyManager: SecureKeyManager

    // Replicating constants to access them in test
    private val PREFS_NAME = "secure_keyboard_prefs"
    private val KEY_DB_PASSPHRASE = "db_passphrase"
    private val KEY_DB_PASSPHRASE_B64 = "db_passphrase_b64"

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        // Clear preferences before each test
        getEncryptedPrefs().edit().clear().commit()
        secureKeyManager = SecureKeyManager(context)
    }

    @Test
    fun `getDatabasePassphrase for new user generates new b64 key`() {
        // When
        val passphrase = secureKeyManager.getDatabasePassphrase()

        // Then
        val prefs = getEncryptedPrefs()
        val b64Passphrase = prefs.getString(KEY_DB_PASSPHRASE_B64, null)

        assertNotNull(b64Passphrase)
        assertNull(prefs.getString(KEY_DB_PASSPHRASE, null))
        assertArrayEquals(passphrase, Base64.decode(b64Passphrase, Base64.NO_WRAP))
    }

    @Test
    fun `getDatabasePassphrase for existing user with legacy key uses legacy key`() {
        // Given
        val legacyPassphraseString = "legacy-passphrase"
        val prefs = getEncryptedPrefs()
        prefs.edit().putString(KEY_DB_PASSPHRASE, legacyPassphraseString).commit()

        // When
        val passphrase = secureKeyManager.getDatabasePassphrase()

        // Then
        assertArrayEquals(passphrase, legacyPassphraseString.toByteArray(Charsets.UTF_8))
        // Ensure new key was not created
        assertNull(prefs.getString(KEY_DB_PASSPHRASE_B64, null))
    }

    @Test
    fun `getDatabasePassphrase for user with new key uses new key`() {
        // Given
        val newPassphraseString = Base64.encodeToString("new-passphrase".toByteArray(), Base64.NO_WRAP)
        val prefs = getEncryptedPrefs()
        prefs.edit().putString(KEY_DB_PASSPHRASE_B64, newPassphraseString).commit()

        // When
        val passphrase = secureKeyManager.getDatabasePassphrase()

        // Then
        assertArrayEquals(passphrase, Base64.decode(newPassphraseString, Base64.NO_WRAP))
    }

    @Test
    fun `getDatabasePassphrase prioritizes new key over legacy key`() {
        // Given
        val legacyPassphraseString = "legacy-passphrase"
        val newPassphraseString = Base64.encodeToString("new-passphrase".toByteArray(), Base64.NO_WRAP)
        val prefs = getEncryptedPrefs()
        prefs.edit()
            .putString(KEY_DB_PASSPHRASE, legacyPassphraseString)
            .putString(KEY_DB_PASSPHRASE_B64, newPassphraseString)
            .commit()

        // When
        val passphrase = secureKeyManager.getDatabasePassphrase()

        // Then
        assertArrayEquals(passphrase, Base64.decode(newPassphraseString, Base64.NO_WRAP))
    }

    // Helper to get EncryptedSharedPreferences instance for test setup
    private fun getEncryptedPrefs(): EncryptedSharedPreferences {
        val masterKey = MasterKey.Builder(context)
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

        return EncryptedSharedPreferences.create(
            context,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }
}