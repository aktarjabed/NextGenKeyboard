package com.aktarjabed.nextgenkeyboard.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

private val Context.testDataStore: DataStore<Preferences> by preferencesDataStore(name = "keyboard_preferences")

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class PreferencesRepositoryTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var repository: PreferencesRepository

    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        hiltRule.inject()
        runBlocking {
            context.testDataStore.edit { it.clear() }
        }
    }

    @After
    fun tearDown() = runBlocking {
        context.testDataStore.edit { it.clear() }
    }

    @Test
    fun setDarkMode_retrievesCorrectly() = runBlocking {
        val isDark = true
        repository.setDarkMode(isDark)
        val retrievedValue = repository.isDarkMode.first()
        assertThat(retrievedValue).isEqualTo(isDark)
    }

    @Test
    fun setClipboardEnabled_updatesState() = runBlocking {
        val initialState = repository.isClipboardEnabled.first()
        repository.setClipboardEnabled(!initialState)
        val newState = repository.isClipboardEnabled.first()
        assertThat(newState).isNotEqualTo(initialState)
    }

    @Test
    fun setAutoDeleteDays_savesCorrectValue() = runBlocking {
        val retentionDays = 90
        repository.setAutoDeleteDays(retentionDays)
        val retrieved = repository.autoDeleteDays.first()
        assertThat(retrieved).isEqualTo(retentionDays)
    }

    @Test
    fun setMaxClipboardItems_coercesValue() = runBlocking {
        repository.setMaxClipboardItems(10)
        var retrieved = repository.maxClipboardItems.first()
        assertThat(retrieved).isEqualTo(50)

        repository.setMaxClipboardItems(6000)
        retrieved = repository.maxClipboardItems.first()
        assertThat(retrieved).isEqualTo(2000)
    }

    @Test
    fun setCurrentLanguage_retrievesCorrectly() = runBlocking {
        val language = "fr_FR"
        repository.setCurrentLanguage(language)
        val retrieved = repository.currentLanguage.first()
        assertThat(retrieved).isEqualTo(language)
    }
}