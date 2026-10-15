package com.aistudio.gestbracciantiw.data.repository.memory

import com.aistudio.gestbracciantiw.data.entity.Worker
import com.aistudio.gestbracciantiw.data.model.WorkerWithRate
import com.aistudio.gestbracciantiw.data.repository.WorkerRepository
import com.aistudio.gestbracciantiw.data.repository.WorkerYearConfigRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class InMemoryWorkerRepository(
    private val configRepository: WorkerYearConfigRepository? = null
) : WorkerRepository {

    private val workersState = MutableStateFlow<List<Worker>>(emptyList())
    private var nextId = 1L

    override val activeWorkers: Flow<List<Worker>> = workersState.map { list -> list.filter { !it.isArchived } }
    override val allWorkers: Flow<List<Worker>> = workersState.asStateFlow()

    override fun getWorkersWithRateForYear(yearId: Int): Flow<List<WorkerWithRate>> {
        if (configRepository == null) {
            return activeWorkers.map { list ->
                list.map { WorkerWithRate(it, hourlyRate = 0.0, extraHourlyRate = 0.0, holidayHourlyRate = 0.0) }
            }
        }
        return activeWorkers.combine(configRepository.getWorkersForYear(yearId)) { active, forYear ->
            val forYearIds = forYear.map { it.id }.toSet()
            active.filter { it.id in forYearIds }.map { worker ->
                val config = configRepository.getConfig(worker.id, yearId)
                WorkerWithRate(
                    worker = worker,
                    hourlyRate = config?.hourlyRate ?: 0.0,
                    extraHourlyRate = config?.extraHourlyRate ?: 0.0,
                    holidayHourlyRate = config?.holidayHourlyRate ?: 0.0
                )
            }
        }
    }

    override suspend fun getAllWorkersStatic(): List<Worker> = workersState.value

    override suspend fun insertWorker(worker: Worker): Long {
        val id = if (worker.id <= 0) nextId++ else worker.id
        val newWorker = worker.copy(id = id)
        workersState.value = workersState.value + newWorker
        return id
    }

    override suspend fun updateWorker(worker: Worker) {
        workersState.value = workersState.value.map { if (it.id == worker.id) worker else it }
    }

    override suspend fun deleteWorker(worker: Worker) {
        workersState.value = workersState.value.filterNot { it.id == worker.id }
    }

    override suspend fun getWorkerById(id: Long): Worker? {
        return workersState.value.firstOrNull { it.id == id }
    }

    override suspend fun mergeWorkers(sourceId: Long, targetId: Long) {
        val source = getWorkerById(sourceId) ?: return
        updateWorker(source.copy(isArchived = true))
    }
}
