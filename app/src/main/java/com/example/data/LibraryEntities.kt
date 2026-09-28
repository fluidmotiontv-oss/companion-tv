package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Entity representing a media item in the FluidmotionTV Library.
 * Stores core metadata required for playback and organization.
 */
@Entity(tableName = "library_items")
data class LibraryItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val path: String,
    val duration: Long, // in milliseconds
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Data Access Object for the FluidmotionTV Library.
 */
@Dao
interface LibraryDao {
    @Query("SELECT * FROM library_items ORDER BY timestamp DESC")
    fun getAllLibraryItems(): Flow<List<LibraryItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: LibraryItem): Long

    @Query("DELETE FROM library_items WHERE id = :id")
    suspend fun deleteItemById(id: Long)

    @Query("SELECT * FROM library_items WHERE id = :id")
    suspend fun getItemById(id: Long): LibraryItem?

    @Query("DELETE FROM library_items")
    suspend fun clearLibrary()
}
