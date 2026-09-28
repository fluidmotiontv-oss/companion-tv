package com.example.di

import android.content.Context
import com.example.data.AppDatabase
import com.example.data.CompanionDao
import com.example.data.CompanionRepository
import com.example.data.MediaItemDao
import com.example.data.MediaItemRepository
import com.example.data.MediaRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for providing Room database instance, DAOs, and repositories.
 * Ensures clean dependency injection across the FluidmotionTV Companion application.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return AppDatabase.getDatabase(context)
    }

    @Provides
    @Singleton
    fun provideMediaItemDao(
        database: AppDatabase
    ): MediaItemDao {
        return database.mediaItemDao()
    }

    @Provides
    @Singleton
    fun provideCompanionDao(
        database: AppDatabase
    ): CompanionDao {
        return database.companionDao()
    }

    @Provides
    @Singleton
    fun provideMediaItemRepository(
        mediaItemDao: MediaItemDao
    ): MediaItemRepository {
        return MediaItemRepository(mediaItemDao)
    }

    @Provides
    @Singleton
    fun provideMediaRepository(
        mediaItemRepository: MediaItemRepository
    ): MediaRepository {
        return mediaItemRepository
    }

    @Provides
    @Singleton
    fun provideCompanionRepository(
        companionDao: CompanionDao,
        mediaItemRepository: MediaItemRepository
    ): CompanionRepository {
        return CompanionRepository(
            companionDao = companionDao,
            mediaRepository = mediaItemRepository
        )
    }
}
