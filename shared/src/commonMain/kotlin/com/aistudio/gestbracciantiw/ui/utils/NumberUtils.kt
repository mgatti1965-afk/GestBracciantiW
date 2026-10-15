package com.aistudio.gestbracciantiw.ui.utils

import kotlin.math.roundToInt

/**
 * Formatta un importo come valuta italiana (€) con separatore migliaia (punto) e decimali (virgola).
 */
fun formatCurrency(amount: Double): String {
    return platformFormatCurrency(amount)
}

/**
 * Formatta un numero con stile italiano (punto per le migliaia, virgola per i decimali).
 */
fun formatDecimal(value: Double, decimals: Int = 2): String {
    return platformFormatDecimal(value, decimals, grouping = true)
}

/**
 * Formatta un numero per campi di input (senza separatore migliaia).
 */
fun formatInputDecimal(value: Double, decimals: Int = 2): String {
    return platformFormatDecimal(value, decimals, grouping = false)
}

/**
 * Formatta le ore in formato leggibile (es: 8,75 -> 8h 45m).
 */
fun formatHours(hours: Double): String {
    val h = hours.toInt()
    val m = ((hours - h) * 60).roundToInt()
    return "${h}h ${m}'"
}

/**
 * Formatta le ore in formato decimale italiano (es: 1.250,50).
 */
fun formatHoursDecimal(hours: Double): String {
    return formatDecimal(hours, 2)
}
