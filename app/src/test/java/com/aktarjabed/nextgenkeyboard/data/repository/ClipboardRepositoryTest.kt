package com.aktarjabed.nextgenkeyboard.data.repository

import com.aktarjabed.nextgenkeyboard.data.local.ClipboardDao
import com.aktarjabed.nextgenkeyboard.data.models.ClipboardEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class ClipboardRepositoryTest {

    private lateinit var clipboardDao: ClipboardDao
    private lateinit var repository: ClipboardRepository

    @Before
    fun setup() {
        clipboardDao = mock()
        repository = ClipboardRepository(clipboardDao)
    }

    @Test
    fun `getClipboardHistory maps entities to items`() = runBlocking {
        val entity = ClipboardEntity(id = 1, content = "test", timestamp = 100L, isEncrypted = true)
        whenever(clipboardDao.getRecentClips()).thenReturn(flowOf(listOf(entity)))

        val result = repository.getClipboardHistory().first()

        assertEquals(1, result.size)
        assertEquals("1", result[0].id)
        assertEquals("test", result[0].text)
        assertEquals(true, result[0].isSensitive)
    }

    @Test
    fun `saveClip inserts entity into dao`() = runBlocking {
        repository.saveClip("content", true)

        verify(clipboardDao).insert(any())
    }

    @Test
    fun `clearAllClips calls delete on dao`() = runBlocking {
        repository.clearAllClips()

        verify(clipboardDao).deleteAllClips()
    }
}