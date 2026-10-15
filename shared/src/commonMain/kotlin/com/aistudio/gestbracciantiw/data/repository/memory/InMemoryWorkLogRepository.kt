package com.aistudio.gestbracciantiw.data.repository.memory

import com.aistudio.gestbracciantiw.data.entity.WorkLog
import com.aistudio.gestbracciantiw.data.repository.WorkLogRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

class InMemoryWorkLogRepository : WorkLogRepository {
    private val logsState = MutableStateFlow<List<WorkLog>>(emptyList())
    private var nextId = 1L

    override fun getLogsForYear(yearId: Int): Flow<List<WorkLog>> {
        return logsState.map { list -> list.filter { it.harvestYearId == yearId } }
    }

    override suspend fun insertLog(workLog: WorkLog): Long {
        val id = if (workLog.id <= 0) nextId++ else workLog.id
        val newLog = workLog.copy(id = id)
        logsState.value = logsState.value + newLog
        return id
    }

    override suspend fun updateLog(workLog: WorkLog) {
        logsState.value = logsState.value.map { if (it.id == workLog.id) workLog else it }
    }

    override suspend fun deleteLog(workLog: WorkLog) {
        logsState.value = logsState.value.filterNot { it.id == workLog.id }
    }

    override suspend fun getLogById(id: Long): WorkLog? {
        return logsState.value.firstOrNull { it.id == id }
    }

    override suspend fun getLogsByDate(date: Long, yearId: Int): List<WorkLog> {
        return logsState.value.filter { it.date == date && it.harvestYearId == yearId }
    }

    override suspend fun fillMissingRates() {
        // No-op for in-memory
    }
}
