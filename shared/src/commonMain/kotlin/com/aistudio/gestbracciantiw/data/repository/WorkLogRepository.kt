package com.aistudio.gestbracciantiw.data.repository

import com.aistudio.gestbracciantiw.data.entity.WorkLog
import kotlinx.coroutines.flow.Flow

interface WorkLogRepository {
    fun getLogsForYear(yearId: Int): Flow<List<WorkLog>>
    suspend fun insertLog(workLog: WorkLog): Long
    suspend fun updateLog(workLog: WorkLog)
    suspend fun deleteLog(workLog: WorkLog)
    suspend fun getLogById(id: Long): WorkLog?
    suspend fun getLogsByDate(date: Long, yearId: Int): List<WorkLog>
    suspend fun fillMissingRates()
}
