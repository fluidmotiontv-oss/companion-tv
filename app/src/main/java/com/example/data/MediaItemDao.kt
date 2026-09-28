package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for MediaItem entities in the Room database.
 * Provides asynchronous methods for insertions, deletions, updates, and queries,
 * specifically supporting staging status filtering, consent, and routing states.
 */
@Dao
interface MediaItemDao {

    // --- Insert Operations (Asynchronous via suspend) ---

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedia(media: MediaItem): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMediaItem(media: MediaItem): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(media: MediaItem): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMediaList(mediaList: List<MediaItem>): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(mediaList: List<MediaItem>): List<Long>

    // --- Query Operations by Staging Status (Asynchronous via Flow & suspend) ---

    @Query("SELECT * FROM media_items WHERE stagingStatus = :stagingStatus ORDER BY timestamp DESC")
    fun getMediaByStagingStatus(stagingStatus: String): Flow<List<MediaItem>>

    @Query("SELECT * FROM media_items WHERE stagingStatus = :stagingStatus ORDER BY timestamp DESC")
    suspend fun getMediaByStagingStatusSync(stagingStatus: String): List<MediaItem>

    @Query("SELECT * FROM media_items WHERE isRouted = :isRouted ORDER BY timestamp DESC")
    fun getMediaByRoutedStatus(isRouted: Boolean): Flow<List<MediaItem>>

    @Query("SELECT * FROM media_items WHERE isConsented = :isConsented ORDER BY timestamp DESC")
    fun getMediaByConsentStatus(isConsented: Boolean): Flow<List<MediaItem>>

    @Query("SELECT * FROM media_items WHERE isRouted = 0 ORDER BY timestamp DESC")
    fun getStagedMedia(): Flow<List<MediaItem>>

    @Query("SELECT * FROM media_items WHERE isRouted = 1 ORDER BY timestamp DESC")
    fun getRoutedMedia(): Flow<List<MediaItem>>

    @Query("SELECT * FROM media_items WHERE isConsented = 0 ORDER BY timestamp DESC")
    fun getUnconsentedMedia(): Flow<List<MediaItem>>

    @Query("SELECT * FROM media_items WHERE isConsented = 1 ORDER BY timestamp DESC")
    fun getConsentedMedia(): Flow<List<MediaItem>>

    // --- General Queries (Asynchronous via Flow & suspend) ---

    @Query("SELECT * FROM media_items ORDER BY timestamp DESC")
    fun getAllMedia(): Flow<List<MediaItem>>

    @Query("SELECT * FROM media_items WHERE id = :id")
    fun getMediaById(id: Long): Flow<MediaItem?>

    @Query("SELECT * FROM media_items WHERE id = :id")
    suspend fun findMediaById(id: Long): MediaItem?

    @Query("SELECT * FROM media_items WHERE uri = :uri LIMIT 1")
    fun getMediaByUri(uri: String): Flow<MediaItem?>

    @Query("SELECT * FROM media_items WHERE uri = :uri LIMIT 1")
    suspend fun findMediaByUri(uri: String): MediaItem?

    @Query("SELECT * FROM media_items WHERE category = :category ORDER BY timestamp DESC")
    fun getMediaByCategory(category: String): Flow<List<MediaItem>>

    @Query("SELECT COUNT(*) FROM media_items WHERE stagingStatus = :stagingStatus")
    fun getCountByStagingStatus(stagingStatus: String): Flow<Int>

    // --- Update Operations (Asynchronous via suspend) ---

    @Update
    suspend fun updateMedia(media: MediaItem)

    @Update
    suspend fun update(media: MediaItem)

    @Query("UPDATE media_items SET stagingStatus = :status WHERE id = :id")
    suspend fun updateStagingStatus(id: Long, status: String)

    @Query("UPDATE media_items SET isConsented = :consented WHERE id = :id")
    suspend fun updateConsent(id: Long, consented: Boolean)

    @Query("UPDATE media_items SET isRouted = 1, stagingStatus = 'ROUTED' WHERE id = :id")
    suspend fun markAsRouted(id: Long)

    // --- Delete Operations (Asynchronous via suspend) ---

    @Delete
    suspend fun delete(media: MediaItem)

    @Delete
    suspend fun deleteMedia(media: MediaItem)

    @Query("DELETE FROM media_items WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM media_items WHERE id = :id")
    suspend fun deleteMediaById(id: Long)

    @Query("DELETE FROM media_items WHERE stagingStatus = :stagingStatus")
    suspend fun deleteByStagingStatus(stagingStatus: String)

    @Query("DELETE FROM media_items WHERE isRouted = 1")
    suspend fun deleteRoutedMedia()

    @Query("DELETE FROM media_items")
    suspend fun deleteAll()

    @Query("DELETE FROM media_items")
    suspend fun clearAllMedia()
}
