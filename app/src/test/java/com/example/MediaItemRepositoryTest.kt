package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.AppDatabase
import com.example.data.MediaItem
import com.example.data.MediaItemDao
import com.example.data.MediaItemRepository
import com.example.data.MediaRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class MediaItemRepositoryTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: MediaItemDao
    private lateinit var repository: MediaItemRepository

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.mediaItemDao()
        repository = MediaItemRepository(dao)
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun insertAndFetchAllMedia() = runBlocking {
        val item1 = MediaItem(
            title = "flight_takeoff.mp4",
            path = "content://media/external/video/1",
            duration = 120000L,
            mediaType = "video/mp4",
            uri = "content://media/external/video/1",
            fileName = "flight_takeoff.mp4",
            category = "Flight",
            stagingStatus = "STAGED",
            isConsented = false,
            isRouted = false
        )
        val item2 = MediaItem(
            title = "cockpit_shot.png",
            path = "content://media/external/images/2",
            duration = 0L,
            mediaType = "image/png",
            uri = "content://media/external/images/2",
            fileName = "cockpit_shot.png",
            category = "Screenshot",
            stagingStatus = "STAGED",
            isConsented = true,
            isRouted = false
        )

        val id1 = repository.insertMedia(item1)
        val id2 = repository.insertMediaItem(item2)

        assertTrue(id1 > 0)
        assertTrue(id2 > 0)

        val allItems = repository.allMedia.first()
        assertEquals(2, allItems.size)

        val flightCategory = repository.getMediaByCategory("Flight").first()
        assertEquals(1, flightCategory.size)
        assertEquals("flight_takeoff.mp4", flightCategory[0].title)

        val stagedItems = repository.stagedMedia.first()
        assertEquals(2, stagedItems.size)

        val consented = repository.consentedMedia.first()
        assertEquals(1, consented.size)
        assertEquals("cockpit_shot.png", consented[0].title)

        val unconsented = repository.unconsentedMedia.first()
        assertEquals(1, unconsented.size)
        assertEquals("flight_takeoff.mp4", unconsented[0].title)
    }

    @Test
    fun updateConsentAndStagingStatus() = runBlocking {
        val item = MediaItem(
            title = "briefing.mp4",
            path = "content://media/external/video/300",
            uri = "content://media/external/video/300",
            fileName = "briefing.mp4",
            stagingStatus = "STAGED",
            isConsented = false,
            isRouted = false
        )
        val id = repository.insert(item)

        repository.updateConsent(id, true)
        val itemAfterConsent = repository.getMediaById(id).first()
        assertNotNull(itemAfterConsent)
        assertTrue(itemAfterConsent!!.isConsented)

        repository.updateStagingStatus(id, "PROCESSED")
        val itemAfterStatus = repository.findMediaById(id)
        assertNotNull(itemAfterStatus)
        assertEquals("PROCESSED", itemAfterStatus!!.stagingStatus)

        repository.markAsRouted(id)
        val itemAfterRouted = repository.findMediaByUri("content://media/external/video/300")
        assertNotNull(itemAfterRouted)
        assertTrue(itemAfterRouted!!.isRouted)
        assertEquals("ROUTED", itemAfterRouted.stagingStatus)

        val routedCount = repository.getCountByStagingStatus("ROUTED").first()
        assertEquals(1, routedCount)
    }

    @Test
    fun batchOperationsAndDelete() = runBlocking {
        val items = listOf(
            MediaItem(
                title = "clip1.mp4",
                path = "path/1",
                uri = "path/1",
                fileName = "clip1.mp4",
                stagingStatus = "STAGED"
            ),
            MediaItem(
                title = "clip2.mp4",
                path = "path/2",
                uri = "path/2",
                fileName = "clip2.mp4",
                stagingStatus = "ARCHIVED"
            ),
            MediaItem(
                title = "clip3.mp4",
                path = "path/3",
                uri = "path/3",
                fileName = "clip3.mp4",
                stagingStatus = "ROUTED",
                isRouted = true
            )
        )

        val ids = repository.insertMediaList(items)
        assertEquals(3, ids.size)

        val initialAll = repository.allMedia.first()
        assertEquals(3, initialAll.size)

        repository.deleteByStagingStatus("ARCHIVED")
        val afterArchivedDelete = repository.allMedia.first()
        assertEquals(2, afterArchivedDelete.size)

        repository.deleteRoutedMedia()
        val afterRoutedDelete = repository.allMedia.first()
        assertEquals(1, afterRoutedDelete.size)
        assertEquals("clip1.mp4", afterRoutedDelete[0].title)

        repository.clearAllMedia()
        val emptyList = repository.allMedia.first()
        assertTrue(emptyList.isEmpty())
    }
}
