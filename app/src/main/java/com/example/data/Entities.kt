package com.example.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "crew_status")
data class CrewStatus(
    @PrimaryKey val id: Int = 1,
    val isCrew: Boolean = false,
    val handshakeTimestamp: Long? = null,
    val handshakeMediaUri: String? = null
)

/**
 * Entity representing local media files tracked and staged by the FluidmotionTV Companion.
 * Stores core media properties: id, title, path, duration, mediaType, along with
 * staging status and sovereign user consent metadata.
 */
@Entity(
    tableName = "media_items",
    indices = [Index(value = ["path"], unique = false)]
)
data class MediaItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String = "",
    val path: String = "",
    val duration: Long = 0L,
    val mediaType: String = "video",
    val uri: String = "",
    val fileName: String = "",
    val filePath: String? = null,
    val mimeType: String? = null,
    val size: Long = 0L,
    val durationMs: Long = 0L,
    val category: String = "Media", // e.g., "Nature", "Urban", "Rant", "Screenshot", "Raw Footage"
    val stagingStatus: String = "STAGED", // e.g., "STAGED", "CONSENT_PENDING", "CONSENTED", "ROUTED", "ARCHIVED"
    val timestamp: Long = System.currentTimeMillis(),
    val isConsented: Boolean = false,
    val isRouted: Boolean = false,
    val notes: String? = null
) {
    val displayTitle: String
        get() = title.ifBlank { fileName.ifBlank { "Untitled Media" } }

    val resolvedPath: String
        get() = path.ifBlank { filePath ?: uri }

    val resolvedDuration: Long
        get() = if (duration > 0) duration else durationMs

    val resolvedMediaType: String
        get() = if (mediaType.isNotBlank() && mediaType != "video") mediaType else (mimeType ?: "video/mp4")
}
