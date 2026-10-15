package com.aistudio.gestbracciantiw.ui.utils

/**
 * Converte una stringa in minuscolo con la prima lettera di ogni parola in maiuscolo.
 * Esempio: "MARIO ROSSI" -> "Mario Rossi", "de luca" -> "De Luca"
 */
fun String.capitalizeWords(): String {
    if (this.isBlank()) return this
    return this.lowercase()
        .split(" ")
        .filter { it.isNotEmpty() }
        .joinToString(" ") { word ->
            word.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        }
}
