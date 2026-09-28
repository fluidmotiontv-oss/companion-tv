package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.CompanionRepository
import com.example.data.CrewStatus
import com.example.data.MediaItem
import com.example.logic.MediaScanner
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class CompanionViewModel(
    private val repository: CompanionRepository,
    private val scanner: MediaScanner
) : ViewModel() {

    val crewStatus: StateFlow<CrewStatus?> = repository.crewStatus.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val allMedia: StateFlow<List<MediaItem>> = repository.allMedia.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _selectedCategory = MutableStateFlow("ALL")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    val filteredMedia: StateFlow<List<MediaItem>> = combine(allMedia, _selectedCategory) { mediaList, category ->
        if (category == "ALL") {
            mediaList
        } else {
            mediaList.filter { it.category.equals(category, ignoreCase = true) }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _activeConsentItem = MutableStateFlow<MediaItem?>(null)
    val activeConsentItem: StateFlow<MediaItem?> = _activeConsentItem.asStateFlow()

    private val _activePlaybackItem = MutableStateFlow<MediaItem?>(null)
    val activePlaybackItem: StateFlow<MediaItem?> = _activePlaybackItem.asStateFlow()

    fun playMedia(item: MediaItem) {
        _activePlaybackItem.value = item
    }

    fun stopPlayback() {
        _activePlaybackItem.value = null
    }

    fun selectCategory(category: String) {
        _selectedCategory.value = category
    }

    fun promptConsent(item: MediaItem) {
        _activeConsentItem.value = item
    }

    fun dismissConsentPrompt() {
        _activeConsentItem.value = null
    }

    fun performHandshake(mediaUri: String) {
        viewModelScope.launch {
            val status = CrewStatus(
                id = 1,
                isCrew = true,
                handshakeTimestamp = System.currentTimeMillis(),
                handshakeMediaUri = mediaUri
            )
            repository.updateCrewStatus(status)
        }
    }

    fun scanFiles() {
        viewModelScope.launch {
            _isScanning.value = true
            try {
                val results = scanner.scanLocalMedia()
                results.forEach { item ->
                    repository.insertMedia(item)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isScanning.value = false
            }
        }
    }

    fun provideConsent(id: Long) {
        viewModelScope.launch {
            repository.updateConsent(id, true)
            if (_activeConsentItem.value?.id == id) {
                _activeConsentItem.value = null
            }
        }
    }

    fun routeMedia(id: Long) {
        viewModelScope.launch {
            repository.markRouted(id)
        }
    }

    fun deleteMedia(id: Long) {
        viewModelScope.launch {
            repository.deleteMedia(id)
            if (_activeConsentItem.value?.id == id) {
                _activeConsentItem.value = null
            }
        }
    }

    fun addStagedMedia(fileName: String, category: String, sizeBytes: Long = 1024L * 1024L) {
        viewModelScope.launch {
            val localPath = "local://staged_${System.currentTimeMillis()}_$fileName"
            val type = if (category == "Screenshot") "image/png" else "video/mp4"
            val item = MediaItem(
                title = fileName,
                path = localPath,
                duration = 30000L,
                mediaType = type,
                uri = localPath,
                fileName = fileName,
                mimeType = type,
                size = sizeBytes,
                category = category,
                stagingStatus = "STAGED",
                timestamp = System.currentTimeMillis(),
                isConsented = false,
                isRouted = false
            )
            repository.insertMedia(item)
        }
    }

    fun clearAll() {
        viewModelScope.launch {
            repository.clearAllMedia()
        }
    }
}
