package com.aistudio.gestbracciantiw.data.repository

import kotlinx.coroutines.flow.Flow

data class Settings(
    val ownerName: String = "",
    val ownerSurname: String = "",
    val extraHoursThreshold: Float = 8.0f,
    val festiveDaysType: Int = 3
)

interface SettingsRepository {
    val settings: Flow<Settings>
    suspend fun updateSettings(settings: Settings)
}
