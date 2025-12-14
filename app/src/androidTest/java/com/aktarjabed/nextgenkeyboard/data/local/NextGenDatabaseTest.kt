package com.aktarjabed.nextgenkeyboard.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.aktarjabed.nextgenkeyboard.data.model.Clip
import kotlinx.coroutines.runBlocking
import net.sqlcipher.database.SupportFactory
import net.sqlcipher.database.SQLiteDatabase
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import com.google.common.truth.Truth.assertThat

@RunWith(AndroidJUnit4::class)
class NextGenDatabaseTest {

    private lateinit var db: NextGenDatabase
    private lateinit var clipboardDao: ClipboardDao
    private lateinit var learnedWordDao: LearnedWordDao
    private lateinit var context: Context

    @Before
    fun createDb() {
        context = ApplicationProvider.getApplicationContext()
        val passphrase = "test_passphrase".toByteArray()
        val factory = SupportFactory(passphrase)

        db = Room.inMemoryDatabaseBuilder(context, NextGenDatabase::class.java)
            .openHelperFactory(factory)
            .build()
        clipboardDao = db.clipboardDao()
        learnedWordDao = db.learnedWordDao()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun testLearnedWordInsertionAndRetrieval() = runBlocking {
        val word = LearnedWordEntity("kotlin", frequency = 5)
        learnedWordDao.insert(word)

        val retrieved = learnedWordDao.getWord("kotlin")
        assertThat(retrieved).isNotNull()
        assertThat(retrieved?.frequency).isEqualTo(5)
    }

    @Test
    fun testLearnedWordFrequencyIncrement() = runBlocking {
        val word = LearnedWordEntity("java", frequency = 1)
        learnedWordDao.insert(word)

        learnedWordDao.incrementFrequency("java")
        val retrieved = learnedWordDao.getWord("java")
        assertThat(retrieved?.frequency).isEqualTo(2)
    }

    @Test
    fun testClipboardInsertionAndRetrieval() = runBlocking {
        val clip = Clip(content = "Sensitive Data", isEncrypted = true)
        clipboardDao.insertClip(clip)

        val clips = clipboardDao.getAllClips()
        assertTrue(clips.any { it.content == "Sensitive Data" })
    }
}
