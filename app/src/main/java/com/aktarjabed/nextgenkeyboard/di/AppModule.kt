package com.aktarjabed.nextgenkeyboard.di

import android.content.Context
import com.aktarjabed.nextgenkeyboard.BuildConfig
import com.aktarjabed.nextgenkeyboard.feature.ai.AiPredictionClient
import com.aktarjabed.nextgenkeyboard.feature.ai.GeminiPredictionClient
import com.aktarjabed.nextgenkeyboard.feature.ai.MockPredictionClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideApplicationContext(@ApplicationContext context: Context): Context {
        return context
    }

    @Provides
    @Singleton
    fun provideAiPredictionClient(): AiPredictionClient {
        val apiKey = BuildConfig.GEMINI_API_KEY
        return if (apiKey.isNotBlank()) {
            GeminiPredictionClient()
        } else {
            MockPredictionClient()
        }
    }
}
