package com.example.babyphotoapp.sync

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * A repository to load / mutate / save your index.json under
 * <app-files-dir>/index.json
 */
class SyncRepository(private val context: Context) {
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    private val indexFile = File(context.filesDir, "index.json")

    /** Load from disk (or produce an empty IndexJson if missing) */
    suspend fun loadIndex(): IndexJson = withContext(Dispatchers.IO) {
        if (!indexFile.exists()) {
            return@withContext IndexJson(version = 1, days = emptyMap())
        }
        val text = indexFile.readText()
        return@withContext json.decodeFromString(IndexJson.serializer(), text)
    }

    /** Overwrite index.json on disk */
    private suspend fun saveIndex(index: IndexJson) = withContext(Dispatchers.IO) {
        val serialized = json.encodeToString(IndexJson.serializer(), index)
        indexFile.writeText(serialized)
    }

    /**
     * Mark [fileName] (in “yyyy-MM-dd” = [dateKey]) as active.
     *   • demotes previous active → PASSIVE
     *   • promotes this to ACTIVE (adding it to shots if not already present)
     *   • writes back to disk & returns the new IndexJson
     */
    suspend fun markActive(
        dateKey: String,
        fileName: String,
        deviceId: String
    ): IndexJson {
        // 1) load or empty
        val old = loadIndex()
        val days = old.days.toMutableMap()

        // 2) build new DayEntry
        val newEntry = days[dateKey]?.let { entry ->
            // demote previous active
            val demoted = entry.shots.map { shot ->
                if (shot.file == entry.active)
                    shot.copy(status = ShotStatus.PASSIVE)
                else
                    shot
            }
            // either update an existing shot→ACTIVE or append a new one
            val updatedShots = if (demoted.any { it.file == fileName }) {
                demoted.map { shot ->
                    if (shot.file == fileName) shot.copy(status = ShotStatus.ACTIVE)
                    else shot
                }
            } else {
                demoted + Shot(
                    file   = fileName,
                    status = ShotStatus.ACTIVE,
                    device = deviceId
                )
            }
            DayEntry(
                active = fileName,
                shots  = updatedShots
            )
        } ?: run {
            // first‐ever shot of that day
            DayEntry(
                active = fileName,
                shots  = listOf(
                    Shot(
                        file   = fileName,
                        status = ShotStatus.ACTIVE,
                        device = deviceId
                    )
                )
            )
        }

        // 3) put back & persist
        days[dateKey] = newEntry
        val updatedIndex = old.copy(days = days)
        saveIndex(updatedIndex)
        return updatedIndex
    }

    /** Convenience: “yyyy-MM-dd” for today */
    fun todayKey(): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
}
