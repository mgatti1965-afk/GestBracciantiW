package com.aistudio.gestbracciantiw.data.entity

data class Worker(
    val id: Long = 0,
    val name: String,
    val surname: String,
    val phoneNumber: String = "",
    val isArchived: Boolean = false
)
