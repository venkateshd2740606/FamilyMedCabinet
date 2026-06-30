package com.familymedcabinet.presentation.ui.screens.cabinet

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.familymedcabinet.ads.AdManager
import com.familymedcabinet.domain.model.FamilyRelation
import com.familymedcabinet.domain.model.Medicine
import com.familymedcabinet.domain.model.Profile
import com.familymedcabinet.presentation.ui.components.AdBanner
import com.familymedcabinet.presentation.viewmodel.CabinetViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProfileDialog(
    onDismiss: () -> Unit,
    onSave: (String, FamilyRelation, Int?) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var relation by remember { mutableStateOf(FamilyRelation.SELF) }
    var ageText by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Family Member") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                    OutlinedTextField(
                        value = relation.displayName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Relation") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        FamilyRelation.entries.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option.displayName) },
                                onClick = {
                                    relation = option
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = ageText,
                    onValueChange = { ageText = it.filter { c -> c.isDigit() } },
                    label = { Text("Age (optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(name, relation, ageText.toIntOrNull()) },
                enabled = name.isNotBlank()
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CabinetHomeScreen(
    onAddMedicine: (Long) -> Unit,
    onMedicineDetail: (Long) -> Unit,
    adManager: AdManager,
    adsEnabled: Boolean,
    viewModel: CabinetViewModel = hiltViewModel()
) {
    val profiles by viewModel.profiles.collectAsStateWithLifecycle()
    val medicines by viewModel.medicines.collectAsStateWithLifecycle()
    val expiring by viewModel.expiringSoon.collectAsStateWithLifecycle()
    val selectedId by viewModel.selectedProfileId.collectAsStateWithLifecycle()
    var showAddProfile by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Family Med Cabinet") }) },
        floatingActionButton = {
            val profileId = selectedId ?: profiles.firstOrNull()?.id
            if (profileId != null) {
                FloatingActionButton(onClick = { onAddMedicine(profileId) }) {
                    Icon(Icons.Default.Add, contentDescription = "Add medicine")
                }
            }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            AdBanner(adManager = adManager, adsEnabled = adsEnabled)
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    ProfileChipRow(
                        profiles = profiles,
                        selectedId = selectedId,
                        onSelect = viewModel::selectProfile,
                        onAddProfile = { showAddProfile = true }
                    )
                }
                if (expiring.isNotEmpty()) {
                    item {
                        ExpiringSoonSection(
                            medicines = expiring,
                            formatExpiry = viewModel::formatExpiry,
                            daysUntil = viewModel::daysUntilExpiry,
                            onClick = onMedicineDetail
                        )
                    }
                }
                item {
                    Text("Medicines", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                if (profiles.isEmpty()) {
                    item {
                        Text("Add a family member to start tracking medicines.")
                    }
                } else if (medicines.isEmpty()) {
                    item {
                        Text("No medicines for this profile yet.")
                        TextButton(onClick = {
                            selectedId?.let(onAddMedicine)
                        }) { Text("Add Medicine") }
                    }
                } else {
                    items(medicines, key = { it.id }) { medicine ->
                        MedicineCard(
                            medicine = medicine,
                            formatExpiry = viewModel::formatExpiry,
                            isLowStock = medicine.isLowStock,
                            isExpiringSoon = viewModel.isExpiringSoon(medicine),
                            onClick = { onMedicineDetail(medicine.id) }
                        )
                    }
                }
            }
        }
    }

    if (showAddProfile) {
        AddProfileDialog(
            onDismiss = { showAddProfile = false },
            onSave = { name, relation, age ->
                viewModel.saveProfile(name, relation, age)
                showAddProfile = false
            }
        )
    }
}

@Composable
private fun ProfileChipRow(
    profiles: List<Profile>,
    selectedId: Long?,
    onSelect: (Long) -> Unit,
    onAddProfile: () -> Unit
) {
    Row(
        Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        profiles.forEach { profile ->
            FilterChip(
                selected = profile.id == selectedId,
                onClick = { onSelect(profile.id) },
                label = { Text("${profile.name} (${profile.relation.displayName})") }
            )
        }
        FilterChip(
            selected = false,
            onClick = onAddProfile,
            label = { Text("+ Member") }
        )
    }
}

@Composable
private fun ExpiringSoonSection(
    medicines: List<Medicine>,
    formatExpiry: (Long) -> String,
    daysUntil: (Medicine) -> Long,
    onClick: (Long) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Warning, contentDescription = null)
                Text(" Expiring Soon (30 days)", fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(8.dp))
            medicines.take(5).forEach { med ->
                TextButton(onClick = { onClick(med.id) }) {
                    Text("${med.name} — ${formatExpiry(med.expiryDateMillis)} (${daysUntil(med)} days)")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MedicineCard(
    medicine: Medicine,
    formatExpiry: (Long) -> String,
    isLowStock: Boolean,
    isExpiringSoon: Boolean,
    onClick: () -> Unit
) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text(medicine.name, fontWeight = FontWeight.Bold)
            Text("${medicine.dosage} • Qty: ${medicine.quantityRemaining}")
            Text("Expires: ${formatExpiry(medicine.expiryDateMillis)}", style = MaterialTheme.typography.bodySmall)
            if (medicine.purpose.isNotBlank()) {
                Text(medicine.purpose, style = MaterialTheme.typography.bodySmall)
            }
            if (isLowStock) {
                Text("Low stock warning", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.SemiBold)
            }
            if (isExpiringSoon) {
                Text("Expiring soon", color = MaterialTheme.colorScheme.tertiary, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditMedicineScreen(
    profileId: Long,
    medicineId: Long?,
    onBack: () -> Unit,
    viewModel: CabinetViewModel = hiltViewModel()
) {
    val editing by viewModel.editingMedicine.collectAsStateWithLifecycle()
    val effectiveProfileId = editing?.profileId?.takeIf { it > 0 } ?: profileId
    var name by remember { mutableStateOf("") }
    var dosage by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var expiry by remember { mutableStateOf(LocalDate.now().plusMonths(6).format(DateTimeFormatter.ISO_LOCAL_DATE)) }
    var purpose by remember { mutableStateOf("") }
    var refillReminder by remember { mutableStateOf(false) }

    LaunchedEffect(medicineId, profileId) {
        if (medicineId != null && medicineId > 0) viewModel.loadMedicine(medicineId)
        else viewModel.startNewMedicine(profileId)
    }

    LaunchedEffect(editing) {
        editing?.let {
            name = it.name
            dosage = it.dosage
            quantity = it.quantityRemaining.toString()
            expiry = java.time.Instant.ofEpochMilli(it.expiryDateMillis)
                .atZone(java.time.ZoneId.systemDefault()).toLocalDate()
                .format(DateTimeFormatter.ISO_LOCAL_DATE)
            purpose = it.purpose
            refillReminder = it.refillReminder
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (medicineId != null && medicineId > 0) "Edit Medicine" else "Add Medicine") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(value = dosage, onValueChange = { dosage = it }, label = { Text("Dosage") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = quantity,
                onValueChange = { quantity = it.filter { c -> c.isDigit() } },
                label = { Text("Quantity remaining") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = expiry,
                onValueChange = { expiry = it },
                label = { Text("Expiry date (yyyy-MM-dd)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(value = purpose, onValueChange = { purpose = it }, label = { Text("Purpose / notes") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            androidx.compose.foundation.layout.Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(checked = refillReminder, onCheckedChange = { refillReminder = it })
                Text("Refill reminder")
            }
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = {
                    viewModel.saveMedicine(
                        profileId = effectiveProfileId,
                        name = name,
                        dosage = dosage,
                        quantity = quantity.toIntOrNull() ?: 0,
                        expiryDateInput = expiry,
                        purpose = purpose,
                        refillReminder = refillReminder
                    )
                    onBack()
                },
                enabled = name.isNotBlank() && dosage.isNotBlank() && quantity.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) { Text("Save") }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicineDetailScreen(
    medicineId: Long,
    onBack: () -> Unit,
    onEdit: (Long) -> Unit,
    viewModel: CabinetViewModel = hiltViewModel()
) {
    LaunchedEffect(medicineId) { viewModel.loadMedicine(medicineId) }
    val medState by viewModel.editingMedicine.collectAsStateWithLifecycle()
    val med = medState
    if (med == null || med.id != medicineId) return

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(med.name) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (med.isLowStock) {
                item { WarningCard("Low stock: ${med.quantityRemaining} remaining (≤ 5)") }
            }
            if (viewModel.isExpiringSoon(med)) {
                item { WarningCard("Expiring in ${viewModel.daysUntilExpiry(med)} days") }
            }
            if (viewModel.isExpired(med)) {
                item { WarningCard("This medicine has expired") }
            }
            item { DetailRow("Dosage", med.dosage) }
            item { DetailRow("Quantity", med.quantityRemaining.toString()) }
            item { DetailRow("Expiry", viewModel.formatExpiry(med.expiryDateMillis)) }
            item { DetailRow("Purpose", med.purpose.ifBlank { "—" }) }
            item { DetailRow("Refill reminder", if (med.refillReminder) "On" else "Off") }
            item {
                Button(onClick = { onEdit(med.id) }, modifier = Modifier.fillMaxWidth()) {
                    Text("Edit")
                }
            }
            item {
                OutlinedButton(
                    onClick = {
                        viewModel.markFinished(med.id)
                        onBack()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Mark as Finished") }
            }
        }
    }
}

@Composable
private fun WarningCard(message: String) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
        Text(message, Modifier.padding(12.dp), fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Column {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
        Text(value, style = MaterialTheme.typography.bodyLarge)
    }
}
