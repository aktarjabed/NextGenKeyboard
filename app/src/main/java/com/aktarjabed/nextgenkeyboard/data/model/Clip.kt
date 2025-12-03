package com.aktarjabed.nextgenkeyboard.data.model

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "clips")
data class Clip(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isPinned: Boolean = false,

    // New fields for v2 and v3
    @ColumnInfo(name = "is_encrypted", defaultValue = "0")
    val isEncrypted: Boolean = false,
    @ColumnInfo(name = "category", defaultValue = "general")
    val category: String = "general",

    // New field for plan compliance
    @ColumnInfo(name = "source", defaultValue = "keyboard")
    val source: String = "keyboard"
) : Parcelable
