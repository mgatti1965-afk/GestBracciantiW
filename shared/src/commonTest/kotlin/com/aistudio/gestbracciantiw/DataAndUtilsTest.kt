package com.aistudio.gestbracciantiw

import com.aistudio.gestbracciantiw.data.entity.Worker
import com.aistudio.gestbracciantiw.ui.utils.capitalizeWords
import com.aistudio.gestbracciantiw.ui.utils.formatDecimalHours
import com.aistudio.gestbracciantiw.ui.utils.formatHours
import com.aistudio.gestbracciantiw.ui.utils.parseTimeToDouble
import kotlin.test.Test
import kotlin.test.assertEquals

class DataAndUtilsTest {

    @Test
    fun testWorkerCreation() {
        val worker = Worker(id = 1, name = "Mario", surname = "Rossi")
        assertEquals("Mario", worker.name)
        assertEquals("Rossi", worker.surname)
        assertEquals(false, worker.isArchived)
    }

    @Test
    fun testCapitalizeWords() {
        assertEquals("Mario Rossi", "mario rossi".capitalizeWords())
        assertEquals("Mario Rossi", "MARIO ROSSI".capitalizeWords())
        assertEquals("De Luca", "de luca".capitalizeWords())
    }

    @Test
    fun testParseTimeToDouble() {
        assertEquals(8.5, parseTimeToDouble("8:30"))
        assertEquals(7.25, parseTimeToDouble("7:15"))
        assertEquals(6.75, parseTimeToDouble("6.75"))
        assertEquals(6.75, parseTimeToDouble("6,75"))
    }

    @Test
    fun testFormatHours() {
        assertEquals("8h 30'", formatHours(8.5))
        assertEquals("7h 15'", formatHours(7.25))
    }

    @Test
    fun testFormatDecimalHours() {
        assertEquals("8.50", formatDecimalHours(8.5))
    }
}
