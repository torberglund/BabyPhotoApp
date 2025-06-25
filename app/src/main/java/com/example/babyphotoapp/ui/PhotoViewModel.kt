package com.example.babyphotoapp.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.babyphotoapp.sync.SyncRepository
import com.example.babyphotoapp.sync.ShotStatus
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ShotUi(
    val uri: android.net.Uri,
    val fileName: String,
    val isActive: Boolean
)

data class ReviewUiState(
    val shots: List<ShotUi> = emptyList(),
    val isTakenToday: Boolean = false
)

class PhotoViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = SyncRepository(app)
    private val _uiState = MutableStateFlow(ReviewUiState())
    val uiState: StateFlow<ReviewUiState> = _uiState.asStateFlow()

    init {
        loadToday()
    }

    /** Load index.json + photos → UI state */
    fun loadToday() = viewModelScope.launch {
        // 1) read your media-store URIs for today
        val uris = PhotoStore.listTodayUris(getApplication())

        // 2) load index.json, find today's entry
        val index = repo.loadIndex()
        val todayKey = repo.todayKey()
        val entry = index.days[todayKey]
        val activeFile = entry?.active

        // 3) map into ShotUi
        val shotsUi = uris.map { uri ->
            val name = PhotoStore.fileNameFromUri(getApplication(), uri)
            ShotUi(
                uri        = uri,
                fileName   = name,
                isActive   = name == activeFile
            )
        }

        _uiState.value = ReviewUiState(
            shots = shotsUi,
            isTakenToday = activeFile != null
        )
    }

    /** When user taps a shot → markActive, then reload */
    fun onShotClicked(fileName: String) = viewModelScope.launch {
        repo.markActive(repo.todayKey(), fileName, deviceId = PhotoStore.deviceId(getApplication()))
        loadToday()
    }
}
