package com.aistudio.gestbracciantiw.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aistudio.gestbracciantiw.data.entity.Worker
import com.aistudio.gestbracciantiw.data.entity.WorkerGroup
import com.aistudio.gestbracciantiw.data.repository.WorkerGroupRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class WorkerGroupViewModel(private val repository: WorkerGroupRepository) : ViewModel() {

    private val _selectedYearId = MutableStateFlow<Int?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val groupsForYear: StateFlow<List<WorkerGroup>> = _selectedYearId
        .filterNotNull()
        .flatMapLatest { yearId ->
            repository.getGroupsForYear(yearId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setSelectedYear(yearId: Int) {
        _selectedYearId.value = yearId
    }

    fun createGroup(name: String) {
        val yearId = _selectedYearId.value ?: return
        viewModelScope.launch {
            repository.createGroup(name, yearId)
        }
    }

    fun deleteGroup(group: WorkerGroup) {
        viewModelScope.launch {
            repository.deleteGroup(group)
        }
    }

    fun addWorkerToGroup(workerId: Long, groupId: Long) {
        viewModelScope.launch {
            repository.addWorkerToGroup(workerId, groupId)
        }
    }

    fun removeWorkerFromGroup(workerId: Long, groupId: Long) {
        viewModelScope.launch {
            repository.removeWorkerFromGroup(workerId, groupId)
        }
    }

    fun getWorkersInGroup(groupId: Long): Flow<List<Worker>> {
        return repository.getWorkersInGroup(groupId)
    }

    fun copyGroupsFromPreviousYear(currentYearId: Int, onResult: (Int) -> Unit) {
        viewModelScope.launch {
            val previousYearId = currentYearId - 1
            val previousYearGroups = repository.getGroupsForYear(previousYearId).first()
            val currentYearGroups = repository.getGroupsForYear(currentYearId).first()
            val currentGroupNames = currentYearGroups.map { it.name.lowercase() }.toSet()

            var copiedCount = 0

            previousYearGroups.forEach { oldGroup ->
                if (oldGroup.name.lowercase() !in currentGroupNames) {
                    val newGroupId = repository.createGroupAndReturnId(oldGroup.name, currentYearId)
                    val members = repository.getWorkersInGroup(oldGroup.id).first()
                    members.forEach { worker ->
                        repository.addWorkerToGroup(worker.id, newGroupId)
                    }
                    copiedCount++
                }
            }

            _selectedYearId.value = null
            _selectedYearId.value = currentYearId

            onResult(copiedCount)
        }
    }
}
