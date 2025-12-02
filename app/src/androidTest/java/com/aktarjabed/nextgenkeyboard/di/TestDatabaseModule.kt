package com.aktarjabed.nextgenkeyboard.di

import android.content.Context
import androidx.room.Room
import com.aktarjabed.nextgenkeyboard.data.local.ClipboardDao
import com.aktarjabed.nextgenkeyboard.data.local.ClipboardDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Singleton

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [DatabaseModule::class]
)
object TestDatabaseModule {

    @Provides
    @Singleton
    fun provideInMemoryDb(@ApplicationContext context: Context): ClipboardDatabase {
        return Room.inMemoryDatabaseBuilder(
            context,
            ClipboardDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()
    }

    @Provides
    @Singleton
    fun provideTestDao(db: ClipboardDatabase): ClipboardDao {
        return db.clipboardDao()
    }
}