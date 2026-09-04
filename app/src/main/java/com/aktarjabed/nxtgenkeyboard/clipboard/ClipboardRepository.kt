package com.aktarjabed.nxtgenkeyboard.clipboard

import kotlinx.coroutines.flow.Flow

class ClipboardRepository(private val dao: ClipboardDao) {

    val history: Flow<List<ClipboardEntity>> = dao.all()

    suspend fun addIfNew(text: String) {
        val cleaned = text.trim()
        if (cleaned.isEmpty()) return

        val existingId = dao.findIdByText(cleaned)
        if (existingId != null) {
            dao.touch(existingId, System.currentTimeMillis())
        } else {
            dao.insert(ClipboardEntity(text = cleaned))
        }
        // Enforce retention policy
        dao.trimHistory()
    }

    suspend fun delete(item: ClipboardEntity) {
        dao.delete(item)
    }

    suspend fun togglePin(item: ClipboardEntity) {
        dao.setPinned(item.id, !item.pinned)
    }

    suspend fun clearUnpinned() {
        dao.clearUnpinned()
    }

    suspend fun clearAll() {
        dao.clearAll()
    }
}