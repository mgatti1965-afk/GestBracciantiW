package com.aistudio.gestbracciantiw.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aistudio.gestbracciantiw.data.entity.WorkLog
import com.aistudio.gestbracciantiw.data.model.WorkerYearStats
import com.aistudio.gestbracciantiw.data.repository.Settings
import com.aistudio.gestbracciantiw.data.repository.SettingsRepository
import com.aistudio.gestbracciantiw.data.repository.WorkLogRepository
import com.aistudio.gestbracciantiw.data.repository.WorkerYearConfigRepository
import com.aistudio.gestbracciantiw.ui.utils.TimeUtils
import com.aistudio.gestbracciantiw.ui.utils.formatCurrency
import com.aistudio.gestbracciantiw.ui.utils.parseTimeToDouble
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class WorkLogViewModel(
    private val workLogRepository: WorkLogRepository,
    private val configRepository: WorkerYearConfigRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _selectedYearId = MutableStateFlow<Int?>(null)
    private val _dateRange = MutableStateFlow<Pair<Long, Long>?>(null)
    private val _currentReferenceDate = MutableStateFlow(0L)
    val currentReferenceDate: StateFlow<Long> = _currentReferenceDate.asStateFlow()

    private val _isCalendarExpanded = MutableStateFlow(false)
    val isCalendarExpanded: StateFlow<Boolean> = _isCalendarExpanded.asStateFlow()

    private val _selectedDate = MutableStateFlow<Long?>(null)
    val selectedDate: StateFlow<Long?> = _selectedDate.asStateFlow()

    private val _globalFestiveDates = MutableStateFlow<Set<Long>>(emptySet())
    val globalFestiveDates: StateFlow<Set<Long>> = _globalFestiveDates.asStateFlow()

    private val _uiEvent = MutableSharedFlow<String>()
    val uiEvent = _uiEvent.asSharedFlow()

    val settings: StateFlow<Settings> = settingsRepository.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Settings())

    fun setSelectedYear(yearId: Int) {
        _selectedYearId.value = yearId
        viewModelScope.launch {
            workLogRepository.fillMissingRates()
            val logs = workLogRepository.getLogsForYear(yearId).first()
            val currentSettings = settings.value
            logs.forEach { log ->
                val needsUpdate = log.totalHours > 0 && (
                    log.totalAmount == 0.0 ||
                    (log.ordinaryHours == 0.0 && log.extraHours == 0.0 && log.holidayHours == 0.0)
                )
                if (needsUpdate) {
                    val updated = calculateAmounts(log, currentSettings)
                    workLogRepository.updateLog(updated)
                }
            }
        }
    }

    fun setDateRange(start: Long?, end: Long?) {
        if (start == null || end == null) {
            _dateRange.value = null
        } else {
            _dateRange.value = Pair(start, end)
        }
    }

    fun updateReferenceDate(date: Long) {
        _currentReferenceDate.value = date
        _selectedDate.value = date
    }

    fun setCalendarExpanded(expanded: Boolean) {
        _isCalendarExpanded.value = expanded
    }

    val yearlyStats: StateFlow<List<WorkerYearStats>> = _selectedYearId
        .flatMapLatest { yearId ->
            if (yearId == null) flowOf(emptyList())
            else configRepository.getWorkerStatsForYear(yearId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allLogs: StateFlow<List<WorkLog>> = _selectedYearId
        .flatMapLatest { yearId ->
            if (yearId == null) flowOf(emptyList())
            else workLogRepository.getLogsForYear(yearId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredLogs: StateFlow<List<WorkLog>> = combine(allLogs, _dateRange) { logs, range ->
        if (range == null) logs
        else logs.filter { it.date in range.first..range.second }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun saveLog(
        id: Long = 0,
        workerId: Long,
        yearId: Int,
        date: Long,
        morningStart: String?,
        morningEnd: String?,
        afternoonStart: String?,
        afternoonEnd: String?
    ) {
        viewModelScope.launch {
            val totalHours = calculateHours(morningStart, morningEnd) + calculateHours(afternoonStart, afternoonEnd)
            val config = configRepository.getConfig(workerId, yearId)
            val currentRate = config?.hourlyRate ?: 0.0
            val extraRate = config?.extraHourlyRate ?: 0.0
            val holidayRate = config?.holidayHourlyRate ?: 0.0

            val existingLog = if (id != 0L) workLogRepository.getLogById(id) else null

            if (existingLog != null && existingLog.hourlyRate != currentRate && existingLog.hourlyRate != 0.0) {
                _uiEvent.emit("Tariffa aggiornata: da ${formatCurrency(existingLog.hourlyRate)} a ${formatCurrency(currentRate)}")
            }

            val currentSettings = settings.value
            val log = WorkLog(
                id = id,
                workerId = workerId,
                harvestYearId = yearId,
                date = date,
                morningStart = morningStart,
                morningEnd = morningEnd,
                afternoonStart = afternoonStart,
                afternoonEnd = afternoonEnd,
                totalHours = totalHours,
                hourlyRate = currentRate,
                extraHourlyRate = extraRate,
                holidayHourlyRate = holidayRate,
                isManualHoliday = existingLog?.isManualHoliday ?: false
            ).let { calculateAmounts(it, currentSettings) }

            if (id == 0L) {
                workLogRepository.insertLog(log)
            } else {
                workLogRepository.updateLog(log)
            }
        }
    }

    private fun calculateAmounts(log: WorkLog, settings: Settings): WorkLog {
        val threshold = settings.extraHoursThreshold.toDouble()
        val festiveType = settings.festiveDaysType

        val isFestive = TimeUtils.isFestive(log.date, log.isManualHoliday, festiveType, globalFestiveDates.value)

        var ordH = 0.0
        var extH = 0.0
        var holH = 0.0
        var ordA = 0.0
        var extA = 0.0
        var holA = 0.0

        if (isFestive) {
            holH = log.totalHours
            holA = log.totalHours * log.holidayHourlyRate
        } else {
            if (log.totalHours > threshold) {
                ordH = threshold
                extH = log.totalHours - threshold
                ordA = threshold * log.hourlyRate
                extA = extH * log.extraHourlyRate
            } else {
                ordH = log.totalHours
                ordA = log.totalHours * log.hourlyRate
            }
        }

        return log.copy(
            totalAmount = ordA + extA + holA,
            ordinaryHours = ordH,
            extraHours = extH,
            holidayHours = holH,
            ordinaryAmount = ordA,
            extraAmount = extA,
            holidayAmount = holA
        )
    }

    fun toggleManualHoliday(log: WorkLog) {
        viewModelScope.launch {
            val updatedLog = log.copy(isManualHoliday = !log.isManualHoliday).let { calculateAmounts(it, settings.value) }
            workLogRepository.updateLog(updatedLog)
        }
    }

    fun toggleHolidayForDate(date: Long) {
        viewModelScope.launch {
            val currentGlobal = _globalFestiveDates.value.toMutableSet()
            val isCurrentlyGlobalFestive = currentGlobal.contains(date)

            if (isCurrentlyGlobalFestive) {
                currentGlobal.remove(date)
            } else {
                currentGlobal.add(date)
            }
            _globalFestiveDates.value = currentGlobal

            val yearId = _selectedYearId.value ?: return@launch
            val logsForDay = workLogRepository.getLogsByDate(date, yearId)
            val affectedCount = logsForDay.size
            if (affectedCount > 0) {
                val targetState = !isCurrentlyGlobalFestive
                val currentSettings = settings.value
                logsForDay.forEach { log ->
                    val updatedLog = log.copy(isManualHoliday = targetState).let { calculateAmounts(it, currentSettings) }
                    workLogRepository.updateLog(updatedLog)
                }
            }

            val toastMsg = if (!isCurrentlyGlobalFestive) {
                if (affectedCount > 0) "Festivo impostato: $affectedCount braccianti ricalcolati"
                else "Giorno impostato come festivo"
            } else {
                if (affectedCount > 0) "Feriale ripristinato: $affectedCount braccianti ricalcolati"
                else "Ripristinato giorno feriale"
            }
            _uiEvent.emit(toastMsg)
        }
    }

    private fun calculateHours(start: String?, end: String?): Double {
        if (start.isNullOrBlank() || end.isNullOrBlank()) return 0.0
        val startVal = parseTimeToDouble(start)
        val endVal = parseTimeToDouble(end)
        return if (endVal > startVal) endVal - startVal else 0.0
    }

    fun deleteLog(log: WorkLog) {
        viewModelScope.launch {
            workLogRepository.deleteLog(log)
        }
    }
}
