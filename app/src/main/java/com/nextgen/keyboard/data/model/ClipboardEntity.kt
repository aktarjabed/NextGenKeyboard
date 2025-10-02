package com.nextgen.keyboard.data.model

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "clips") // Corrected table name
data class ClipboardEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "content")
    val content: String,

    @ColumnInfo(name = "timestamp")
    val timestamp: Long = System.currentTimeMillis(),

    // Corrected property name to match original 'Clip' entity and avoid migration issues
    @ColumnInfo(name = "isPinned")
    val isPinned: Boolean = false,

    @ColumnInfo(name = "is_encrypted", defaultValue = "0")
    val isEncrypted: Boolean = false, // v2

    @ColumnInfo(name = "category", defaultValue = "general")
    val category: String = "general" // v3
) : Parcelable