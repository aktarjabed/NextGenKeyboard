package com.aktarjabed.nextgenkeyboard.data.repository

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EmojiUsageRepository @Inject constructor(
    private val preferencesRepository: PreferencesRepository
) {
    val recentEmojis: Flow<List<String>> = preferencesRepository.recentEmojis

    suspend fun trackEmojiUsage(emoji: String) {
        preferencesRepository.addRecentEmoji(emoji)
    }
}
