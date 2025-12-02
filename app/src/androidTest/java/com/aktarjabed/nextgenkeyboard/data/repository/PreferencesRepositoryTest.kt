package com.aktarjabed.nextgenkeyboard.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

private val Context.testDataStore: DataStore<Preferences> by preferencesDataStore(name = "test_keyboard_preferences")

@RunWith(AndroidJUnit4::class)
class PreferencesRepositoryTest {

    private lateinit var context: Context
    private lateinit var repository: PreferencesRepository

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        // Manually create the repository to control the DataStore instance for testing
        repository = PreferencesRepository(context)

        // Clear preferences before each test to ensure isolation
        runBlocking {
            context.testDataStore.edit { it.clear() }
        }
    }

    @Test
    fun setDarkMode_retrievesCorrectly() = runBlocking {
        // Arrange
        val isDark = false

        // Act
        repository.setDarkMode(isDark)
        val retrievedValue = repository.isDarkMode.first()

        // Assert
        assertThat(retrievedValue).isEqualTo(isDark)
    }

    @Test
    fun setClipboardEnabled_updatesState() = runBlocking {
        // Arrange
        val initialState = repository.isClipboardEnabled.first() // Default is true

        // Act
        repository.setClipboardEnabled(!initialState)
        val newState = repository.isClipboardEnabled.first()

        // Assert
        assertThat(newState).isNotEqualTo(initialState)
        assertThat(newState).isFalse()
    }

    @Test
    fun setAutoDeleteDays_savesCorrectValue() = runBlocking {
        // Arrange
        val retentionDays = 60

        // Act
        repository.setAutoDeleteDays(retentionDays)
        val retrieved = repository.autoDeleteDays.first()

        // Assert
        assertThat(retrieved).isEqualTo(retentionDays)
    }

    @Test
    fun setAutoDeleteDays_coercesValue() = runBlocking {
        // Test lower bound
        repository.setAutoDeleteDays(0)
        var retrieved = repository.autoDeleteDays.first()
        assertThat(retrieved).isEqualTo(1)

        // Test upper bound
        repository.setAutoDeleteDays(400)
        retrieved = repository.autoDeleteDays.first()
        assertThat(retrieved).isEqualTo(365)
    }

    @Test
    fun setCurrentLanguage_retrievesCorrectly() = runBlocking {
        // Arrange
        val language = "fr_FR"

        // Act
        repository.setCurrentLanguage(language)
        val retrieved = repository.currentLanguage.first()

        // Assert
        assertThat(retrieved).isEqualTo(language)
    }
}