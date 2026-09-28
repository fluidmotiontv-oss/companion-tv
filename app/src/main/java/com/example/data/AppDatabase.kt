package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * AppDatabase is the primary Room database for the FluidmotionTV Companion application.
 * Manages local media storage persistence via [MediaItem] and sovereign handshake state via [CrewStatus].
 */
@Database(
    entities = [MediaItem::class, CrewStatus::class, LibraryItem::class],
    version = 6,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun mediaItemDao(): MediaItemDao
    abstract fun companionDao(): CompanionDao
    abstract fun libraryDao(): LibraryDao

    companion object {
        private const val DATABASE_NAME = "companion_db"

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
