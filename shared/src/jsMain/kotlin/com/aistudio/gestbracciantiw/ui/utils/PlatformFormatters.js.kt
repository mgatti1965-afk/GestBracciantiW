package com.aistudio.gestbracciantiw.ui.utils

import kotlin.js.Date

actual fun platformFormatCurrency(amount: Double): String {
    // Formattazione semplice valuta per JS: € X,XX
    val formatted = platformFormatDecimal(amount, 2, true)
    return "$formatted €"
}

actual fun platformFormatDecimal(value: Double, decimals: Int, grouping: Boolean): String {
    // Esempio: 1234.56 -> 1.234,56
    val isNeg = value < 0
    val absVal = if (isNeg) -value else value
    val fixed = js("absVal.toFixed(decimals)") as String
    val parts = fixed.split(".")
    val integerPart = parts[0]
    val decimalPart = if (parts.size > 1) parts[1] else ""

    val withGrouping = if (grouping) {
        val reversed = integerPart.reversed()
        val chunked = reversed.chunked(3).joinToString(".")
        chunked.reversed()
    } else {
        integerPart
    }

    val res = if (decimals > 0) "$withGrouping,$decimalPart" else withGrouping
    return if (isNeg) "-$res" else res
}

actual fun platformFormatDate(timestamp: Long, pattern: String): String {
    val date = Date(timestamp.toDouble())
    val day = date.getDate().toString().padStart(2, '0')
    val month = (date.getMonth() + 1).toString().padStart(2, '0')
    val year = date.getFullYear().toString()
    val hours = date.getHours().toString().padStart(2, '0')
    val minutes = date.getMinutes().toString().padStart(2, '0')

    val italianMonths = arrayOf("Gennaio", "Febbraio", "Marzo", "Aprile", "Maggio", "Giugno", "Luglio", "Agosto", "Settembre", "Ottobre", "Novembre", "Dicembre")
    val italianDays = arrayOf("Domenica", "Lunedì", "Martedì", "Mercoledì", "Giovedì", "Venerdì", "Sabato")
    val italianDaysShort = arrayOf("Dom", "Lun", "Mar", "Mer", "Gio", "Ven", "Sab")

    val dayOfWeek = date.getDay()
    val monthIdx = date.getMonth()

    return when (pattern) {
        "dd/MM/yyyy" -> "$day/$month/$year"
        "dd/MM/yyyy HH:mm" -> "$day/$month/$year $hours:$minutes"
        "yyyyMMdd_HHmm" -> "${year}${month}${day}_${hours}${minutes}"
        "MMMM yyyy" -> "${italianMonths.getOrElse(monthIdx) { "" }} $year"
        "dd/MM" -> "$day/$month"
        "EEEE d MMMM yyyy" -> "${italianDays.getOrElse(dayOfWeek) { "" }} ${date.getDate()} ${italianMonths.getOrElse(monthIdx) { "" }} $year"
        "HH:mm" -> "$hours:$minutes"
        "EEE dd/MM/yyyy" -> "${italianDaysShort.getOrElse(dayOfWeek) { "" }} $day/$month/$year"
        "EEEE" -> italianDays.getOrElse(dayOfWeek) { "" }
        "MMM" -> italianMonths.getOrElse(monthIdx) { "" }.take(3)
        "d MMM" -> "${date.getDate()} ${italianMonths.getOrElse(monthIdx) { "" }.take(3)}"
        "EEE d MMMM yyyy" -> "${italianDaysShort.getOrElse(dayOfWeek) { "" }} ${date.getDate()} ${italianMonths.getOrElse(monthIdx) { "" }} $year"
        "yyyy" -> year
        else -> "$day/$month/$year"
    }
}

actual fun platformGetDayOfWeek(timestamp: Long): Int {
    val date = Date(timestamp.toDouble())
    // Date.getDay(): 0 = Sunday, 1 = Monday, ..., 6 = Saturday
    // Calendar.DAY_OF_WEEK: 1 = Sunday, 2 = Monday, ..., 7 = Saturday
    return date.getDay() + 1
}
