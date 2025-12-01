package com.nextgen.keyboard.data.repository

import com.nextgen.keyboard.data.local.ClipDao
import com.nextgen.keyboard.data.model.Clip
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