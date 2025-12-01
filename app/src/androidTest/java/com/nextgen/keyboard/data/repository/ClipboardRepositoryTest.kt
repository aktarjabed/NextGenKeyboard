package com.nextgen.keyboard.data.repository

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.nextgen.keyboard.data.local.ClipboardDatabase
import com.nextgen.keyboard.data.model.ClipboardEntity
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import javax.inject.Inject

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class ClipboardRepositoryTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    // Inject the repository to be tested. Hilt will provide it with the in-memory database.
    @Inject
    lateinit var repository: ClipboardRepository

    // Inject the database itself to allow for cleanup.
    @Inject
    lateinit var db: ClipboardDatabase

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @After
    @Throws(IOException::class)
    fun tearDown() {
        db.clearAllTables()
        db.close()
    }

    @Test
    fun saveClip_and_getRecentClips() = runBlocking {
        // Arrange
        val content = "test clip"

        // Act
        repository.saveClip(content)
        val allClips = repository.getRecentClips().first()

        // Assert
        assertThat(allClips).isNotEmpty()
        assertThat(allClips[0].content).isEqualTo(content)
    }

    @Test
    fun saveClip_withBlankContent_fails() = runBlocking {
        // Act
        val result = repository.saveClip("   ")

        // Assert
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun deleteClip_removesFromDatabase() = runBlocking {
        // Given
        repository.saveClip("to be deleted")
        val clipToDelete = repository.getRecentClips().first().first()

        // When
        repository.deleteClip(clipToDelete)
        val clips = repository.getRecentClips().first()

        // Then
        assertThat(clips).isEmpty()
    }

    @Test
    fun updateClip_pinsClip() = runBlocking {
        // Given
        repository.saveClip("to be pinned")
        val clipToPin = repository.getRecentClips().first().first()

        // When
        val pinnedClip = clipToPin.copy(isPinned = true)
        repository.updateClip(pinnedClip)

        // Then
        val pinnedClips = repository.getPinnedClips().first()
        val recentClips = repository.getRecentClips().first()
        assertThat(pinnedClips).hasSize(1)
        assertThat(pinnedClips[0].isPinned).isTrue()
        assertThat(recentClips).isEmpty()
    }
}