package com.aistudio.gestbracciantiw.data.repository.memory

import com.aistudio.gestbracciantiw.data.entity.Plantation
import com.aistudio.gestbracciantiw.data.repository.PlantationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

class InMemoryPlantationRepository : PlantationRepository {
    private val plantationsState = MutableStateFlow<List<Plantation>>(emptyList())
    private var nextId = 1L

    override val activePlantations: Flow<List<Plantation>> = plantationsState.map { list -> list.filter { !it.isArchived } }
    override val allPlantations: Flow<List<Plantation>> = plantationsState.asStateFlow()

    override suspend fun insertPlantation(plantation: Plantation): Long {
        val id = if (plantation.id <= 0) nextId++ else plantation.id
        val newPlantation = plantation.copy(id = id)
        plantationsState.value = plantationsState.value + newPlantation
        return id
    }

    override suspend fun updatePlantation(plantation: Plantation) {
        plantationsState.value = plantationsState.value.map { if (it.id == plantation.id) plantation else it }
    }

    override suspend fun deletePlantation(plantation: Plantation) {
        plantationsState.value = plantationsState.value.filterNot { it.id == plantation.id }
    }
}
