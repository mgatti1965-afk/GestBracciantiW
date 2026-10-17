package com.aistudio.gestbracciantiw.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aistudio.gestbracciantiw.ui.components.SmallStatChip
import com.aistudio.gestbracciantiw.ui.utils.TimeUtils
import com.aistudio.gestbracciantiw.ui.utils.formatCurrency
import com.aistudio.gestbracciantiw.ui.utils.formatHours
import com.aistudio.gestbracciantiw.ui.viewmodel.WorkLogViewModel
import com.aistudio.gestbracciantiw.ui.viewmodel.WorkerGroupViewModel

enum class GroupingType { BY_WORKER, BY_GROUP }
enum class ViewMode { DETAIL, TOTALS }

@Composable
fun FinancialSummaryScreen(
    viewModel: WorkLogViewModel,
    groupViewModel: WorkerGroupViewModel
) {
    val stats by viewModel.yearlyStats.collectAsState()
    val filteredLogs by viewModel.filteredLogs.collectAsState()
    val referenceDate by viewModel.currentReferenceDate.collectAsState()
    var selectedFilter by remember { mutableIntStateOf(0) }
    val filters = listOf("Anno", "Mese", "Settimana", "Giorno")

    var groupingType by remember { mutableStateOf(GroupingType.BY_WORKER) }
    var viewModeForWorker by remember { mutableStateOf(ViewMode.DETAIL) }

    Scaffold { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            ScrollableTabRow(
                selectedTabIndex = selectedFilter,
                edgePadding = 16.dp,
                divider = {}
            ) {
                filters.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedFilter == index,
                        onClick = { selectedFilter = index },
                        text = { Text(title) }
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val groupColor = Color(0xFF1E88E5)
                Button(
                    onClick = { groupingType = GroupingType.BY_WORKER },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (groupingType == GroupingType.BY_WORKER) groupColor else groupColor.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier.height(36.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Braccianti")
                }

                Button(
                    onClick = { groupingType = GroupingType.BY_GROUP },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (groupingType == GroupingType.BY_GROUP) groupColor else groupColor.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier.height(36.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Gruppi")
                }

                Spacer(modifier = Modifier.weight(1f))

                val modeColor = Color(0xFFF4511E)
                Button(
                    onClick = { viewModeForWorker = ViewMode.DETAIL },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (viewModeForWorker == ViewMode.DETAIL) modeColor else modeColor.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier.height(36.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Dettaglio")
                }

                Button(
                    onClick = { viewModeForWorker = ViewMode.TOTALS },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (viewModeForWorker == ViewMode.TOTALS) modeColor else modeColor.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier.height(36.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Totali")
                }
            }

            Box(modifier = Modifier.weight(1f)) {
                if (filteredLogs.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Nessun dato presente per la selezione corrente.")
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        itemsIndexed(filteredLogs, key = { _, log -> log.id }) { index, log ->
                            val worker = stats.find { it.workerId == log.workerId }
                            val cardBg = if (index % 2 == 0) MaterialTheme.colorScheme.surface
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)

                            Card(
                                modifier = Modifier.fillMaxWidth().clickable { },
                                colors = CardDefaults.cardColors(containerColor = cardBg),
                                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            color = MaterialTheme.colorScheme.primaryContainer,
                                            shape = MaterialTheme.shapes.small
                                        ) {
                                            Text(
                                                text = TimeUtils.format(log.date, TimeUtils.DAY_MONTH_FORMAT),
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "${worker?.surname ?: ""} ${worker?.name ?: "Bracciante ${log.workerId}"}",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(
                                                text = formatHours(log.totalHours),
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                            Text(
                                                text = formatCurrency(log.totalAmount),
                                                style = MaterialTheme.typography.bodyLarge,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
