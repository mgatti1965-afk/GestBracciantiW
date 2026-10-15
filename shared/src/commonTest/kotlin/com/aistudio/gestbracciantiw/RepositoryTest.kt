package com.aistudio.gestbracciantiw

import com.aistudio.gestbracciantiw.data.entity.Worker
import com.aistudio.gestbracciantiw.data.repository.memory.InMemoryHarvestRepository
import com.aistudio.gestbracciantiw.data.repository.memory.InMemoryWorkerRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class RepositoryTest {

    @Test
    fun testHarvestRepository() = runTest {
        val repo = InMemoryHarvestRepository()
        val current = repo.getCurrentYear()
        assertNotNull(current)
        assertEquals(2026, current.id)

        repo.createNewYear(2027, notes = "Anno nuovo")
        val newCurrent = repo.getCurrentYear()
        assertEquals(2027, newCurrent?.id)
    }

    @Test
    fun testWorkerRepository() = runTest {
        val repo = InMemoryWorkerRepository()
        val id = repo.insertWorker(Worker(name = "Giuseppe", surname = "Verdi"))
        val worker = repo.getWorkerById(id)
        assertNotNull(worker)
        assertEquals("Giuseppe", worker.name)

        val active = repo.activeWorkers.first()
        assertEquals(1, active.size)
    }
}
