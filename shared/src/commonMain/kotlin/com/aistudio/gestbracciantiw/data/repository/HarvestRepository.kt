package com.aistudio.gestbracciantiw.data.repository

import com.aistudio.gestbracciantiw.data.entity.HarvestYear
import kotlinx.coroutines.flow.Flow

interface HarvestRepository {
    val allYears: Flow<List<HarvestYear>>

    suspend fun getCurrentYear(): HarvestYear?
    suspend fun createNewYear(
        year: Int,
        notes: String = "",
        migrateFromYear: Int? = null,
        migrateWorkers: Boolean = false,
        migrateGroups: Boolean = false
    )
    suspend fun switchYear(yearId: Int)
    suspend fun deselectYear()
    suspend fun deleteYear(yearId: Int)
}
