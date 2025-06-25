package com.example.babyphotoapp.sync

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class IndexJson(
    val version: Int = 1,
    val days: Map<String, DayEntry> = emptyMap()
)

@Serializable
data class DayEntry(
    val active: String,
    val shots: List<Shot>
)

@Serializable
data class Shot(
    val file: String,
    val status: ShotStatus,
    val device: String
)

@Serializable
enum class ShotStatus {
    @SerialName("active") ACTIVE,
    @SerialName("passive") PASSIVE
}
