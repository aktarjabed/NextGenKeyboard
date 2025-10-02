package com.nextgen.keyboard.data.repository

import com.nextgen.keyboard.data.local.ClipDao
import com.nextgen.keyboard.data.model.Clip
import kotlinx.coroutines.flow.Flow
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ClipboardRepository @Inject constructor(
    private val clipDao: ClipDao
) {

    fun getPinnedClips(): Flow<List<Clip>> = clipDao.getPinnedClips()

    fun getRecentClips(): Flow<List<Clip>> = clipDao.getRecentClips()

    suspend fun searchClips(query: String): Result<List<Clip>> {
        return try {
            val clips = clipDao.searchClips(query)
            Result.success(clips)
        } catch (e: Exception) {
            Timber.e(e, "Error searching clips")
            Result.failure(e)
        }
    }

    suspend fun saveClip(content: String): Result<Long> {
        return try {
            if (content.isBlank()) {
                return Result.failure(IllegalArgumentException("Clip content cannot be blank"))
            }

            val clip = Clip(content = content.trim())
            val id = clipDao.insertClip(clip)
            Timber.d("Saved clip: $content")
            Result.success(id)
        } catch (e: Exception) {
            Timber.e(e, "Error saving clip")
            Result.failure(e)
        }
    }

    suspend fun updateClip(clip: Clip): Result<Unit> {
        return try {
            clipDao.updateClip(clip)
            Timber.d("Updated clip: ${clip.id}")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error updating clip")
            Result.failure(e)
        }
    }

    suspend fun deleteClip(clip: Clip): Result<Unit> {
        return try {
            clipDao.deleteClip(clip)
            Timber.d("Deleted clip: ${clip.id}")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error deleting clip")
            Result.failure(e)
        }
    }

    suspend fun clearAllClips(): Result<Unit> {
        return try {
            clipDao.deleteAllClips()
            Timber.d("Cleared all clips")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error clearing clips")
            Result.failure(e)
        }
    }

    suspend fun clearUnpinnedClips(): Result<Unit> {
        return try {
            clipDao.clearUnpinnedClips()
            Timber.d("Cleared unpinned clips")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error clearing unpinned clips")
            Result.failure(e)
        }
    }
}