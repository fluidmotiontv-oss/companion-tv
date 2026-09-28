package com.example

import android.app.Application
import com.example.data.AppDatabase
import com.example.data.CompanionRepository
import com.example.data.LibraryRepository
import com.example.data.LibraryRepositoryImpl
import com.example.data.MediaItemRepository
import com.example.logic.MediaScanner

class CompanionApp : Application() {
    val database: AppDatabase by lazy {
        AppDatabase.getDatabase(this)
    }

    val mediaItemRepository: MediaItemRepository by lazy {
        MediaItemRepository(database.mediaItemDao())
    }

    val libraryRepository: LibraryRepository by lazy {
        LibraryRepositoryImpl(database.libraryDao())
    }

    val repository: CompanionRepository by lazy {
        CompanionRepository(
            companionDao = database.companionDao(),
            mediaRepository = mediaItemRepository
        )
    }

    val scanner: MediaScanner by lazy {
        MediaScanner(applicationContext)
    }
}
