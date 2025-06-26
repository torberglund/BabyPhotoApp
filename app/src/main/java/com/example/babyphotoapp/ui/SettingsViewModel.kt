// app/src/main/java/com/example/babyphotoapp/ui/SettingsViewModel.kt
package com.example.babyphotoapp.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.babyphotoapp.data.SettingsRepository
import com.example.babyphotoapp.sync.SyncRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class SettingsUiState(
    val host: String       = "",
    val port: String       = "22",    // keep it a String for easy TextField binding
    val user: String       = "",
    val pass: String       = "",
    val remotePath: String = ""
)

sealed class SyncStatus {
    object Idle   : SyncStatus()
    object Loading: SyncStatus()
    object Success: SyncStatus()
    data class Error(val msg: String): SyncStatus()
}

class SettingsViewModel(app: Application): AndroidViewModel(app) {
    private val settingsRepo = SettingsRepository(app)
    private val syncRepo     = SyncRepository(app)

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private val _syncStatus = MutableStateFlow<SyncStatus>(SyncStatus.Idle)
    val syncStatus: StateFlow<SyncStatus> = _syncStatus.asStateFlow()

    init {
        // load stored prefs into our UiState
        viewModelScope.launch {
            combine(
                settingsRepo.host,
                settingsRepo.port.map { it.toString() },
                settingsRepo.user,
                settingsRepo.pass,
                settingsRepo.remotePath
            ) { host, port, user, pass, rpath ->
                SettingsUiState(host, port, user, pass, rpath)
            }.collect { _uiState.value = it }
        }
    }

    fun onHostChange(v: String) = viewModelScope.launch { settingsRepo.updateHost(v) }
    fun onPortChange(v: String) {
        _uiState.update { it.copy(port = v) }
        v.toIntOrNull()?.let { viewModelScope.launch { settingsRepo.updatePort(it) } }
    }
    fun onUserChange(v: String) = viewModelScope.launch { settingsRepo.updateUser(v) }
    fun onPassChange(v: String) = viewModelScope.launch { settingsRepo.updatePass(v) }
    fun onRemotePathChange(v: String) = viewModelScope.launch { settingsRepo.updateRemotePath(v) }

    fun syncNow() = viewModelScope.launch {
        _syncStatus.value = SyncStatus.Loading
        try {
            val s = uiState.value
            syncRepo.syncNow(
                host       = s.host,
                port       = s.port.toIntOrNull() ?: 22,
                username   = s.user,
                password   = s.pass,
                remotePath = s.remotePath
            )
            _syncStatus.value = SyncStatus.Success
        } catch (t: Throwable) {
            _syncStatus.value = SyncStatus.Error(t.message ?: "Unknown error")
        }
    }
}
