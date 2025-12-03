package com.aktarjabed.nextgenkeyboard.data.local

import androidx.room.*
import com.aktarjabed.nextgenkeyboard.data.models.ClipboardEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ClipboardDao {

    @Query("SELECT * FROM clips WHERE isPinned = 1 ORDER BY timestamp DESC")
    fun getPinnedClips(): Flow<List<ClipboardEntity>>

    @Query("SELECT * FROM clips WHERE isPinned = 0 ORDER BY timestamp DESC LIMIT 50")
    fun getRecentClips(): Flow<List<ClipboardEntity>>

    @Query("SELECT * FROM clips WHERE content LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    suspend fun searchClips(query: String): List<ClipboardEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(clip: ClipboardEntity): Long

    @Update
    suspend fun update(clip: ClipboardEntity)

    @Delete
    suspend fun delete(clip: ClipboardEntity)

    @Query("DELETE FROM clips WHERE id = :id")
    suspend fun deleteClip(id: Long)

    @Query("DELETE FROM clips WHERE isPinned = 0")
    suspend fun clearUnpinnedClips()

    @Query("DELETE FROM clips")
    suspend fun deleteAllClips()

    @Query("SELECT COUNT(*) FROM clips")
    suspend fun getClipCount(): Int

    @Query("""
        DELETE FROM clips
        WHERE id IN (
            SELECT id FROM clips
            WHERE isPinned = 0
            ORDER BY timestamp ASC
            LIMIT :limit
        )
    """)
    suspend fun deleteOldestUnpinned(limit: Int)

    @Query("DELETE FROM clips WHERE isPinned = 0 AND timestamp < :beforeTimestamp")
    suspend fun deleteOlderThan(beforeTimestamp: Long)

    @Query("SELECT COUNT(*) FROM clips WHERE isPinned = 0")
    suspend fun getUnpinnedCount(): Int
}