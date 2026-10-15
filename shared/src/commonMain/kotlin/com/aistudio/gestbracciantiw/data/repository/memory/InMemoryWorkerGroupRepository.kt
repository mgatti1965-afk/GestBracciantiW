package com.aistudio.gestbracciantiw.data.repository.memory

import com.aistudio.gestbracciantiw.data.entity.Worker
import com.aistudio.gestbracciantiw.data.entity.WorkerGroup
import com.aistudio.gestbracciantiw.data.entity.WorkerGroupCrossRef
import com.aistudio.gestbracciantiw.data.repository.WorkerGroupRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class InMemoryWorkerGroupRepository : WorkerGroupRepository {
    private val groupsState = MutableStateFlow<List<WorkerGroup>>(emptyList())
    private val crossRefsState = MutableStateFlow<List<WorkerGroupCrossRef>>(emptyList())
    private val workersState = MutableStateFlow<List<Worker>>(emptyList())
    private var nextGroupId = 1L

    fun setWorkers(workers: List<Worker>) {
        workersState.value = workers
    }

    override fun getGroupsForYear(yearId: Int): Flow<List<WorkerGroup>> {
        return groupsState.map { list -> list.filter { it.yearId == yearId } }
    }

    override fun getWorkersInGroup(groupId: Long): Flow<List<Worker>> {
        return crossRefsState.map { refs ->
            val workerIds = refs.filter { it.groupId == groupId }.map { it.workerId }.toSet()
            workersState.value.filter { it.id in workerIds }
        }
    }

    override suspend fun createGroup(name: String, yearId: Int) {
        createGroupAndReturnId(name, yearId)
    }

    override suspend fun createGroupAndReturnId(name: String, yearId: Int): Long {
        val id = nextGroupId++
        val group = WorkerGroup(id = id, name = name, yearId = yearId)
        groupsState.value = groupsState.value + group
        return id
    }

    override suspend fun deleteGroup(group: WorkerGroup) {
        groupsState.value = groupsState.value.filterNot { it.id == group.id }
        crossRefsState.value = crossRefsState.value.filterNot { it.groupId == group.id }
    }

    override suspend fun addWorkerToGroup(workerId: Long, groupId: Long) {
        val exists = crossRefsState.value.any { it.workerId == workerId && it.groupId == groupId }
        if (!exists) {
            crossRefsState.value = crossRefsState.value + WorkerGroupCrossRef(workerId, groupId)
        }
    }

    override suspend fun removeWorkerFromGroup(workerId: Long, groupId: Long) {
        crossRefsState.value = crossRefsState.value.filterNot { it.workerId == workerId && it.groupId == groupId }
    }
}
