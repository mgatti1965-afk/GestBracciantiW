package com.aistudio.gestbracciantiw.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aistudio.gestbracciantiw.data.entity.WorkLog
import com.aistudio.gestbracciantiw.ui.components.SmallStatChip
import com.aistudio.gestbracciantiw.ui.utils.TimeUtils
import com.aistudio.gestbracciantiw.ui.utils.formatHours
import com.aistudio.gestbracciantiw.ui.viewmodel.WorkLogViewModel
import com.aistudio.gestbracciantiw.ui.viewmodel.WorkerGroupViewModel
import com.aistudio.gestbracciantiw.ui.viewmodel.WorkerViewModel

@Composable
fun WorkDayDetailScreen(
    date: Long,
    yearId: Int,
    workLogViewModel: WorkLogViewModel,
    workerViewModel: WorkerViewModel,
    groupViewModel: WorkerGroupViewModel,
    onBack: () -> Unit,
    onShowHelp: (String) -> Unit
) {
    val allLogs by workLogViewModel.allLogs.collectAsState()
    val logsForDay = remember(allLogs, date) { allLogs.filter { it.date == date } }
    val workers by workerViewModel.workersForCurrentYear.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        Surface(
            color = MaterialTheme.colorScheme.secondaryContainer,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Indietro")
                }
                Text(
                    text = TimeUtils.format(date, TimeUtils.FULL_DATE_FORMAT).replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(16.dp).weight(1f)
                )
            }
        }

        Box(modifier = Modifier.weight(1f)) {
            if (logsForDay.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Nessun bracciante presente per questa giornata", style = MaterialTheme.typography.titleMedium)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 100.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(logsForDay, key = { _, log: WorkLog -> log.id }) { index, log: WorkLog ->
                        val worker = workers.find { it.id == log.workerId }
                        val cardBg = if (index % 2 == 0) MaterialTheme.colorScheme.surface
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { },
                            colors = CardDefaults.cardColors(containerColor = cardBg),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    modifier = Modifier.size(48.dp),
                                    shape = MaterialTheme.shapes.small,
                                    color = MaterialTheme.colorScheme.primaryContainer
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = worker?.surname?.take(1)?.uppercase() ?: "?",
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${worker?.surname ?: ""} ${worker?.name ?: ""}".trim(),
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Ore Totali: ${formatHours(log.totalHours)}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    if (log.ordinaryHours > 0 || log.extraHours > 0 || log.holidayHours > 0) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            if (log.ordinaryHours > 0) {
                                                SmallStatChip(label = "Ord", hours = log.ordinaryHours, color = MaterialTheme.colorScheme.primary)
                                            }
                                            if (log.extraHours > 0) {
                                                SmallStatChip(label = "STR", hours = log.extraHours, color = MaterialTheme.colorScheme.secondary)
                                            }
                                            if (log.holidayHours > 0) {
                                                SmallStatChip(label = "fest", hours = log.holidayHours, color = MaterialTheme.colorScheme.tertiary)
                                            }
                                        }
                                    }
                                }
                                IconButton(onClick = { workLogViewModel.deleteLog(log) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Elimina", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.95f))
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ExtendedFloatingActionButton(
                    onClick = { },
                    modifier = Modifier.weight(1f),
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    icon = { Icon(Icons.Default.GroupAdd, contentDescription = null) },
                    text = { Text("Gruppo", fontWeight = FontWeight.Bold) }
                )
                ExtendedFloatingActionButton(
                    onClick = { },
                    modifier = Modifier.weight(1f),
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    icon = { Icon(Icons.Default.PersonAdd, contentDescription = null) },
                    text = { Text("Bracciante", fontWeight = FontWeight.Bold) }
                )
            }
        }
    }
}
