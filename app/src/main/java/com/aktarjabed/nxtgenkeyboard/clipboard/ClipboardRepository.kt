package com.aktarjabed.nxtgenkeyboard.clipboard

import kotlinx.coroutines.flow.Flow

class ClipboardRepository(private val dao: ClipboardDao) {

    val history: Flow<List<ClipboardEntity>> = dao.all()

    suspend fun addIfNew(text: String) {
        val cleaned = text.trim()
        if (cleaned.isEmpty()) return

        try {
            val existingId = dao.findIdByText(cleaned)
            if (existingId != null) {
                dao.touch(existingId, System.currentTimeMillis())
            } else {
                dao.insert(ClipboardEntity(text = cleaned))
            }
            // Enforce retention policy
            dao.trimHistory()
        } catch (e: Exception) {
            android.util.Log.e("NxtGenIME", "Failed to add/update clipboard history", e)
        }
    }

    suspend fun delete(item: ClipboardEntity) {
        dao.delete(item)
    }

    suspend fun togglePin(item: ClipboardEntity) {
        dao.setPinned(item.id, !item.pinned)
        dao.trimHistory()
    }

    suspend fun clearUnpinned() {
        dao.clearUnpinned()
    }

    suspend fun clearAll() {
        dao.clearAll()
    }
}