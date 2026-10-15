package com.aistudio.gestbracciantiw.data.repository

import com.aistudio.gestbracciantiw.data.entity.Worker
import com.aistudio.gestbracciantiw.data.model.WorkerWithRate
import kotlinx.coroutines.flow.Flow

interface WorkerRepository {
    val activeWorkers: Flow<List<Worker>>
    val allWorkers: Flow<List<Worker>>

    fun getWorkersWithRateForYear(yearId: Int): Flow<List<WorkerWithRate>>
    suspend fun getAllWorkersStatic(): List<Worker>
    suspend fun insertWorker(worker: Worker): Long
    suspend fun updateWorker(worker: Worker)
    suspend fun deleteWorker(worker: Worker)
    suspend fun getWorkerById(id: Long): Worker?
    suspend fun mergeWorkers(sourceId: Long, targetId: Long)
}
