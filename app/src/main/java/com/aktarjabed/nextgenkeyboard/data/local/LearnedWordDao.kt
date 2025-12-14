package com.aktarjabed.nextgenkeyboard.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface LearnedWordDao {
    @Query("SELECT * FROM learned_words ORDER BY frequency DESC")
    fun getAllWords(): Flow<List<LearnedWordEntity>>

    @Query("SELECT * FROM learned_words ORDER BY frequency DESC")
    suspend fun getAllWordsSync(): List<LearnedWordEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(word: LearnedWordEntity)

    @Delete
    suspend fun delete(word: LearnedWordEntity)

    @Query("SELECT * FROM learned_words WHERE word = :word LIMIT 1")
    suspend fun getWord(word: String): LearnedWordEntity?

    @Query("UPDATE learned_words SET frequency = frequency + 1 WHERE word = :word")
    suspend fun incrementFrequency(word: String)
}
