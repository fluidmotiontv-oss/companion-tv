package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.AppDatabase
import com.example.data.MediaItem
import com.example.data.MediaItemRepository
import com.example.ui.MediaListUiState
import com.example.ui.MediaViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class MediaViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var db: AppDatabase
    private lateinit var repository: MediaItemRepository
    private lateinit var viewModel: MediaViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = MediaItemRepository(db.mediaItemDao())
        viewModel = MediaViewModel(repository)
    }

    @After
    fun tearDown() {
        db.close()
        Dispatchers.resetMain()
    }

    @Test
    fun testExposesMediaItemsStateFlow() = runBlocking {
        val item = MediaItem(
            title = "cockpit_view.mp4",
            path = "content://media/1",
            duration = 15000L,
            mediaType = "video/mp4",
            uri = "content://media/1",
            fileName = "cockpit_view.mp4",
            category = "Cockpit",
            stagingStatus = "STAGED"
        )
        repository.insertMedia(item)

        val mediaList = viewModel.mediaItems.first { it.isNotEmpty() }
        assertEquals(1, mediaList.size)
        assertEquals("cockpit_view.mp4", mediaList[0].title)

        val allMedia = viewModel.allMedia.first { it.isNotEmpty() }
        assertEquals(1, allMedia.size)
    }

    @Test
    fun testCategoryFilteringAndSearch() = runBlocking {
        val item1 = MediaItem(
            title = "clouds.mp4",
            path = "path/1",
            uri = "path/1",
            fileName = "clouds.mp4",
            category = "Atmospheric"
        )
        val item2 = MediaItem(
            title = "runway.mp4",
            path = "path/2",
            uri = "path/2",
            fileName = "runway.mp4",
            category = "Airport"
        )

        repository.insertMediaList(listOf(item1, item2))

        val initialFiltered = viewModel.filteredMedia.first { it.size == 2 }
        assertEquals(2, initialFiltered.size)

        viewModel.selectCategory("Airport")
        val airportFiltered = viewModel.filteredMedia.first { it.size == 1 }
        assertEquals("runway.mp4", airportFiltered[0].title)

        viewModel.selectCategory("ALL")
        viewModel.setSearchQuery("clouds")
        val searchFiltered = viewModel.filteredMedia.first { it.size == 1 }
        assertEquals("clouds.mp4", searchFiltered[0].title)
    }

    @Test
    fun testInsertAndDeleteFromViewModel() = runBlocking {
        viewModel.insertMedia(
            title = "new_clip.mp4",
            path = "path/new",
            duration = 5000L,
            mediaType = "video/mp4",
            category = "General"
        )
        testDispatcher.scheduler.advanceUntilIdle()

        val list = repository.allMedia.first()
        assertEquals(1, list.size)
        val id = list[0].id

        viewModel.deleteMedia(id)
        testDispatcher.scheduler.advanceUntilIdle()

        val emptyList = repository.allMedia.first()
        assertTrue(emptyList.isEmpty())
    }
}
