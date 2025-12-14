package com.aktarjabed.nextgenkeyboard.data.local

import android.util.Log
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.aktarjabed.nextgenkeyboard.data.model.Clip

@Database(
    entities = [Clip::class, LearnedWordEntity::class],
    version = 4,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class NextGenDatabase : RoomDatabase() {

    abstract fun clipboardDao(): ClipboardDao
    abstract fun learnedWordDao(): LearnedWordDao

    companion object {
        const val DATABASE_NAME = "nextgen_keyboard_db"

        // Migration from old ClipboardDatabase (v3) to NextGenDatabase (v4)
        // Since we are changing the DB name/file, this migration logic is mainly for
        // within the same file if we were reusing it. But assuming we use a NEW file,
        // we start fresh or need manual data migration.
        // However, if we just RENAME the class but keep the file name or just use the new file,
        // we'll start at version 4.

        // For simplicity in this consolidation plan, we will define the migration
        // assuming we might want to attach the old DB or just start fresh for LearnedWords.

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                try {
                    // Create the learned_words table
                    database.execSQL("""
                        CREATE TABLE IF NOT EXISTS `learned_words` (
                            `word` TEXT NOT NULL,
                            `frequency` INTEGER NOT NULL DEFAULT 1,
                            `lastUsedTimestamp` INTEGER NOT NULL,
                            PRIMARY KEY(`word`)
                        )
                    """.trimIndent())
                    Log.d("NextGenDatabase", "Migration 3->4: Added learned_words table.")
                } catch (e: Exception) {
                    Log.e("NextGenDatabase", "Migration 3->4 failed", e)
                    throw e
                }
            }
        }
    }
}
