package com.aistudio.gestbracciantiw.data.model

import com.aistudio.gestbracciantiw.data.entity.Worker

data class WorkerWithRate(
    val worker: Worker,
    val hourlyRate: Double,
    val extraHourlyRate: Double,
    val holidayHourlyRate: Double
)
