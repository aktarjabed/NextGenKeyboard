package com.aktarjabed.nextgenkeyboard.di

import com.aktarjabed.nextgenkeyboard.feature.keyboard.UtilityKey
import com.aktarjabed.nextgenkeyboard.feature.keyboard.UtilityKeyAction
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object KeyboardModule {

    @Provides
    @Singleton
    fun provideUtilityKeys(): List<UtilityKey> = listOf(
        UtilityKey("select_all", "Select All", UtilityKeyAction.SELECT_ALL),
        UtilityKey("copy", "Copy", UtilityKeyAction.COPY),
        UtilityKey("cut", "Cut", UtilityKeyAction.CUT),
        UtilityKey("paste", "Paste", UtilityKeyAction.PASTE_CLIPBOARD),
        UtilityKey("undo", "Undo", UtilityKeyAction.UNDO_LAST_DELETE),
        UtilityKey("date", "Date", UtilityKeyAction.INSERT_DATE)
    )
}
