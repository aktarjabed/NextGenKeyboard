package com.aktarjabed.nxtgenkeyboard.clipboard

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "clipboard",
    indices = [Index(value = ["text"], unique = true)]
)
data class ClipboardEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val text: String,
    val createdAt: Long = System.currentTimeMillis(),
    val pinned: Boolean = false
)