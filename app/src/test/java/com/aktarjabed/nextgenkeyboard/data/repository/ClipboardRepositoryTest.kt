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
import com.aktarjabed.nextgenkeyboard.data.local.ClipDao
import com.aktarjabed.nextgenkeyboard.data.model.Clip
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ClipboardRepositoryTest {

    private lateinit var clipboardRepository: ClipboardRepository
    private lateinit var clipDao: FakeClipDao

    @Before
    fun setup() {
        clipDao = FakeClipDao()
        clipboardRepository = ClipboardRepository(clipDao)
    }

    @Test
    fun `saveClip should save non-sensitive content`() = runBlocking {
        val content = "This is a normal clip"
        val result = clipboardRepository.saveClip(content)
        assertTrue(result.isSuccess)
        assertEquals(1, clipDao.getClipCount())
    }

    @Test
    fun `saveClip should not save OTP`() = runBlocking {
        val content = "123456"
        val result = clipboardRepository.saveClip(content)
        assertTrue(result.isFailure)
        assertEquals(0, clipDao.getClipCount())
    }

    @Test
    fun `saveClip should not save credit card number`() = runBlocking {
        val content = "1234567890123456"
        val result = clipboardRepository.saveClip(content)
        assertTrue(result.isFailure)
        assertEquals(0, clipDao.getClipCount())
    }

    @Test
    fun `saveClip should not save password`() = runBlocking {
        val content = "my password is very secure"
        val result = clipboardRepository.saveClip(content)
        assertTrue(result.isFailure)
        assertEquals(0, clipDao.getClipCount())
    }

    @Test
    fun `saveClip should not save blank content`() = runBlocking {
        val content = " "
        val result = clipboardRepository.saveClip(content)
        assertTrue(result.isFailure)
        assertEquals(0, clipDao.getClipCount())
    }

    @Test
    fun `performAutoCleanup should delete oldest clips`() = runBlocking {
        for (i in 1..510) {
            clipDao.insertClip(Clip(content = "clip $i"))
        }
        assertEquals(510, clipDao.getClipCount())

        clipboardRepository.performManualCleanup()

        assertEquals(500, clipDao.getClipCount())
    }
}

class FakeClipDao : ClipDao {
    private val clips = mutableListOf<Clip>()

    override fun getPinnedClips(): Flow<List<Clip>> = flow { emit(clips.filter { it.isPinned }) }
    override fun getRecentClips(): Flow<List<Clip>> = flow { emit(clips.filter { !it.isPinned }) }
    override suspend fun searchClips(query: String): List<Clip> = clips.filter { it.content.contains(query) }
    override suspend fun insertClip(clip: Clip): Long {
        clips.add(clip.copy(id = (clips.size + 1).toLong()))
        return clips.size.toLong()
    }
    override suspend fun updateClip(clip: Clip) {
        val index = clips.indexOfFirst { it.id == clip.id }
        if (index != -1) {
            clips[index] = clip
        }
    }
    override suspend fun deleteClip(clip: Clip) {
        clips.remove(clip)
    }
    override suspend fun clearUnpinnedClips() {
        clips.removeAll { !it.isPinned }
    }
    override suspend fun deleteAllClips() {
        clips.clear()
    }
    override suspend fun getClipCount(): Int = clips.size
    override suspend fun deleteOldestUnpinned(limit: Int) {
        val unpinned = clips.filter { !it.isPinned }.sortedBy { it.timestamp }
        val toDelete = unpinned.take(limit)
        clips.removeAll(toDelete)
    }
    override suspend fun deleteOlderThan(beforeTimestamp: Long) {
        clips.removeAll { !it.isPinned && it.timestamp < beforeTimestamp }
    }
    override suspend fun getUnpinnedCount(): Int = clips.count { !it.isPinned }
}