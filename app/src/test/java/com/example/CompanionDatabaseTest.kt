package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.CompanionDao
import com.example.data.CompanionDatabase
import com.example.data.CrewStatus
import com.example.data.MediaItem
import com.example.data.MediaItemDao
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
class CompanionDatabaseTest {

    private lateinit var db: CompanionDatabase
    private lateinit var companionDao: CompanionDao
    private lateinit var mediaItemDao: MediaItemDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, CompanionDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        companionDao = db.companionDao()
        mediaItemDao = db.mediaItemDao()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun insertAndReadMediaItem() = runBlocking {
        val item = MediaItem(
            title = "sample_nature_clip.mp4",
            path = "content://media/external/video/media/101",
            duration = 45000L,
            mediaType = "video/mp4",
            uri = "content://media/external/video/media/101",
            fileName = "sample_nature_clip.mp4",
            size = 2048576L,
            category = "Nature",
            stagingStatus = "STAGED",
            timestamp = 1700000000000L,
            isConsented = false,
            isRouted = false
        )

        val id = mediaItemDao.insertMedia(item)
        assertTrue(id > 0)

        val allMedia = mediaItemDao.getAllMedia().first()
        assertEquals(1, allMedia.size)
        assertEquals("sample_nature_clip.mp4", allMedia[0].title)
        assertEquals("content://media/external/video/media/101", allMedia[0].path)
        assertEquals(45000L, allMedia[0].duration)
        assertEquals("video/mp4", allMedia[0].mediaType)
        assertEquals("sample_nature_clip.mp4", allMedia[0].fileName)
        assertEquals("Nature", allMedia[0].category)
        assertEquals("STAGED", allMedia[0].stagingStatus)
    }

    @Test
    fun queryMediaByStagingStatus() = runBlocking {
        val item1 = MediaItem(
            uri = "content://media/external/video/media/1",
            fileName = "pending1.mp4",
            stagingStatus = "STAGED",
            isRouted = false
        )
        val item2 = MediaItem(
            uri = "content://media/external/video/media/2",
            fileName = "routed1.mp4",
            stagingStatus = "ROUTED",
            isRouted = true
        )
        val item3 = MediaItem(
            uri = "content://media/external/video/media/3",
            fileName = "pending2.mp4",
            stagingStatus = "STAGED",
            isRouted = false
        )

        mediaItemDao.insertMediaList(listOf(item1, item2, item3))

        val stagedItems = mediaItemDao.getMediaByStagingStatus("STAGED").first()
        assertEquals(2, stagedItems.size)

        val routedItems = mediaItemDao.getMediaByStagingStatus("ROUTED").first()
        assertEquals(1, routedItems.size)
        assertEquals("routed1.mp4", routedItems[0].fileName)

        val stagedSync = mediaItemDao.getMediaByStagingStatusSync("STAGED")
        assertEquals(2, stagedSync.size)
    }

    @Test
    fun deleteMediaOperations() = runBlocking {
        val item1 = MediaItem(
            uri = "content://media/external/video/media/10",
            fileName = "delete_me.mp4",
            stagingStatus = "ARCHIVED"
        )
        val item2 = MediaItem(
            uri = "content://media/external/video/media/20",
            fileName = "keep_me.mp4",
            stagingStatus = "STAGED"
        )

        val id1 = mediaItemDao.insertMedia(item1)
        mediaItemDao.insertMedia(item2)

        mediaItemDao.deleteById(id1)
        val remaining = mediaItemDao.getAllMedia().first()
        assertEquals(1, remaining.size)
        assertEquals("keep_me.mp4", remaining[0].fileName)

        // Delete by staging status
        mediaItemDao.deleteByStagingStatus("STAGED")
        val emptyList = mediaItemDao.getAllMedia().first()
        assertEquals(0, emptyList.size)
    }

    @Test
    fun updateConsentAndRoute() = runBlocking {
        val item = MediaItem(
            uri = "content://media/external/video/media/202",
            fileName = "rant_session.mp4",
            category = "Rant"
        )
        val id = mediaItemDao.insertMedia(item)

        mediaItemDao.updateConsent(id, true)
        val consentedItem = mediaItemDao.getMediaById(id).first()
        assertNotNull(consentedItem)
        assertTrue(consentedItem!!.isConsented)

        mediaItemDao.markAsRouted(id)
        val routedItem = mediaItemDao.getMediaById(id).first()
        assertTrue(routedItem!!.isRouted)
        assertEquals("ROUTED", routedItem.stagingStatus)
    }

    @Test
    fun crewStatusHandshake() = runBlocking {
        val status = CrewStatus(
            id = 1,
            isCrew = true,
            handshakeTimestamp = 1700000000000L,
            handshakeMediaUri = "local://handshake.mp4"
        )
        companionDao.updateCrewStatus(status)

        val readStatus = companionDao.getCrewStatus().first()
        assertNotNull(readStatus)
        assertTrue(readStatus!!.isCrew)
    }
}
