package com.aistudio.gestbracciantiw.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.aistudio.gestbracciantiw.ui.viewmodel.WorkLogViewModel
import com.aistudio.gestbracciantiw.ui.viewmodel.WorkerViewModel

@Composable
fun OthersScreen(
    workerViewModel: WorkerViewModel,
    workLogViewModel: WorkLogViewModel,
    yearId: Int
) {
    val settings by workLogViewModel.settings.collectAsState()

    var ownerName by remember(settings) { mutableStateOf(settings.ownerName) }
    var ownerSurname by remember(settings) { mutableStateOf(settings.ownerSurname) }
    var extraHoursThreshold by remember(settings) { mutableStateOf(settings.extraHoursThreshold.toInt().toString()) }
    var festiveDaysType by remember(settings) { mutableIntStateOf(settings.festiveDaysType) }

    var selectedTab by remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Impostazioni e Backup", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

        TabRow(selectedTabIndex = selectedTab) {
            Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Dati e Preferenze") })
            Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Backup CSV") })
        }

        when (selectedTab) {
            0 -> {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Dati Titolare / Azienda", style = MaterialTheme.typography.titleMedium)
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(
                                        value = ownerSurname,
                                        onValueChange = {
                                            ownerSurname = it
                                        },
                                        label = { Text("Cognome", style = MaterialTheme.typography.labelLarge) },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true
                                    )
                                    OutlinedTextField(
                                        value = ownerName,
                                        onValueChange = {
                                            ownerName = it
                                        },
                                        label = { Text("Nome", style = MaterialTheme.typography.labelLarge) },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Calcolo Ore e Festività", style = MaterialTheme.typography.titleMedium)
                                Spacer(modifier = Modifier.height(16.dp))

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Soglia Ore Straordinarie",
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                        Text(
                                            text = "Soglia oltre la quale scatta la tariffa straordinaria",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.outline
                                        )
                                    }
                                    OutlinedTextField(
                                        value = extraHoursThreshold,
                                        onValueChange = { input ->
                                            if (input.isEmpty() || (input.all { it.isDigit() } && input.length <= 2)) {
                                                extraHoursThreshold = input
                                            }
                                        },
                                        modifier = Modifier.width(80.dp),
                                        textStyle = LocalTextStyle.current.copy(
                                            textAlign = TextAlign.Center,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true
                                    )
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                Text(
                                    text = "Giorni Festivi Settimanali",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                val isSaturdayFestive = (festiveDaysType and 1) != 0
                                val isSundayFestive = (festiveDaysType and 2) != 0

                                Column {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .heightIn(min = 48.dp)
                                            .clickable {
                                                festiveDaysType = if (isSaturdayFestive) festiveDaysType and 2 else festiveDaysType or 1
                                            }
                                    ) {
                                        Checkbox(checked = isSaturdayFestive, onCheckedChange = null)
                                        Text("Sabato Festivo", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(start = 12.dp).weight(1f))
                                    }
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .heightIn(min = 48.dp)
                                            .clickable {
                                                festiveDaysType = if (isSundayFestive) festiveDaysType and 1 else festiveDaysType or 2
                                            }
                                    ) {
                                        Checkbox(checked = isSundayFestive, onCheckedChange = null)
                                        Text("Domenica Festiva", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(start = 12.dp).weight(1f))
                                    }
                                }
                            }
                        }
                    }
                }
            }
            1 -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Esportazione e Importazione Backup CSV disponibile nelle opzioni di storage.")
                }
            }
        }
    }
}
