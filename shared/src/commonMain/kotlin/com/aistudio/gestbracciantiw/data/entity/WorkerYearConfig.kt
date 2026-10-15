package com.aistudio.gestbracciantiw.data.entity

data class WorkerYearConfig(
    val workerId: Long,
    val harvestYearId: Int,
    val hourlyRate: Double,
    val extraHourlyRate: Double = 0.0,
    val holidayHourlyRate: Double = 0.0
)
