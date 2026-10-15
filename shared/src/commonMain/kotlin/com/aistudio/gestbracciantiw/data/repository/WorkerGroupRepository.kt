package com.aistudio.gestbracciantiw.data.repository

import com.aistudio.gestbracciantiw.data.entity.Worker
import com.aistudio.gestbracciantiw.data.entity.WorkerGroup
import kotlinx.coroutines.flow.Flow

interface WorkerGroupRepository {
    fun getGroupsForYear(yearId: Int): Flow<List<WorkerGroup>>
    fun getWorkersInGroup(groupId: Long): Flow<List<Worker>>
    suspend fun createGroup(name: String, yearId: Int)
    suspend fun createGroupAndReturnId(name: String, yearId: Int): Long
    suspend fun deleteGroup(group: WorkerGroup)
    suspend fun addWorkerToGroup(workerId: Long, groupId: Long)
    suspend fun removeWorkerFromGroup(workerId: Long, groupId: Long)
}
