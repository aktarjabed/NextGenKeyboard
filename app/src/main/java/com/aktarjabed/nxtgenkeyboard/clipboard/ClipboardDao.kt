package com.aktarjabed.nxtgenkeyboard.clipboard

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ClipboardDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(item: ClipboardEntity)

    @Query("SELECT * FROM clipboard ORDER BY pinned DESC, createdAt DESC LIMIT 200")
    fun all(): Flow<List<ClipboardEntity>>

    @Query("SELECT id FROM clipboard WHERE text = :text LIMIT 1")
    suspend fun findIdByText(text: String): Long?

    @Query("UPDATE clipboard SET createdAt = :timestamp WHERE id = :id")
    suspend fun touch(id: Long, timestamp: Long)

    @Delete
    suspend fun delete(item: ClipboardEntity)

    @Query("UPDATE clipboard SET pinned = :pinned WHERE id = :id")
    suspend fun setPinned(id: Long, pinned: Boolean)

    @Query("DELETE FROM clipboard WHERE pinned = 0")
    suspend fun clearUnpinned()

    @Query("DELETE FROM clipboard")
    suspend fun clearAll()

    // Retention: Keep all pinned items; for unpinned items, keep only the 200 most recent.
    @Query(
        """
        DELETE FROM clipboard
        WHERE pinned = 0
          AND id NOT IN (
              SELECT id FROM clipboard
              WHERE pinned = 0
              ORDER BY createdAt DESC
              LIMIT 200
          )
        """
    )
    suspend fun trimHistory()
}