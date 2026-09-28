package com.example.data

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Unified Companion repository coordinating crew status and media items.
 */
@Singleton
class CompanionRepository @Inject constructor(
    private val companionDao: CompanionDao,
    private val mediaRepository: MediaItemRepository
) : MediaRepository {

    constructor(
        companionDao: CompanionDao,
        mediaItemDao: MediaItemDao
    ) : this(
        companionDao = companionDao,
        mediaRepository = MediaItemRepository(mediaItemDao)
    )

    val crewStatus: Flow<CrewStatus?> = companionDao.getCrewStatus()

    suspend fun updateCrewStatus(status: CrewStatus) = companionDao.updateCrewStatus(status)

    override val allMedia: Flow<List<MediaItem>> get() = mediaRepository.allMedia
    override val stagedMedia: Flow<List<MediaItem>> get() = mediaRepository.stagedMedia
    override val routedMedia: Flow<List<MediaItem>> get() = mediaRepository.routedMedia
    override val unconsentedMedia: Flow<List<MediaItem>> get() = mediaRepository.unconsentedMedia
    override val consentedMedia: Flow<List<MediaItem>> get() = mediaRepository.consentedMedia

    override fun getMediaById(id: Long): Flow<MediaItem?> = mediaRepository.getMediaById(id)
    override suspend fun findMediaById(id: Long): MediaItem? = mediaRepository.findMediaById(id)
    override fun getMediaByUri(uri: String): Flow<MediaItem?> = mediaRepository.getMediaByUri(uri)
    override suspend fun findMediaByUri(uri: String): MediaItem? = mediaRepository.findMediaByUri(uri)
    override fun getMediaByCategory(category: String): Flow<List<MediaItem>> = mediaRepository.getMediaByCategory(category)
    override fun getMediaByStagingStatus(status: String): Flow<List<MediaItem>> = mediaRepository.getMediaByStagingStatus(status)
    override suspend fun getMediaByStagingStatusSync(status: String): List<MediaItem> = mediaRepository.getMediaByStagingStatusSync(status)
    override fun getCountByStagingStatus(status: String): Flow<Int> = mediaRepository.getCountByStagingStatus(status)

    override suspend fun insertMedia(media: MediaItem): Long = mediaRepository.insertMedia(media)
    override suspend fun insertMediaItem(media: MediaItem): Long = mediaRepository.insertMediaItem(media)
    override suspend fun insert(media: MediaItem): Long = mediaRepository.insert(media)
    override suspend fun insertMediaList(mediaList: List<MediaItem>): List<Long> = mediaRepository.insertMediaList(mediaList)

    override suspend fun updateMedia(media: MediaItem) = mediaRepository.updateMedia(media)
    override suspend fun update(media: MediaItem) = mediaRepository.update(media)
    override suspend fun updateConsent(id: Long, consented: Boolean) = mediaRepository.updateConsent(id, consented)
    override suspend fun updateStagingStatus(id: Long, status: String) = mediaRepository.updateStagingStatus(id, status)
    override suspend fun markAsRouted(id: Long) = mediaRepository.markAsRouted(id)
    override suspend fun markRouted(id: Long) = mediaRepository.markRouted(id)

    override suspend fun deleteMedia(media: MediaItem) = mediaRepository.deleteMedia(media)
    override suspend fun deleteMedia(id: Long) = mediaRepository.deleteMedia(id)
    override suspend fun delete(media: MediaItem) = mediaRepository.delete(media)
    override suspend fun deleteById(id: Long) = mediaRepository.deleteById(id)
    override suspend fun deleteMediaById(id: Long) = mediaRepository.deleteMediaById(id)
    override suspend fun deleteByStagingStatus(status: String) = mediaRepository.deleteByStagingStatus(status)
    override suspend fun deleteRoutedMedia() = mediaRepository.deleteRoutedMedia()
    override suspend fun clearAllMedia() = mediaRepository.clearAllMedia()
    override suspend fun deleteAll() = mediaRepository.deleteAll()
}
