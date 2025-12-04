package com.nextgen.keyboard.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.nextgen.keyboard.data.model.Clip

@Database(
    entities = [Clip::class],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class ClipboardDatabase : RoomDatabase() {
    abstract fun clipDao(): ClipDao

    companion object {
        const val DATABASE_NAME = "nextgen_keyboard_db"
    }
}