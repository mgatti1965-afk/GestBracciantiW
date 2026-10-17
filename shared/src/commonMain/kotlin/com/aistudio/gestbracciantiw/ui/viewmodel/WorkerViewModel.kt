package com.aistudio.gestbracciantiw.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aistudio.gestbracciantiw.data.entity.Worker
import com.aistudio.gestbracciantiw.data.entity.WorkerYearConfig
import com.aistudio.gestbracciantiw.data.model.WorkerWithRate
import com.aistudio.gestbracciantiw.data.repository.WorkerRepository
import com.aistudio.gestbracciantiw.data.repository.WorkerYearConfigRepository
import com.aistudio.gestbracciantiw.ui.utils.capitalizeWords
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class WorkerViewModel(
    private val workerRepository: WorkerRepository,
    private val configRepository: WorkerYearConfigRepository
) : ViewModel() {

    private val _selectedYearId = MutableStateFlow<Int?>(null)

    val workersWithRateForCurrentYear: StateFlow<List<WorkerWithRate>> = _selectedYearId
        .filterNotNull()
        .flatMapLatest { yearId ->
            workerRepository.getWorkersWithRateForYear(yearId)
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val workersForCurrentYear: StateFlow<List<Worker>> = _selectedYearId
        .flatMapLatest { yearId ->
            if (yearId != null) {
                configRepository.getWorkersForYear(yearId)
            } else {
                MutableStateFlow(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setSelectedYear(yearId: Int) {
        _selectedYearId.value = yearId
    }

    val duplicatesFound: StateFlow<List<List<Worker>>> = workerRepository.allWorkers
        .map { workers ->
            workers.groupBy { (it.surname + it.name).lowercase().replace(" ", "") }
                .filter { it.value.size > 1 }
                .values.toList()
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _uiEvent = MutableSharedFlow<String>()
    val uiEvent = _uiEvent.asSharedFlow()

    fun mergeDuplicateGroup(group: List<Worker>) {
        viewModelScope.launch {
            if (group.size < 2) return@launch

            val target = group.minByOrNull { it.id } ?: return@launch
            val sources = group.filter { it.id != target.id }

            sources.forEach { source ->
                workerRepository.mergeWorkers(source.id, target.id)
            }

            _uiEvent.emit("Profili uniti correttamente")

            val currentYear = _selectedYearId.value
            _selectedYearId.value = null
            _selectedYearId.value = currentYear
        }
    }

    fun addWorkerToYear(
        name: String,
        surname: String,
        phoneNumber: String,
        hourlyRate: Double,
        extraRate: Double,
        holidayRate: Double,
        yearId: Int
    ) {
        viewModelScope.launch {
            val formattedName = name.trim().capitalizeWords()
            val formattedSurname = surname.trim().capitalizeWords()

            val allWorkers = workerRepository.getAllWorkersStatic()
            val existingWorker = allWorkers.find {
                it.name.equals(formattedName, ignoreCase = true) && it.surname.equals(formattedSurname, ignoreCase = true)
            }

            val workerId = if (existingWorker != null) {
                if (phoneNumber.isNotBlank() && existingWorker.phoneNumber != phoneNumber) {
                    workerRepository.updateWorker(existingWorker.copy(phoneNumber = phoneNumber))
                }
                existingWorker.id
            } else {
                workerRepository.insertWorker(Worker(name = formattedName, surname = formattedSurname, phoneNumber = phoneNumber))
            }

            configRepository.insertConfig(
                WorkerYearConfig(
                    workerId = workerId,
                    harvestYearId = yearId,
                    hourlyRate = hourlyRate,
                    extraHourlyRate = extraRate,
                    holidayHourlyRate = holidayRate
                )
            )
        }
    }

    suspend fun getWorkerConfig(workerId: Long, yearId: Int): WorkerYearConfig? {
        return configRepository.getConfig(workerId, yearId)
    }

    fun updateWorkerInfo(
        workerId: Long,
        name: String,
        surname: String,
        phoneNumber: String,
        yearId: Int,
        newRate: Double,
        extraRate: Double,
        holidayRate: Double
    ) {
        viewModelScope.launch {
            val formattedName = name.trim().capitalizeWords()
            val formattedSurname = surname.trim().capitalizeWords()

            val existingWorker = workerRepository.getWorkerById(workerId)
            if (existingWorker != null) {
                workerRepository.updateWorker(
                    existingWorker.copy(
                        name = formattedName,
                        surname = formattedSurname,
                        phoneNumber = phoneNumber
                    )
                )
            }
            configRepository.insertConfig(
                WorkerYearConfig(
                    workerId = workerId,
                    harvestYearId = yearId,
                    hourlyRate = newRate,
                    extraHourlyRate = extraRate,
                    holidayHourlyRate = holidayRate
                )
            )

            val currentYear = _selectedYearId.value
            _selectedYearId.value = null
            _selectedYearId.value = currentYear
        }
    }

    fun deleteWorker(worker: Worker) {
        viewModelScope.launch {
            workerRepository.deleteWorker(worker)
            val currentYear = _selectedYearId.value
            _selectedYearId.value = null
            _selectedYearId.value = currentYear
        }
    }

    fun copyWorkersFromPreviousYear(currentYearId: Int, onResult: (Int) -> Unit) {
        viewModelScope.launch {
            val previousYearId = currentYearId - 1
            val previousYearConfigs = configRepository.getConfigsForYear(previousYearId)
            val currentYearConfigs = configRepository.getConfigsForYear(currentYearId)
            val currentWorkerIds = currentYearConfigs.map { it.workerId }.toSet()

            val missingConfigs = previousYearConfigs.filter { it.workerId !in currentWorkerIds }

            missingConfigs.forEach { config ->
                configRepository.insertConfig(
                    WorkerYearConfig(
                        workerId = config.workerId,
                        harvestYearId = currentYearId,
                        hourlyRate = config.hourlyRate,
                        extraHourlyRate = config.extraHourlyRate,
                        holidayHourlyRate = config.holidayHourlyRate
                    )
                )
            }

            _selectedYearId.value = null
            _selectedYearId.value = currentYearId

            onResult(missingConfigs.size)
        }
    }
}
