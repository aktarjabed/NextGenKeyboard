package com.nextgen.keyboard.di

import android.content.Context
import androidx.room.Room
import com.nextgen.keyboard.data.local.ClipDao
import com.nextgen.keyboard.data.local.ClipboardDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideClipboardDatabase(
        @ApplicationContext context: Context
    ): ClipboardDatabase {
        return Room.databaseBuilder(
            context,
            ClipboardDatabase::class.java,
            ClipboardDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideClipDao(database: ClipboardDatabase): ClipDao {
        return database.clipDao()
    }
}