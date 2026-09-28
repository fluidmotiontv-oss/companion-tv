package com.example.logic

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import com.example.data.MediaItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MediaScanner(private val context: Context) {

    suspend fun scanLocalMedia(): List<MediaItem> = withContext(Dispatchers.IO) {
        val mediaList = mutableListOf<MediaItem>()

        // Scan Videos
        scanType(
            uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            isVideo = true,
            list = mediaList
        )

        // Scan Images
        scanType(
            uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            isVideo = false,
            list = mediaList
        )

        mediaList.sortedByDescending { it.timestamp }
    }

    private fun scanType(uri: Uri, isVideo: Boolean, list: MutableList<MediaItem>) {
        val projection = arrayOf(
            MediaStore.MediaColumns._ID,
            MediaStore.MediaColumns.DISPLAY_NAME,
            MediaStore.MediaColumns.DATE_ADDED,
            MediaStore.MediaColumns.SIZE,
            MediaStore.MediaColumns.MIME_TYPE
        )

        val sortOrder = "${MediaStore.MediaColumns.DATE_ADDED} DESC LIMIT 30"

        try {
            val cursor = context.contentResolver.query(
                uri,
                projection,
                null,
                null,
                sortOrder
            )

            cursor?.use {
                val idColumn = it.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
                val nameColumn = it.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)
                val dateColumn = it.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_ADDED)
                val sizeColumn = it.getColumnIndexOrThrow(MediaStore.MediaColumns.SIZE)
                val mimeColumn = it.getColumnIndexOrThrow(MediaStore.MediaColumns.MIME_TYPE)

                while (it.moveToNext()) {
                    val id = it.getLong(idColumn)
                    val name = it.getString(nameColumn) ?: "media_$id"
                    val date = it.getLong(dateColumn) * 1000L // Convert seconds to ms
                    val size = it.getLong(sizeColumn)
                    val mimeType = it.getString(mimeColumn) ?: (if (isVideo) "video/*" else "image/*")
                    val contentUri = ContentUris.withAppendedId(uri, id).toString()

                    val category = categorize(name, mimeType, isVideo)

                    list.add(
                        MediaItem(
                            title = name,
                            path = contentUri,
                            duration = 0L,
                            mediaType = mimeType,
                            uri = contentUri,
                            fileName = name,
                            filePath = contentUri,
                            mimeType = mimeType,
                            size = size,
                            category = category,
                            timestamp = if (date > 0) date else System.currentTimeMillis()
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun categorize(name: String, mimeType: String, isVideo: Boolean): String {
        val lower = name.lowercase()
        return when {
            lower.contains("screenshot") -> "Screenshot"
            lower.contains("nature") || lower.contains("tree") || lower.contains("forest") || lower.contains("sky") -> "Nature"
            lower.contains("urban") || lower.contains("street") || lower.contains("city") -> "Urban"
            lower.contains("rant") || lower.contains("speech") || lower.contains("voice") -> "Rant"
            isVideo -> "Raw Footage"
            else -> "Media"
        }
    }
}
