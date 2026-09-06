package com.aktarjabed.nxtgenkeyboard.user

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "user_word",
    indices = [Index(value = ["lang", "word"], unique = true)]
)
data class UserWordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val lang: String,
    val word: String
)
