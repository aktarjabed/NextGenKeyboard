package com.aktarjabed.nextgenkeyboard.data.repository

import android.content.Context
import com.aktarjabed.nextgenkeyboard.data.db.ClipboardDatabase
import com.aktarjabed.nextgenkeyboard.data.local.ClipboardDao
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations

class ClipboardRepositoryTest {

    @Mock
    lateinit var context: Context

    @Mock
    lateinit var database: ClipboardDatabase

    @Mock
    lateinit var clipboardDao: ClipboardDao

    private lateinit var clipboardRepository: ClipboardRepository

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        // Mock the database to return our mocked DAO
        `when`(database.clipboardDao()).thenReturn(clipboardDao)

        clipboardRepository = ClipboardRepository(context, database)
    }

    @Test
    fun `saveClip should block OTP`() = runBlocking {
        val otp = "123456"
        val result = clipboardRepository.saveClip(otp)
        assertTrue(result.isFailure)
        assertEquals("Potential sensitive data detected", result.exceptionOrNull()?.message)
    }

    @Test
    fun `saveClip should allow normal numbers`() = runBlocking {
        val number = "12345" // 5 digits
        `when`(clipboardDao.insertClip(any())).thenReturn(1L)
        `when`(clipboardDao.getUnpinnedCount()).thenReturn(0)

        val result = clipboardRepository.saveClip(number)
        assertTrue(result.isSuccess)
    }

    @Test
    fun `saveClip should block Credit Card`() = runBlocking {
        val cc = "1234 5678 1234 5678" // 16 digits with spaces
        val result = clipboardRepository.saveClip(cc)
        assertTrue(result.isFailure)
    }

    @Test
    fun `saveClip should block Password keyword`() = runBlocking {
        val text = "My password is 123"
        val result = clipboardRepository.saveClip(text)
        assertTrue(result.isFailure)
    }

    @Test
    fun `saveClip should succeed even if cleanup fails`() = runBlocking {
        val content = "Hello World"
        `when`(clipboardDao.insertClip(any())).thenReturn(1L)
        `when`(clipboardDao.getUnpinnedCount()).thenThrow(RuntimeException("DB Error"))

        val result = clipboardRepository.saveClip(content)

        // Should succeed despite cleanup failure
        assertTrue(result.isSuccess)
        verify(clipboardDao).insertClip(any())
    }
}
