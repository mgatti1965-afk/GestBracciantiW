package com.aistudio.gestbracciantiw

import com.aistudio.gestbracciantiw.data.repository.memory.InMemoryHarvestRepository
import com.aistudio.gestbracciantiw.data.repository.memory.InMemorySettingsRepository
import com.aistudio.gestbracciantiw.data.repository.memory.InMemoryWorkLogRepository
import com.aistudio.gestbracciantiw.data.repository.memory.InMemoryWorkerGroupRepository
import com.aistudio.gestbracciantiw.data.repository.memory.InMemoryWorkerRepository
import com.aistudio.gestbracciantiw.data.repository.memory.InMemoryWorkerYearConfigRepository
import com.aistudio.gestbracciantiw.ui.viewmodel.HarvestViewModel
import com.aistudio.gestbracciantiw.ui.viewmodel.WorkLogViewModel
import com.aistudio.gestbracciantiw.ui.viewmodel.WorkerGroupViewModel
import com.aistudio.gestbracciantiw.ui.viewmodel.WorkerViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class ViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testHarvestViewModel() = runTest {
        val repo = InMemoryHarvestRepository()
        val viewModel = HarvestViewModel(repo)
        advanceUntilIdle()

        var successCalled = false
        viewModel.createYear(2028, notes = "Anno 2028", onSuccess = { successCalled = true })
        advanceUntilIdle()

        assertEquals(true, successCalled)
        assertEquals(2028, viewModel.currentYear.value?.id)
    }

    @Test
    fun testWorkerViewModel() = runTest {
        val configRepo = InMemoryWorkerYearConfigRepository()
        val workerRepo = InMemoryWorkerRepository(configRepo)
        configRepo.setWorkerRepository(workerRepo)

        val viewModel = WorkerViewModel(workerRepo, configRepo)

        backgroundScope.launch { viewModel.workersForCurrentYear.collect() }

        viewModel.addWorkerToYear(
            name = "Mario",
            surname = "Rossi",
            phoneNumber = "123456",
            hourlyRate = 10.0,
            extraRate = 12.0,
            holidayRate = 15.0,
            yearId = 2026
        )
        advanceUntilIdle()

        viewModel.setSelectedYear(2026)
        advanceUntilIdle()

        val workers = viewModel.workersForCurrentYear.value
        assertEquals(1, workers.size)
        assertEquals("Mario", workers[0].name)
    }

    @Test
    fun testWorkLogViewModel() = runTest {
        val logRepo = InMemoryWorkLogRepository()
        val configRepo = InMemoryWorkerYearConfigRepository()
        val settingsRepo = InMemorySettingsRepository()
        val viewModel = WorkLogViewModel(logRepo, configRepo, settingsRepo)

        backgroundScope.launch { viewModel.allLogs.collect() }

        viewModel.setSelectedYear(2026)
        advanceUntilIdle()

        viewModel.saveLog(
            workerId = 1L,
            yearId = 2026,
            date = 1000000L,
            morningStart = "08:00",
            morningEnd = "12:00",
            afternoonStart = "13:00",
            afternoonEnd = "17:00"
        )
        advanceUntilIdle()

        val logs = viewModel.allLogs.value
        assertEquals(1, logs.size)
        assertEquals(8.0, logs[0].totalHours)
    }

    @Test
    fun testWorkerGroupViewModel() = runTest {
        val groupRepo = InMemoryWorkerGroupRepository()
        val viewModel = WorkerGroupViewModel(groupRepo)

        backgroundScope.launch { viewModel.groupsForYear.collect() }

        viewModel.setSelectedYear(2026)
        advanceUntilIdle()

        viewModel.createGroup("Squadra Raccolta")
        advanceUntilIdle()

        val groups = viewModel.groupsForYear.value
        assertEquals(1, groups.size)
        assertEquals("Squadra Raccolta", groups[0].name)
    }
}
