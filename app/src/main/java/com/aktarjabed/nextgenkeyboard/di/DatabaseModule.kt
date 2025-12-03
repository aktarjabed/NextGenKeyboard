package com.aktarjabed.nextgenkeyboard.di

import android.content.Context
import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteDatabase
import com.aktarjabed.nextgenkeyboard.BuildConfig
import com.aktarjabed.nextgenkeyboard.data.local.ClipboardDao
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.aktarjabed.nextgenkeyboard.BuildConfig
import com.aktarjabed.nextgenkeyboard.data.local.ClipDao
import com.aktarjabed.nextgenkeyboard.data.local.ClipboardDatabase
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
    fun provideClipboardDatabase(
        @ApplicationContext context: Context
    ): ClipboardDatabase {
        val builder = Room.databaseBuilder(
        return Room.databaseBuilder(
            context,
            ClipboardDatabase::class.java,
            ClipboardDatabase.DATABASE_NAME
        )
            .addMigrations(ClipboardDatabase.MIGRATION_1_2, ClipboardDatabase.MIGRATION_2_3)
            .addCallback(object : androidx.room.RoomDatabase.Callback() {
            .fallbackToDestructiveMigration()
            // Add the migrations from the ClipboardDatabase companion object
            .addMigrations(ClipboardDatabase.MIGRATION_1_2, ClipboardDatabase.MIGRATION_2_3)
            .addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    Timber.d("Database created successfully")
                }

                override fun onOpen(db: SupportSQLiteDatabase) {
                    super.onOpen(db)
                    Timber.d("Database opened successfully")
                }
            })

        // Removed fallbackToDestructiveMigration to prevent data loss

        return builder.build()
            .apply {
                // Use fallback migration only in debug builds
                if (BuildConfig.DEBUG) {
                    fallbackToDestructiveMigration()
                }
            }
            .build()
    }

    @Provides
    @Singleton
    fun provideClipboardDao(database: ClipboardDatabase): ClipboardDao {
        return database.clipboardDao()
    fun provideClipDao(database: ClipboardDatabase): ClipDao {
        return database.clipDao()
    }
}