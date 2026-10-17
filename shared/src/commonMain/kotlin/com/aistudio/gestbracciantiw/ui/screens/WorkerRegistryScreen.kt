package com.aistudio.gestbracciantiw.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.aistudio.gestbracciantiw.data.entity.Worker
import com.aistudio.gestbracciantiw.data.entity.WorkerGroup
import com.aistudio.gestbracciantiw.ui.utils.MessageBarManager
import com.aistudio.gestbracciantiw.ui.utils.formatCurrency
import com.aistudio.gestbracciantiw.ui.utils.formatInputDecimal
import com.aistudio.gestbracciantiw.ui.viewmodel.WorkerGroupViewModel
import com.aistudio.gestbracciantiw.ui.viewmodel.WorkerViewModel
import kotlinx.coroutines.launch

@Composable
fun WorkerRegistryScreen(
    workerViewModel: WorkerViewModel,
    groupViewModel: WorkerGroupViewModel,
    yearId: Int
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Braccianti", "Gruppi")

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) }
                )
            }
        }

        when (selectedTab) {
            0 -> WorkerListTab(workerViewModel, yearId)
            1 -> GroupListTab(groupViewModel, workerViewModel, yearId)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WorkerListTab(viewModel: WorkerViewModel, yearId: Int) {
    val scope = rememberCoroutineScope()
    val workersWithRate by viewModel.workersWithRateForCurrentYear.collectAsState()
    val duplicates by viewModel.duplicatesFound.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { message ->
            MessageBarManager.showMessage(message)
        }
    }

    var showDialog by remember { mutableStateOf(false) }
    var selectedWorker by remember { mutableStateOf<Worker?>(null) }
    var currentRates by remember { mutableStateOf(Triple(0.0, 0.0, 0.0)) }
    var searchQuery by remember { mutableStateOf("") }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val filteredWorkers = remember(workersWithRate, searchQuery) {
        if (searchQuery.isBlank()) workersWithRate
        else workersWithRate.filter {
            it.worker.name.contains(searchQuery, ignoreCase = true) ||
                    it.worker.surname.contains(searchQuery, ignoreCase = true) ||
                    it.worker.phoneNumber.contains(searchQuery)
        }
    }

    val sortedWorkers = remember(filteredWorkers) {
        filteredWorkers.sortedWith(compareBy({ it.worker.surname.lowercase() }, { it.worker.name.lowercase() }))
    }

    if (showDialog) {
        AddEditWorkerDialog(
            worker = selectedWorker,
            initialRates = currentRates,
            onDismiss = { showDialog = false },
            onConfirm = { name, surname, phone, rate, extraRate, holidayRate ->
                val isDuplicate = workersWithRate.any {
                    it.worker.surname.equals(surname, ignoreCase = true) &&
                            it.worker.name.equals(name, ignoreCase = true) &&
                            it.worker.id != selectedWorker?.id
                }

                if (isDuplicate) {
                    scope.launch {
                        MessageBarManager.showMessage("Un bracciante con questo nome e cognome è già presente.", isError = true)
                    }
                } else {
                    if (selectedWorker == null) {
                        viewModel.addWorkerToYear(name, surname, phone, rate, extraRate, holidayRate, yearId)
                    } else {
                        viewModel.updateWorkerInfo(selectedWorker!!.id, name, surname, phone, yearId, rate, extraRate, holidayRate)
                    }
                    showDialog = false
                }
            },
            onDelete = {
                showDeleteConfirm = true
                showDialog = false
            }
        )
    }

    if (showDeleteConfirm && selectedWorker != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Conferma eliminazione", fontWeight = FontWeight.Bold) },
            text = { Text("Sei sicuro di voler eliminare ${selectedWorker!!.surname} ${selectedWorker!!.name}?") },
            confirmButton = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TextButton(
                        onClick = { showDeleteConfirm = false },
                        modifier = Modifier.weight(1f).height(48.dp)
                    ) {
                        Text("Annulla")
                    }
                    Button(
                        onClick = {
                            viewModel.deleteWorker(selectedWorker!!)
                            showDeleteConfirm = false
                            selectedWorker = null
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

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            if (duplicates.isNotEmpty()) {
                duplicates.forEach { group ->
                    DuplicatesAlert(
                        duplicateGroup = group,
                        onMerge = {
                            viewModel.mergeDuplicateGroup(group)
                        }
                    )
                }
            }

            if (workersWithRate.isNotEmpty()) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    placeholder = { Text("Cerca bracciante...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Cancella ricerca")
                            }
                        }
                    },
                    singleLine = true
                )
            }

            if (workersWithRate.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Nessun bracciante presente per questa annata.")
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                viewModel.copyWorkersFromPreviousYear(yearId) { count ->
                                    scope.launch {
                                        MessageBarManager.showMessage("Copiati $count braccianti dall'anno precedente.")
                                    }
                                }
                            }
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Copia da Anno Precedente")
                        }
                    }
                }
            } else if (filteredWorkers.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Nessun risultato per '$searchQuery'")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(sortedWorkers, key = { _, item -> item.worker.id }) { index, item ->
                        val worker = item.worker
                        val cardBg = if (index % 2 == 0) MaterialTheme.colorScheme.surface
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)

                        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedWorker = worker
                                        currentRates = Triple(item.hourlyRate, item.extraHourlyRate, item.holidayHourlyRate)
                                        showDialog = true
                                    },
                                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                                colors = CardDefaults.cardColors(containerColor = cardBg),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                                shape = MaterialTheme.shapes.medium
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "${worker.surname} ${worker.name}".trim(),
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.Bold
                                        )
                                        FlowRow(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            val rateStyle = MaterialTheme.typography.labelSmall.copy(
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(text = "Ord: ${formatCurrency(item.hourlyRate)}", style = rateStyle)
                                            Text(text = "STR: ${formatCurrency(item.extraHourlyRate)}", style = rateStyle)
                                            Text(text = "fest: ${formatCurrency(item.holidayHourlyRate)}", style = rateStyle)
                                        }
                                    }
                                    IconButton(onClick = {
                                        selectedWorker = worker
                                        currentRates = Triple(item.hourlyRate, item.extraHourlyRate, item.holidayHourlyRate)
                                        showDialog = true
                                    }) {
                                        Icon(
                                            Icons.Default.Edit,
                                            contentDescription = "Modifica",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        FloatingActionButton(
            onClick = { selectedWorker = null; currentRates = Triple(0.0, 0.0, 0.0); showDialog = true },
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)
        ) { Icon(Icons.Default.Add, contentDescription = "Aggiungi Bracciante") }
    }
}

