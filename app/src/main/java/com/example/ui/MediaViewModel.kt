package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.MediaItem
import com.example.data.MediaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI State representing the state of media lists.
 */
sealed interface MediaListUiState {
    object Loading : MediaListUiState
    data class Success(val items: List<MediaItem>) : MediaListUiState
    data class Error(val message: String) : MediaListUiState
}

/**
 * MediaViewModel interacts with MediaRepository to expose reactive StateFlows
 * of local media items for Jetpack Compose UI data binding and management.
 */
class MediaViewModel @Inject constructor(
    private val repository: MediaRepository
) : ViewModel() {

    // Raw stream of all local media items
    val allMedia: StateFlow<List<MediaItem>> = repository.allMedia.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Aliased for flexible UI data binding
    val mediaItems: StateFlow<List<MediaItem>> = allMedia

    // Categorized and staged reactive streams
    val stagedMedia: StateFlow<List<MediaItem>> = repository.stagedMedia.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val routedMedia: StateFlow<List<MediaItem>> = repository.routedMedia.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val consentedMedia: StateFlow<List<MediaItem>> = repository.consentedMedia.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val unconsentedMedia: StateFlow<List<MediaItem>> = repository.unconsentedMedia.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Selected category filter
    private val _selectedCategory = MutableStateFlow("ALL")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    // Selected staging status filter
    private val _selectedStagingStatus = MutableStateFlow("ALL")
    val selectedStagingStatus: StateFlow<String> = _selectedStagingStatus.asStateFlow()

    // Search / query text
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Filtered media list combining category, status, and search query
    val filteredMedia: StateFlow<List<MediaItem>> = combine(
        allMedia,
        _selectedCategory,
        _selectedStagingStatus,
        _searchQuery
    ) { items: List<MediaItem>, category: String, stagingStatus: String, query: String ->
        items.filter { item ->
            val matchesCategory = category == "ALL" || item.category.equals(category, ignoreCase = true)
            val matchesStaging = stagingStatus == "ALL" || item.stagingStatus.equals(stagingStatus, ignoreCase = true)
            val matchesQuery = query.isBlank() ||
                    item.title.contains(query, ignoreCase = true) ||
                    item.fileName.contains(query, ignoreCase = true) ||
                    item.category.contains(query, ignoreCase = true)

            matchesCategory && matchesStaging && matchesQuery
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // UI state wrapped flow for full loading/success states
    val uiState: StateFlow<MediaListUiState> = combine(
        filteredMedia,
        _selectedCategory
    ) { items: List<MediaItem>, _: String ->
        MediaListUiState.Success(items) as MediaListUiState
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MediaListUiState.Loading
    )

    // Active item selected for detailed preview or consent dialog
    private val _selectedMediaItem = MutableStateFlow<MediaItem?>(null)
    val selectedMediaItem: StateFlow<MediaItem?> = _selectedMediaItem.asStateFlow()

    fun selectCategory(category: String) {
        _selectedCategory.value = category
    }

    fun selectStagingStatus(status: String) {
        _selectedStagingStatus.value = status
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectMediaItem(item: MediaItem?) {
        _selectedMediaItem.value = item
    }

    fun insertMedia(
        title: String,
        path: String,
        duration: Long = 0L,
        mediaType: String = "video/mp4",
        category: String = "General",
        stagingStatus: String = "STAGED"
    ) {
        viewModelScope.launch {
            val item = MediaItem(
                title = title,
                path = path,
                duration = duration,
                mediaType = mediaType,
                uri = path,
                fileName = title,
                mimeType = mediaType,
                category = category,
                stagingStatus = stagingStatus,
                timestamp = System.currentTimeMillis()
            )
            repository.insertMedia(item)
        }
    }

    fun insertMediaItem(item: MediaItem) {
        viewModelScope.launch {
            repository.insertMedia(item)
        }
    }

    fun updateConsent(id: Long, isConsented: Boolean) {
        viewModelScope.launch {
            repository.updateConsent(id, isConsented)
        }
    }

    fun updateStagingStatus(id: Long, status: String) {
        viewModelScope.launch {
            repository.updateStagingStatus(id, status)
        }
    }

    fun markAsRouted(id: Long) {
        viewModelScope.launch {
            repository.markAsRouted(id)
        }
    }

    fun deleteMedia(id: Long) {
        viewModelScope.launch {
            repository.deleteMedia(id)
            if (_selectedMediaItem.value?.id == id) {
                _selectedMediaItem.value = null
            }
        }
    }

    fun deleteMediaItem(item: MediaItem) {
        viewModelScope.launch {
            repository.deleteMedia(item)
            if (_selectedMediaItem.value?.id == item.id) {
                _selectedMediaItem.value = null
            }
        }
    }

    fun clearAllMedia() {
        viewModelScope.launch {
            repository.clearAllMedia()
            _selectedMediaItem.value = null
        }
    }
}
