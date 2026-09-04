package com.aktarjabed.nxtgenkeyboard.clipboard

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.aktarjabed.nxtgenkeyboard.user.UserWordDao
import com.aktarjabed.nxtgenkeyboard.user.UserWordEntity

@Database(
    entities = [
        ClipboardEntity::class,
        UserWordEntity::class
    ],
    version = 2,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun clipboardDao(): ClipboardDao
    abstract fun userWordDao(): UserWordDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Deduplicate clipboard: keep newest (max id) per text
                db.execSQL(
                    """
                    DELETE FROM clipboard WHERE id NOT IN (
                        SELECT MAX(id) FROM clipboard GROUP BY text
                    )
                    """.trimIndent()
                )
                // Deduplicate user_word: keep newest per (lang, word)
                db.execSQL(
                    """
                    DELETE FROM user_word WHERE id NOT IN (
                        SELECT MAX(id) FROM user_word GROUP BY lang, word
                    )
                    """.trimIndent()
                )
                // Create unique indexes
                db.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS index_clipboard_text ON clipboard(text)"
                )
                db.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS index_user_word_lang_word ON user_word(lang, word)"
                )
            }
        }

        fun getInstance(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "nxtgen.db"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                    .also { instance = it }
            }
    }
}