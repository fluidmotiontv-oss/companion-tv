package com.example.data

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository interface for the FluidmotionTV Library.
 */
interface LibraryRepository {
    val allItems: Flow<List<LibraryItem>>
    suspend fun insertItem(item: LibraryItem): Long
    suspend fun deleteItemById(id: Long)
    suspend fun getItemById(id: Long): LibraryItem?
    suspend fun clearLibrary()
}

/**
 * Implementation of LibraryRepository using LibraryDao.
 */
class LibraryRepositoryImpl(
    private val libraryDao: LibraryDao
) : LibraryRepository {
    override val allItems: Flow<List<LibraryItem>> = libraryDao.getAllLibraryItems()

    override suspend fun insertItem(item: LibraryItem): Long = libraryDao.insertItem(item)

    override suspend fun deleteItemById(id: Long) = libraryDao.deleteItemById(id)

    override suspend fun getItemById(id: Long): LibraryItem? = libraryDao.getItemById(id)

    override suspend fun clearLibrary() = libraryDao.clearLibrary()
}
