package com.nextgen.keyboard.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.util.Log
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.nextgen.keyboard.data.model.Clip

@Database(
    entities = [Clip::class],
    version = 1,
    version = 3, // Incremented version
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class ClipboardDatabase : RoomDatabase() {
    abstract fun clipDao(): ClipDao

    companion object {
        const val DATABASE_NAME = "nextgen_keyboard_db"

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                try {
                    database.execSQL("ALTER TABLE clips ADD COLUMN is_encrypted INTEGER NOT NULL DEFAULT 0")
                    Log.d("ClipboardDB", "Migration 1->2 completed successfully.")
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
                    Log.d("ClipboardDB", "Migration 2->3 completed successfully.")
                } catch (e: Exception) {
                    Log.e("ClipboardDB", "Migration 2->3 failed", e)
                    throw e
                }
            }
        }
    }
}