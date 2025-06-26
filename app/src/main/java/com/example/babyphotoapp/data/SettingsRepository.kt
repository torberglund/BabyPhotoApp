// app/src/main/java/com/example/babyphotoapp/data/SettingsRepository.kt
package com.example.babyphotoapp.data

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore by preferencesDataStore(name = "nas_settings")

class SettingsRepository(private val context: Context) {
    private val ds = context.settingsDataStore

    companion object {
        private val HOST_KEY       = stringPreferencesKey("nas_host")
        private val PORT_KEY       = intPreferencesKey   ("nas_port")
        private val USER_KEY       = stringPreferencesKey("nas_user")
        private val PASS_KEY       = stringPreferencesKey("nas_pass")
        private val REMOTE_PATH_KEY= stringPreferencesKey("nas_remote_path")
    }

    val host: Flow<String> = ds.data.map { it[HOST_KEY] ?: "" }
    val port: Flow<Int>    = ds.data.map { it[PORT_KEY] ?: 22 }
    val user: Flow<String> = ds.data.map { it[USER_KEY] ?: "" }
    val pass: Flow<String> = ds.data.map { it[PASS_KEY] ?: "" }
    val remotePath: Flow<String> = ds.data.map { it[REMOTE_PATH_KEY] ?: "/BabyPhotos/photos" }

    suspend fun updateHost(v: String) = ds.edit { it[HOST_KEY] = v }
    suspend fun updatePort(v: Int)    = ds.edit { it[PORT_KEY] = v }
    suspend fun updateUser(v: String) = ds.edit { it[USER_KEY] = v }
    suspend fun updatePass(v: String) = ds.edit { it[PASS_KEY] = v }
    suspend fun updateRemotePath(v: String) = ds.edit { it[REMOTE_PATH_KEY] = v }
}