@Composable
fun GroupListTab(groupViewModel: WorkerGroupViewModel, workerViewModel: WorkerViewModel, yearId: Int) {
    val scope = rememberCoroutineScope()
    val groups by groupViewModel.groupsForYear.collectAsState()
    val allWorkers by workerViewModel.workersForCurrentYear.collectAsState()
    var showAddGroupDialog by remember { mutableStateOf(false) }
    var groupToEditMembers by remember { mutableStateOf<WorkerGroup?>(null) }

    val sortedGroups = remember(groups) {
        groups.sortedBy { it.name.lowercase() }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (groups.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Nessun gruppo creato.")
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            groupViewModel.copyGroupsFromPreviousYear(yearId) { count ->
                                scope.launch {
                                    MessageBarManager.showMessage("Copiati $count gruppi dall'anno precedente.")
                                }
                            }
                        }
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Copia da Anno Precedente")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(sortedGroups, key = { _, group -> group.id }) { index, group ->
                    val members by groupViewModel.getWorkersInGroup(group.id).collectAsState(initial = emptyList())
                    val cardBg = if (index % 2 == 0) MaterialTheme.colorScheme.surface
                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)

                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { groupToEditMembers = group },
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                            colors = CardDefaults.cardColors(containerColor = cardBg),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = group.name,
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "${members.size} componenti",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    IconButton(onClick = { groupViewModel.deleteGroup(group) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Elimina", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                                    }
                                }
                                if (members.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Membri:",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                    members.sortedWith(compareBy({ it.surname }, { it.name })).chunked(2).forEach { rowMembers ->
                                        Row(modifier = Modifier.fillMaxWidth()) {
                                            rowMembers.forEach { member ->
                                                Text(
                                                    text = "${member.surname} ${member.name}".trim(),
                                                    style = MaterialTheme.typography.bodySmall,
                                                    fontWeight = FontWeight.Medium,
                                                    modifier = Modifier.weight(1f),
                                                    maxLines = 1
                                                )
                                            }
                                            if (rowMembers.size == 1) Spacer(Modifier.weight(1f))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        FloatingActionButton(
            onClick = { showAddGroupDialog = true },
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)
        ) { Icon(Icons.Default.Add, contentDescription = "Crea Gruppo") }

        if (groups.isNotEmpty()) {
            SmallFloatingActionButton(
                onClick = {
                    groupViewModel.copyGroupsFromPreviousYear(yearId) { count ->
                        scope.launch {
                            if (count > 0) {
                                MessageBarManager.showMessage("Copiati $count nuovi gruppi dall'anno precedente.")
                            } else {
                                MessageBarManager.showMessage("Nessun nuovo gruppo da copiare.")
                            }
                        }
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 80.dp, end = 16.dp),
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
            ) {
                Icon(Icons.Default.ContentCopy, contentDescription = "Copia Gruppi")
            }
        }
    }

    if (showAddGroupDialog) {
        var groupName by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddGroupDialog = false },
            title = { Text("Nuovo Gruppo", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = groupName,
                    onValueChange = { groupName = it },
                    label = { Text("Nome Gruppo") },
                    textStyle = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TextButton(
                        onClick = { showAddGroupDialog = false },
                        modifier = Modifier.weight(1f).height(48.dp)
                    ) {
                        Text("Annulla")
                    }
                    Button(
                        onClick = {
                            if (groupName.isNotBlank()) {
                                groupViewModel.createGroup(groupName)
                                showAddGroupDialog = false
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

    if (groupToEditMembers != null) {
        val group = groupToEditMembers!!
        val members by groupViewModel.getWorkersInGroup(group.id).collectAsState(initial = emptyList())
        AlertDialog(
            onDismissRequest = { groupToEditMembers = null },
            title = { Text("Membri: ${group.name}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold) },
            text = {
                LazyColumn(modifier = Modifier.heightIn(max = 500.dp)) {
                    items(allWorkers, key = { it.id }) { worker ->
                        val isMember = members.any { it.id == worker.id }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (isMember) groupViewModel.removeWorkerFromGroup(worker.id, group.id)
                                    else groupViewModel.addWorkerToGroup(worker.id, group.id)
                                }
                                .padding(vertical = 8.dp)
                        ) {
                            Checkbox(
                                checked = isMember,
                                onCheckedChange = null
                            )
                            Spacer(Modifier.width(16.dp))
                            Text(
                                text = "${worker.surname} ${worker.name}".trim(),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { groupToEditMembers = null },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Chiudi", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }
}

@Composable
fun DuplicatesAlert(
    duplicateGroup: List<Worker>,
    onMerge: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Delete, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Rilevati Profili Duplicati",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = "${duplicateGroup.first().surname} ${duplicateGroup.first().name}: Trovati più profili simili. Vuoi unirli in un unico profilo?",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = onMerge,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                ),
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Unisci Ora")
            }
        }
    }
}

@Composable
fun AddEditWorkerDialog(
    worker: Worker?,
    initialRates: Triple<Double, Double, Double>,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, Double, Double, Double) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    var name by remember(worker) { mutableStateOf(worker?.name ?: "") }
    var surname by remember(worker) { mutableStateOf(worker?.surname ?: "") }
    var phoneNumber by remember(worker) { mutableStateOf(worker?.phoneNumber ?: "") }
    var rate by remember(initialRates.first) { mutableStateOf(if (initialRates.first > 0) formatInputDecimal(initialRates.first) else "") }
    var extraRate by remember(initialRates.second) { mutableStateOf(if (initialRates.second > 0) formatInputDecimal(initialRates.second) else "") }
    var holidayRate by remember(initialRates.third) { mutableStateOf(if (initialRates.third > 0) formatInputDecimal(initialRates.third) else "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (worker == null) "Nuovo Bracciante" else "Modifica Bracciante",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                if (worker != null && onDelete != null) {
                    IconButton(onClick = onDelete) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Elimina",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = surname,
                    onValueChange = { surname = it },
                    label = { Text("Cognome *", style = MaterialTheme.typography.labelLarge) },
                    textStyle = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.fillMaxWidth(),
                    isError = surname.isBlank()
                )
                if (surname.isBlank()) {
                    Text("Il cognome è obbligatorio", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                }
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nome", style = MaterialTheme.typography.labelLarge) },
                    textStyle = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it },
                    label = { Text("Telefono", style = MaterialTheme.typography.labelLarge) },
                    textStyle = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                )
                OutlinedTextField(
                    value = rate,
                    onValueChange = { input ->
                        if (input.isEmpty() || input.matches(Regex("""^\d*[.,]?\d{0,2}$"""))) {
                            val oldRate = rate
                            rate = input.replace('.', ',')
                            if (extraRate.isEmpty() || extraRate == oldRate) {
                                extraRate = rate
                            }
                            if (holidayRate.isEmpty() || holidayRate == oldRate) {
                                holidayRate = rate
                            }
                        }
                    },
                    label = { Text("Tariffa Base (€/h)", style = MaterialTheme.typography.labelLarge) },
                    textStyle = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                OutlinedTextField(
                    value = extraRate,
                    onValueChange = { input ->
                        if (input.isEmpty() || input.matches(Regex("""^\d*[.,]?\d{0,2}$"""))) {
                            extraRate = input.replace('.', ',')
                        }
                    },
                    label = { Text("Tariffa Straordinari (€/h)", style = MaterialTheme.typography.labelLarge) },
                    textStyle = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                OutlinedTextField(
                    value = holidayRate,
                    onValueChange = { input ->
                        if (input.isEmpty() || input.matches(Regex("""^\d*[.,]?\d{0,2}$"""))) {
                            holidayRate = input.replace('.', ',')
                        }
                    },
                    label = { Text("Tariffa Festivi (€/h)", style = MaterialTheme.typography.labelLarge) },
                    textStyle = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
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
                        val r = rate.replace(',', '.').toDoubleOrNull() ?: 0.0
                        val er = extraRate.replace(',', '.').toDoubleOrNull() ?: 0.0
                        val hr = holidayRate.replace(',', '.').toDoubleOrNull() ?: 0.0
                        if (surname.isNotBlank()) {
                            onConfirm(name, surname, phoneNumber, r, er, hr)
                        }
                    },
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    enabled = surname.isNotBlank()
                ) {
                    Text(
                        if (worker == null) "Aggiungi" else "Salva",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        dismissButton = null,
        shape = RoundedCornerShape(20.dp)
    )
}
