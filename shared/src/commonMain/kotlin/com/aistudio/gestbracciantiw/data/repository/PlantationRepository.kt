package com.aistudio.gestbracciantiw.data.repository

import com.aistudio.gestbracciantiw.data.entity.Plantation
import kotlinx.coroutines.flow.Flow

interface PlantationRepository {
    val activePlantations: Flow<List<Plantation>>
    val allPlantations: Flow<List<Plantation>>

    suspend fun insertPlantation(plantation: Plantation): Long
    suspend fun updatePlantation(plantation: Plantation)
    suspend fun deletePlantation(plantation: Plantation)
}
