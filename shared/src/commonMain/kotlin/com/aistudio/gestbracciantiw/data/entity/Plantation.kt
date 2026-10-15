package com.aistudio.gestbracciantiw.data.entity

data class Plantation(
    val id: Long = 0,
    val name: String,
    val description: String = "",
    val isArchived: Boolean = false
)
