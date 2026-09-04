package com.aktarjabed.nxtgenkeyboard.user

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.OnConflictStrategy

@Dao
interface UserWordDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(item: UserWordEntity)

    @Query("SELECT word FROM user_word WHERE lang = :lang")
    suspend fun words(lang: String): List<String>
}