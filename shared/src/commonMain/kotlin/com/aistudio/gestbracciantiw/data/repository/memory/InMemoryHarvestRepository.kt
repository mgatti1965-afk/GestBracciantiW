package com.aistudio.gestbracciantiw.data.repository.memory

import com.aistudio.gestbracciantiw.data.entity.HarvestYear
import com.aistudio.gestbracciantiw.data.repository.HarvestRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class InMemoryHarvestRepository : HarvestRepository {
    private val yearsState = MutableStateFlow<List<HarvestYear>>(
        listOf(HarvestYear(id = 2026, isCurrent = true, notes = "Anno di prova"))
    )

    override val allYears: Flow<List<HarvestYear>> = yearsState.asStateFlow()

    override suspend fun getCurrentYear(): HarvestYear? {
        return yearsState.value.firstOrNull { it.isCurrent }
    }

    override suspend fun createNewYear(
        year: Int,
        notes: String,
        migrateFromYear: Int?,
        migrateWorkers: Boolean,
        migrateGroups: Boolean
    ) {
        val currentList = yearsState.value
        if (currentList.any { it.id == year }) {
            throw IllegalArgumentException("L'anno $year esiste già.")
        }
        val updated = currentList.map { it.copy(isCurrent = false) } + HarvestYear(id = year, isCurrent = true, notes = notes)
        yearsState.value = updated
    }

    override suspend fun switchYear(yearId: Int) {
        yearsState.value = yearsState.value.map { it.copy(isCurrent = (it.id == yearId)) }
    }

    override suspend fun deselectYear() {
        yearsState.value = yearsState.value.map { it.copy(isCurrent = false) }
    }

    override suspend fun deleteYear(yearId: Int) {
        yearsState.value = yearsState.value.filterNot { it.id == yearId }
    }
}
