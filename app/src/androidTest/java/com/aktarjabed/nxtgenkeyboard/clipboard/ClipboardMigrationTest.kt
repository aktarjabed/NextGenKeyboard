package com.aktarjabed.nxtgenkeyboard.clipboard

import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ClipboardMigrationTest {

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java
    )

    @Test
    fun migrate1To2_deduplicatesClipboardAndUserWord() {
        // Create the v1 database from the exported schema (1.json)
        helper.createDatabase("test-db", 1).apply {
            execSQL("INSERT INTO clipboard (text, createdAt, pinned) VALUES ('Hello', 1, 0)")
            execSQL("INSERT INTO clipboard (text, createdAt, pinned) VALUES ('Hello', 2, 0)")
            execSQL("INSERT INTO clipboard (text, createdAt, pinned) VALUES ('World', 3, 0)")

            execSQL("INSERT INTO user_word (lang, word) VALUES ('en', 'test')")
            execSQL("INSERT INTO user_word (lang, word) VALUES ('en', 'test')")
            close()
        }

        // Run migration to v2
        val db = helper.runMigrationsAndValidate("test-db", 2, true, AppDatabase.MIGRATION_1_2)

        val clipboardCursor = db.query("SELECT COUNT(*) FROM clipboard")
        clipboardCursor.moveToFirst()
        assertEquals(2, clipboardCursor.getInt(0))
        clipboardCursor.close()

        val userWordCursor = db.query("SELECT COUNT(*) FROM user_word")
        userWordCursor.moveToFirst()
        assertEquals(1, userWordCursor.getInt(0))
        userWordCursor.close()

        db.close()
    }
}