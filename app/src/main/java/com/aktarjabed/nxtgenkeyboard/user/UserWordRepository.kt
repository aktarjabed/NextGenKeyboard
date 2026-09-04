package com.aktarjabed.nxtgenkeyboard.user

class UserWordRepository(private val dao: UserWordDao) {

    suspend fun add(lang: String, word: String) {
        val w = word.trim().lowercase()
        if (w.length >= 2) {
            try {
                dao.insert(UserWordEntity(lang = lang, word = w))
            } catch (e: Exception) {
                android.util.Log.e("NxtGenIME", "Failed to add user word", e)
            }
        }
    }

    suspend fun words(lang: String): List<String> {
        return try {
            dao.words(lang)
        } catch (e: Exception) {
            android.util.Log.e("NxtGenIME", "Failed to get user words", e)
            emptyList()
        }
    }
}