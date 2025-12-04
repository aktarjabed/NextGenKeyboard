package com.aktarjabed.nextgenkeyboard.data.local

import android.util.Log
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.aktarjabed.nextgenkeyboard.data.model.Clip

@Database(
    entities = [Clip::class],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class ClipboardDatabase : RoomDatabase() {

    abstract fun clipboardDao(): ClipboardDao

    companion object {
        const val DATABASE_NAME = "clipboard_database"

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                try {
                    database.execSQL("ALTER TABLE clips ADD COLUMN is_encrypted INTEGER NOT NULL DEFAULT 0")
                    Log.d("ClipboardDB", "Migration 1->2 completed successfully: Added 'is_encrypted' column.")
                } catch (e: Exception) {
                    Log.e("ClipboardDB", "Migration 1->2 failed", e)
                    throw e
                }
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                try {
                    database.execSQL("ALTER TABLE clips ADD COLUMN category TEXT NOT NULL DEFAULT 'general'")
                    Log.d("ClipboardDB", "Migration 2->3 completed successfully: Added 'category' column.")
                } catch (e: Exception) {
                    Log.e("ClipboardDB", "Migration 2->3 failed", e)
                    throw e
                }
            }
        }
    }
}
