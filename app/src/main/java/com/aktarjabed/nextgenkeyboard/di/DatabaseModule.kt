package com.aktarjabed.nextgenkeyboard.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.aktarjabed.nextgenkeyboard.BuildConfig
import com.aktarjabed.nextgenkeyboard.data.local.ClipboardDao
import com.aktarjabed.nextgenkeyboard.data.local.ClipboardDatabase
import com.aktarjabed.nextgenkeyboard.data.local.LearnedWordDao
import com.aktarjabed.nextgenkeyboard.data.local.NextGenDatabase
import com.aktarjabed.nextgenkeyboard.util.SecurityUtils
import net.sqlcipher.database.SupportFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import timber.log.Timber
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideNextGenDatabase(
        @ApplicationContext context: Context
    ): NextGenDatabase {
        // Retrieve or generate the passphrase for SQLCipher
        val passphrase = SecurityUtils.getDatabasePassphrase(context)
        val factory = SupportFactory(passphrase)

        // Note: Reuse the old database name "clipboard_database" to attempt preserving data
        // even though we are upgrading the class to NextGenDatabase.
        // We will treat this as an upgrade from v3 to v4.
        return Room.databaseBuilder(
            context,
            NextGenDatabase::class.java,
            ClipboardDatabase.DATABASE_NAME // Reusing filename
        )
            .openHelperFactory(factory) // Enable SQLCipher encryption
            // Include old migrations to support upgrades from older versions
            .addMigrations(
                ClipboardDatabase.MIGRATION_1_2,
                ClipboardDatabase.MIGRATION_2_3,
                NextGenDatabase.MIGRATION_3_4
            )
            .addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    Timber.d("NextGenDatabase created successfully (Encrypted)")
                }

                override fun onOpen(db: SupportSQLiteDatabase) {
                    super.onOpen(db)
                    Timber.d("NextGenDatabase opened successfully (Encrypted)")
                }
            })
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideClipboardDao(database: NextGenDatabase): ClipboardDao {
        return database.clipboardDao()
    }

    @Provides
    @Singleton
    fun provideLearnedWordDao(database: NextGenDatabase): LearnedWordDao {
        return database.learnedWordDao()
    }
}
