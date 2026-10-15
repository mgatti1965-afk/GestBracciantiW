package com.aistudio.gestbracciantiw.ui.utils

object TimeUtils {
    const val DATE_FORMAT = "dd/MM/yyyy"
    const val DATE_TIME_FORMAT = "dd/MM/yyyy HH:mm"
    const val FILE_TIMESTAMP_FORMAT = "yyyyMMdd_HHmm"
    const val MONTH_YEAR_FORMAT = "MMMM yyyy"
    const val DAY_MONTH_FORMAT = "dd/MM"
    const val FULL_DATE_FORMAT = "EEEE d MMMM yyyy"
    const val TIME_FORMAT = "HH:mm"
    const val SHORT_DATE_DAY_FORMAT = "EEE dd/MM/yyyy"
    const val DAY_NAME_FORMAT = "EEEE"
    const val MONTH_SHORT_FORMAT = "MMM"
    const val DAY_MONTH_SHORT_FORMAT = "d MMM"
    const val DAY_SHORT_FULL_DATE_FORMAT = "EEE d MMMM yyyy"
    const val YEAR_FORMAT = "yyyy"

    fun isFestive(date: Long, isManual: Boolean, festiveType: Int, globalFestiveDates: Set<Long> = emptySet()): Boolean {
        // dayOfWeek: 1 = Domenica, 7 = Sabato
        val dayOfWeek = platformGetDayOfWeek(date)
        val naturalFestive = when (festiveType) {
            1 -> dayOfWeek == 7 // Sabato
            2 -> dayOfWeek == 1 // Domenica
            3 -> dayOfWeek == 7 || dayOfWeek == 1 // Entrambi
            else -> false
        }
        
        // Se è manuale (singolo log) o presente nelle date globali (tutto il giorno), invertiamo il valore naturale
        val isOverridden = isManual || globalFestiveDates.contains(date)
        
        return if (isOverridden) !naturalFestive else naturalFestive
    }

    fun format(date: Long, pattern: String): String {
        return platformFormatDate(date, pattern)
    }
    
    fun formatMonth(date: Long): String {
        return platformFormatDate(date, MONTH_YEAR_FORMAT).replaceFirstChar { it.uppercase() }
    }
}

fun formatDecimalHours(hours: Double): String {
    return platformFormatDecimal(hours, decimals = 2, grouping = false).replace(',', '.')
}

fun parseTimeToDouble(timeStr: String): Double {
    val cleanStr = timeStr.trim()
    return try {
        if (cleanStr.contains(":")) {
            val parts = cleanStr.split(":")
            val h = parts.getOrNull(0)?.toIntOrNull() ?: 0
            val m = parts.getOrNull(1)?.toIntOrNull() ?: 0
            h.toDouble() + (m.toDouble() / 60.0)
        } else {
            // Support both comma and dot for decimal input
            cleanStr.replace(",", ".").toDoubleOrNull() ?: 0.0
        }
    } catch (e: Exception) {
        0.0
    }
}
