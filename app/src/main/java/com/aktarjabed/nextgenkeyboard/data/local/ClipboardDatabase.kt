package com.aktarjabed.nextgenkeyboard.data.local

import android.util.Log
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.aktarjabed.nextgenkeyboard.data.models.ClipboardEntity

@Database(
    entities = [ClipboardEntity::class],
    version = 3, // Version updated to include both migrations
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class ClipboardDatabase : RoomDatabase() {

    // Corrected DAO abstract function
    abstract fun clipboardDao(): ClipboardDao

    companion object {
        const val DATABASE_NAME = "clipboard_database"

        // Migration from v1 to v2: Adds the 'is_encrypted' column
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

        // Migration from v2 to v3: Adds the 'category' column
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                try {
                    database.execSQL("ALTER TABLE clips ADD COLUMN category TEXT DEFAULT 'general' NOT NULL")
                    Log.d("ClipboardDB", "Migration 2->3 completed successfully: Added 'category' column.")
                } catch (e: Exception) {
                    Log.e("ClipboardDB", "Migration 2->3 failed", e)
                    throw e
                }
            }
        }
    }
}