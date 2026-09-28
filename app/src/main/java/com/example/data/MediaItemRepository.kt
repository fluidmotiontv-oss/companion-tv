package com.example.data

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository interface defining a clean contract for local media operations.
 */
interface MediaRepository {
    val allMedia: Flow<List<MediaItem>>
    val stagedMedia: Flow<List<MediaItem>>
    val routedMedia: Flow<List<MediaItem>>
    val unconsentedMedia: Flow<List<MediaItem>>
    val consentedMedia: Flow<List<MediaItem>>

    fun getMediaById(id: Long): Flow<MediaItem?>
    suspend fun findMediaById(id: Long): MediaItem?
    fun getMediaByUri(uri: String): Flow<MediaItem?>
    suspend fun findMediaByUri(uri: String): MediaItem?
    fun getMediaByCategory(category: String): Flow<List<MediaItem>>
    fun getMediaByStagingStatus(status: String): Flow<List<MediaItem>>
    suspend fun getMediaByStagingStatusSync(status: String): List<MediaItem>
    fun getCountByStagingStatus(status: String): Flow<Int>

    suspend fun insertMedia(media: MediaItem): Long
    suspend fun insertMediaItem(media: MediaItem): Long
    suspend fun insert(media: MediaItem): Long
    suspend fun insertMediaList(mediaList: List<MediaItem>): List<Long>

    suspend fun updateMedia(media: MediaItem)
    suspend fun update(media: MediaItem)
    suspend fun updateConsent(id: Long, consented: Boolean)
    suspend fun updateStagingStatus(id: Long, status: String)
    suspend fun markAsRouted(id: Long)
    suspend fun markRouted(id: Long)

    suspend fun deleteMedia(media: MediaItem)
    suspend fun deleteMedia(id: Long)
    suspend fun delete(media: MediaItem)
    suspend fun deleteById(id: Long)
    suspend fun deleteMediaById(id: Long)
    suspend fun deleteByStagingStatus(status: String)
    suspend fun deleteRoutedMedia()
    suspend fun clearAllMedia()
    suspend fun deleteAll()
}

/**
 * Repository class that abstracts access to the MediaItem DAO,
 * providing a clean and robust API for ViewModels and application components
 * to interact with local media data.
 */
@Singleton
class MediaItemRepository @Inject constructor(
    private val mediaItemDao: MediaItemDao
) : MediaRepository {

    override val allMedia: Flow<List<MediaItem>> = mediaItemDao.getAllMedia()
    val allMediaItems: Flow<List<MediaItem>> = allMedia
    override val stagedMedia: Flow<List<MediaItem>> = mediaItemDao.getStagedMedia()
    override val routedMedia: Flow<List<MediaItem>> = mediaItemDao.getRoutedMedia()
    override val unconsentedMedia: Flow<List<MediaItem>> = mediaItemDao.getUnconsentedMedia()
    override val consentedMedia: Flow<List<MediaItem>> = mediaItemDao.getConsentedMedia()

    override fun getMediaById(id: Long): Flow<MediaItem?> = mediaItemDao.getMediaById(id)

    override suspend fun findMediaById(id: Long): MediaItem? = mediaItemDao.findMediaById(id)

    override fun getMediaByUri(uri: String): Flow<MediaItem?> = mediaItemDao.getMediaByUri(uri)

    override suspend fun findMediaByUri(uri: String): MediaItem? = mediaItemDao.findMediaByUri(uri)

    override fun getMediaByCategory(category: String): Flow<List<MediaItem>> =
        mediaItemDao.getMediaByCategory(category)

    override fun getMediaByStagingStatus(status: String): Flow<List<MediaItem>> =
        mediaItemDao.getMediaByStagingStatus(status)

    override suspend fun getMediaByStagingStatusSync(status: String): List<MediaItem> =
        mediaItemDao.getMediaByStagingStatusSync(status)

    override fun getCountByStagingStatus(status: String): Flow<Int> =
        mediaItemDao.getCountByStagingStatus(status)

    override suspend fun insertMedia(media: MediaItem): Long = mediaItemDao.insertMedia(media)

    override suspend fun insertMediaItem(media: MediaItem): Long = mediaItemDao.insertMediaItem(media)

    override suspend fun insert(media: MediaItem): Long = mediaItemDao.insert(media)

    override suspend fun insertMediaList(mediaList: List<MediaItem>): List<Long> =
        mediaItemDao.insertMediaList(mediaList)

    override suspend fun updateMedia(media: MediaItem) = mediaItemDao.updateMedia(media)

    override suspend fun update(media: MediaItem) = mediaItemDao.update(media)

    override suspend fun updateConsent(id: Long, consented: Boolean) =
        mediaItemDao.updateConsent(id, consented)

    override suspend fun updateStagingStatus(id: Long, status: String) =
        mediaItemDao.updateStagingStatus(id, status)

    override suspend fun markAsRouted(id: Long) = mediaItemDao.markAsRouted(id)

    override suspend fun markRouted(id: Long) = mediaItemDao.markAsRouted(id)

    override suspend fun deleteMedia(media: MediaItem) = mediaItemDao.deleteMedia(media)

    override suspend fun deleteMedia(id: Long) = mediaItemDao.deleteMediaById(id)

    override suspend fun delete(media: MediaItem) = mediaItemDao.delete(media)

    override suspend fun deleteById(id: Long) = mediaItemDao.deleteById(id)

    override suspend fun deleteMediaById(id: Long) = mediaItemDao.deleteMediaById(id)

    override suspend fun deleteByStagingStatus(status: String) =
        mediaItemDao.deleteByStagingStatus(status)

    override suspend fun deleteRoutedMedia() = mediaItemDao.deleteRoutedMedia()

    override suspend fun clearAllMedia() = mediaItemDao.clearAllMedia()

    override suspend fun deleteAll() = mediaItemDao.deleteAll()
}
