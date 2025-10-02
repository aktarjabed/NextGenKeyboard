package com.nextgen.keyboard.data.local

import androidx.room.*
import com.nextgen.keyboard.data.model.Clip
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
}