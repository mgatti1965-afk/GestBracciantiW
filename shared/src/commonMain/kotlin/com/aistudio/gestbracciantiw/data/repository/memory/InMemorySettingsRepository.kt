package com.aistudio.gestbracciantiw.data.repository.memory

import com.aistudio.gestbracciantiw.data.repository.Settings
import com.aistudio.gestbracciantiw.data.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class InMemorySettingsRepository : SettingsRepository {
    private val settingsState = MutableStateFlow(Settings())

    override val settings: Flow<Settings> = settingsState.asStateFlow()

    override suspend fun updateSettings(settings: Settings) {
        settingsState.value = settings
    }
}
