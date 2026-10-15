package com.aistudio.gestbracciantiw.data.entity

data class HarvestYear(
    val id: Int, // Ad es. 2024, 2025
    val isCurrent: Boolean = false,
    val notes: String = ""
)
