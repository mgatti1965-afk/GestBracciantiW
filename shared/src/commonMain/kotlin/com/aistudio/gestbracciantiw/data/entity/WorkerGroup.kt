package com.aistudio.gestbracciantiw.data.entity

data class WorkerGroup(
    val id: Long = 0,
    val name: String,
    val yearId: Int
)

data class WorkerGroupCrossRef(
    val workerId: Long,
    val groupId: Long
)
