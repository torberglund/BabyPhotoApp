// File: app/src/main/java/com/example/babyphotoapp/ui/PhotoViewModel.kt
package com.example.babyphotoapp.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.babyphotoapp.data.PhotoStore
import com.example.babyphotoapp.sync.SyncRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job

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

    private val _snackFlow = MutableSharedFlow<String>()
    val snackFlow: SharedFlow<String> = _snackFlow

    init {
        loadToday()
    }

    fun loadToday() = viewModelScope.launch {
        val context = getApplication<Application>()
        val uris = PhotoStore.listTodayUris(context)
        val todayKey = repo.todayKey()
        val index = repo.loadIndex()
        val entry = index.days[todayKey]

        // ★ if nothing has been marked active yet but we have at least one photo, auto-mark the first
        if (entry == null && uris.isNotEmpty()) {
            val firstFile = PhotoStore.fileNameFromUri(context, uris.first())
            repo.markActive(
                dateKey = todayKey,
                fileName = firstFile,
                deviceId = PhotoStore.deviceId(context)
            )
            // reload after marking

            return@launch
        }

        // build UI state
        val activeFile = entry?.active
        val shotsUi = uris.map { uri ->
            val name = PhotoStore.fileNameFromUri(context, uri)
            ShotUi(
                uri = uri,
                fileName = name,
                isActive = name == activeFile
            )
        }

        _uiState.value = ReviewUiState(
            shots = shotsUi,
            isTakenToday = activeFile != null
        )
    }

    fun onShotClicked(fileName: String) = viewModelScope.launch {
        val currentActive = uiState.value.shots.find { it.isActive }?.fileName
        if (currentActive == fileName) return@launch

        repo.markActive(
            dateKey = repo.todayKey(),
            fileName = fileName,
            deviceId = PhotoStore.deviceId(getApplication())
        )
        _snackFlow.emit("Marked $fileName as active")
        loadToday()
    }
}
