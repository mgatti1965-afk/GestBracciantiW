package com.aistudio.gestbracciantiw.data.entity

data class WorkLog(
    val id: Long = 0,
    val workerId: Long,
    val harvestYearId: Int,
    val date: Long, // Timestamp a mezzanotte
    val morningStart: String? = null,
    val morningEnd: String? = null,
    val afternoonStart: String? = null,
    val afternoonEnd: String? = null,
    val totalHours: Double = 0.0,
    val ordinaryHours: Double = 0.0,
    val extraHours: Double = 0.0,
    val holidayHours: Double = 0.0,
    val hourlyRate: Double = 0.0,
    val extraHourlyRate: Double = 0.0,
    val holidayHourlyRate: Double = 0.0,
    val isManualHoliday: Boolean = false,
    val totalAmount: Double = 0.0,
    val ordinaryAmount: Double = 0.0,
    val extraAmount: Double = 0.0,
    val holidayAmount: Double = 0.0
)
