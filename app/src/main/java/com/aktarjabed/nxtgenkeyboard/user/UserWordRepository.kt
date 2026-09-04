package com.aktarjabed.nxtgenkeyboard.user

class UserWordRepository(private val dao: UserWordDao) {

    suspend fun add(lang: String, word: String) {
        val w = word.trim().lowercase()
        if (w.length >= 2) {
            dao.insert(UserWordEntity(lang = lang, word = w))
        }
    }

    suspend fun words(lang: String): List<String> = dao.words(lang)
}