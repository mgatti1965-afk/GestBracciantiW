package com.aistudio.gestbracciantiw.data.repository

import com.aistudio.gestbracciantiw.data.entity.Worker
import com.aistudio.gestbracciantiw.data.entity.WorkerYearConfig
import com.aistudio.gestbracciantiw.data.model.WorkerYearStats
import kotlinx.coroutines.flow.Flow

interface WorkerYearConfigRepository {
    fun getWorkersForYear(yearId: Int): Flow<List<Worker>>
    suspend fun getConfig(workerId: Long, yearId: Int): WorkerYearConfig?
    suspend fun insertConfig(config: WorkerYearConfig)
    suspend fun getConfigsForYear(yearId: Int): List<WorkerYearConfig>
    fun getWorkerStatsForYear(yearId: Int): Flow<List<WorkerYearStats>>
    fun getWorkerStatsForRange(yearId: Int, startDate: Long, endDate: Long): Flow<List<WorkerYearStats>>
}
