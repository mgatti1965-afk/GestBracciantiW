package com.aistudio.gestbracciantiw.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.aistudio.gestbracciantiw.data.entity.HarvestYear
import com.aistudio.gestbracciantiw.ui.viewmodel.HarvestViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YearSelectionScreen(
    viewModel: HarvestViewModel,
    onYearSelected: (Int) -> Unit
) {
    val years by viewModel.allYears.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var yearToDelete by remember { mutableStateOf<HarvestYear?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    if (showAddDialog) {
        val lastYear = years.maxByOrNull { it.id }?.id
        val suggestedYear = lastYear?.plus(1) ?: 2026

        AddYearDialog(
            suggestedYear = suggestedYear,
            onDismiss = { showAddDialog = false },
            onConfirm = { year, notes, migrateWorkers, migrateGroups ->
                viewModel.createYear(
                    year = year,
                    notes = notes,
                    migrateFrom = lastYear,
                    migrateWorkers = migrateWorkers,
                    migrateGroups = migrateGroups,
                    onSuccess = {
                        showAddDialog = false
                    },
                    onError = { error ->
                        errorMessage = error
                    }
                )
            },
            hasPreviousYear = years.isNotEmpty()
        )
    }

    if (errorMessage != null) {
        AlertDialog(
            onDismissRequest = { errorMessage = null },
            title = { Text("Attenzione", fontWeight = FontWeight.Bold) },
            text = { Text(errorMessage!!) },
            confirmButton = {
                Button(
                    onClick = { errorMessage = null },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("OK", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }

    if (yearToDelete != null) {
        AlertDialog(
            onDismissRequest = { yearToDelete = null },
            title = { Text("Elimina Annata", fontWeight = FontWeight.Bold) },
            text = { Text("Sei sicuro di voler eliminare l'annata ${yearToDelete!!.id}? Tutti i dati relativi verranno persi permanentemente.") },
            confirmButton = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TextButton(
                        onClick = { yearToDelete = null },
                        modifier = Modifier.weight(1f).height(48.dp)
                    ) {
                        Text("Annulla")
                    }
                    Button(
                        onClick = {
                            viewModel.deleteYear(yearToDelete!!.id)
                            yearToDelete = null
                        },
                        modifier = Modifier.weight(1f).height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Elimina", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = null,
            shape = RoundedCornerShape(20.dp)
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Seleziona Annata") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Aggiungi Annata")
            }
        }
    ) { padding ->
        if (years.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Nessuna annata presente. Creane una!")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(years) { year ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.selectYear(year.id)
                                onYearSelected(year.id)
                            },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = if (year.notes.isNotEmpty()) "${year.id} - ${year.notes}" else "${year.id}",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1f)
                                )
                                if (year.isCurrent) {
                                    SuggestionChip(
                                        onClick = {},
                                        label = { Text("Corrente") },
                                        modifier = Modifier.padding(horizontal = 8.dp)
                                    )
                                }
                                IconButton(onClick = { yearToDelete = year }) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Elimina Annata",
                                        tint = MaterialTheme.colorScheme.error
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

@Composable
fun AddYearDialog(
    suggestedYear: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int, String, Boolean, Boolean) -> Unit,
    hasPreviousYear: Boolean
) {
    var yearText by remember(suggestedYear) { mutableStateOf(suggestedYear.toString()) }
    var notesText by remember { mutableStateOf("") }
    var migrateWorkers by remember { mutableStateOf(false) }
    var migrateGroups by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nuova Annata", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = yearText,
                    onValueChange = { if (it.all { char -> char.isDigit() }) yearText = it },
                    label = { Text("Anno (es. 2026)", style = MaterialTheme.typography.labelLarge) },
                    textStyle = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = notesText,
                    onValueChange = { notesText = it },
                    label = { Text("Note (es. Vendemmia)", style = MaterialTheme.typography.labelLarge) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                if (hasPreviousYear) {
                    Text(
                        text = "Importa dati dall'ultima annata:",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { migrateWorkers = !migrateWorkers }
                            .padding(vertical = 8.dp)
                    ) {
                        Text("Braccianti", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                        Switch(
                            checked = migrateWorkers,
                            onCheckedChange = { migrateWorkers = it }
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { migrateGroups = !migrateGroups }
                            .padding(vertical = 8.dp)
                    ) {
                        Text("Gruppi", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                        Switch(
                            checked = migrateGroups,
                            onCheckedChange = { migrateGroups = it }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f).height(48.dp)
                ) {
                    Text("Annulla")
                }
                Button(
                    onClick = {
                        yearText.toIntOrNull()?.let {
                            onConfirm(it, notesText, migrateWorkers && hasPreviousYear, migrateGroups && hasPreviousYear)
                        }
                    },
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Crea", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = null,
        shape = RoundedCornerShape(20.dp)
    )
}
