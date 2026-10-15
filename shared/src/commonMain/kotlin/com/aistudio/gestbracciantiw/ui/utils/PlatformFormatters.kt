package com.aistudio.gestbracciantiw.ui.utils

expect fun platformFormatCurrency(amount: Double): String
expect fun platformFormatDecimal(value: Double, decimals: Int = 2, grouping: Boolean = true): String
expect fun platformFormatDate(timestamp: Long, pattern: String): String
expect fun platformGetDayOfWeek(timestamp: Long): Int // 1 = Sunday, 7 = Saturday (compatibile con java.util.Calendar)
