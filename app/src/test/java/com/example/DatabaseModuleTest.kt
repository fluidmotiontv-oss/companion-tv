package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.di.DatabaseModule
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertSame
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class DatabaseModuleTest {

    @Test
    fun testDatabaseModuleProvidesDependencies() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        val database = DatabaseModule.provideAppDatabase(context)
        assertNotNull(database)

        val mediaItemDao = DatabaseModule.provideMediaItemDao(database)
        assertNotNull(mediaItemDao)

        val companionDao = DatabaseModule.provideCompanionDao(database)
        assertNotNull(companionDao)

        val mediaItemRepository = DatabaseModule.provideMediaItemRepository(mediaItemDao)
        assertNotNull(mediaItemRepository)

        val mediaRepository = DatabaseModule.provideMediaRepository(mediaItemRepository)
        assertNotNull(mediaRepository)

        val repository = DatabaseModule.provideCompanionRepository(companionDao, mediaItemRepository)
        assertNotNull(repository)

        // Verify singleton instance retrieval
        val database2 = DatabaseModule.provideAppDatabase(context)
        assertSame(database, database2)
    }
}
