package com.aktarjabed.nextgenkeyboard.data.local

import androidx.room.*
import com.aktarjabed.nextgenkeyboard.data.model.Clip
import kotlinx.coroutines.flow.Flow

@Dao
interface ClipDao {

    @Query("SELECT * FROM clips WHERE isPinned = 1 ORDER BY timestamp DESC")
    fun getPinnedClips(): Flow<List<Clip>>

    @Query("SELECT * FROM clips WHERE isPinned = 0 ORDER BY timestamp DESC LIMIT 50")
    fun getRecentClips(): Flow<List<Clip>>

    @Query("SELECT * FROM clips WHERE content LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    suspend fun searchClips(query: String): List<Clip>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClip(clip: Clip): Long

    @Update
    suspend fun updateClip(clip: Clip)

    @Delete
    suspend fun deleteClip(clip: Clip)

    @Query("DELETE FROM clips WHERE isPinned = 0")
    suspend fun clearUnpinnedClips()

    @Query("DELETE FROM clips")
    suspend fun deleteAllClips()

    @Query("SELECT COUNT(*) FROM clips")
    suspend fun getClipCount(): Int

    // ✅ NEW: Delete oldest unpinned clips
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

    // ✅ NEW: Delete clips older than timestamp
    @Query("DELETE FROM clips WHERE isPinned = 0 AND timestamp < :beforeTimestamp")
    suspend fun deleteOlderThan(beforeTimestamp: Long)

    // ✅ NEW: Get count of unpinned clips
    @Query("SELECT COUNT(*) FROM clips WHERE isPinned = 0")
    suspend fun getUnpinnedCount(): Int
}