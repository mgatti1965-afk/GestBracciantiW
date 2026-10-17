package com.aistudio.gestbracciantiw.data.repository.memory

import com.aistudio.gestbracciantiw.data.entity.Worker
import com.aistudio.gestbracciantiw.data.entity.WorkerYearConfig
import com.aistudio.gestbracciantiw.data.model.WorkerYearStats
import com.aistudio.gestbracciantiw.data.repository.WorkerRepository
import com.aistudio.gestbracciantiw.data.repository.WorkerYearConfigRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

class InMemoryWorkerYearConfigRepository(
    private var workerRepository: WorkerRepository? = null
) : WorkerYearConfigRepository {

    private val configsState = MutableStateFlow<List<WorkerYearConfig>>(emptyList())

    fun setWorkerRepository(repository: WorkerRepository) {
        this.workerRepository = repository
    }

    override fun getWorkersForYear(yearId: Int): Flow<List<Worker>> {
        val repo = workerRepository ?: return flowOf(emptyList())
        return combine(configsState, repo.allWorkers) { configs, allWorkers ->
            val workerIds = configs.filter { it.harvestYearId == yearId }.map { it.workerId }.toSet()
            allWorkers.filter { it.id in workerIds }
        }
    }

    override suspend fun getConfig(workerId: Long, yearId: Int): WorkerYearConfig? {
        return configsState.value.firstOrNull { it.workerId == workerId && it.harvestYearId == yearId }
    }

    override suspend fun insertConfig(config: WorkerYearConfig) {
        val filtered = configsState.value.filterNot { it.workerId == config.workerId && it.harvestYearId == config.harvestYearId }
        configsState.value = filtered + config
    }

    override suspend fun getConfigsForYear(yearId: Int): List<WorkerYearConfig> {
        return configsState.value.filter { it.harvestYearId == yearId }
    }

    override fun getWorkerStatsForYear(yearId: Int): Flow<List<WorkerYearStats>> {
        val repo = workerRepository ?: return flowOf(emptyList())
        return combine(configsState, repo.allWorkers) { configs, allWorkers ->
            configs.filter { it.harvestYearId == yearId }.mapNotNull { conf ->
                val worker = allWorkers.firstOrNull { it.id == conf.workerId } ?: return@mapNotNull null
                WorkerYearStats(
                    workerId = worker.id,
                    name = worker.name,
                    surname = worker.surname,
                    hourlyRate = conf.hourlyRate,
                    totalHours = 0.0,
                    totalEarnings = 0.0,
                    extraHourlyRate = conf.extraHourlyRate,
                    holidayHourlyRate = conf.holidayHourlyRate
                )
            }
        }
    }

    override fun getWorkerStatsForRange(yearId: Int, startDate: Long, endDate: Long): Flow<List<WorkerYearStats>> {
        return getWorkerStatsForYear(yearId)
    }
}
