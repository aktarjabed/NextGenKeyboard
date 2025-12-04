package com.nextgen.keyboard.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.nextgen.keyboard.BuildConfig
import com.nextgen.keyboard.data.local.ClipDao
import com.nextgen.keyboard.data.local.ClipboardDatabase
import com.nextgen.keyboard.security.SecureKeyManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import net.sqlcipher.database.SupportFactory
import timber.log.Timber
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    // Migration from version 1 to 2 (example: adding isFavorite column)
    private val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            try {
                // Add new column with default value
                db.execSQL("ALTER TABLE clips ADD COLUMN is_favorite INTEGER NOT NULL DEFAULT 0")
                Timber.d("Migration 1->2 completed successfully")
            } catch (e: Exception) {
                Timber.e(e, "Migration 1->2 failed")
                throw e
            }
        }
    }

    // Migration from version 2 to 3 (example: adding category field)
    private val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            try {
                db.execSQL("ALTER TABLE clips ADD COLUMN category TEXT NOT NULL DEFAULT 'general'")
                Timber.d("Migration 2->3 completed successfully")
            } catch (e: Exception) {
                Timber.e(e, "Migration 2->3 failed")
                throw e
            }
        }
    }

    @Provides
    @Singleton
    fun provideSecureKeyManager(
        @ApplicationContext context: Context
    ): SecureKeyManager {
        return SecureKeyManager(context)
    }

    @Provides
    @Singleton
    fun provideClipboardDatabase(
        @ApplicationContext context: Context,
        secureKeyManager: SecureKeyManager
    ): ClipboardDatabase {
        val passphrase = secureKeyManager.getDatabasePassphrase()
        val factory = SupportFactory(passphrase)

        val builder = Room.databaseBuilder(
            context,
            ClipboardDatabase::class.java,
            ClipboardDatabase.DATABASE_NAME
        )
            // ✅ Enable encryption
            .openHelperFactory(factory)

            // ✅ ADD ALL MIGRATIONS HERE (never use fallbackToDestructiveMigration in production)
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3)

            // ✅ Add callback for database initialization
            .addCallback(object : androidx.room.RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    Timber.d("Database created successfully")
                }

                override fun onOpen(db: SupportSQLiteDatabase) {
                    super.onOpen(db)
                    Timber.d("Database opened successfully")
                }
            })

        // ✅ ONLY use fallback in debug builds
        if (BuildConfig.DEBUG) {
            builder.fallbackToDestructiveMigration()
        }

        return builder.build()
    }

    @Provides
    @Singleton
    fun provideClipDao(database: ClipboardDatabase): ClipDao {
        return database.clipDao()
    }
}