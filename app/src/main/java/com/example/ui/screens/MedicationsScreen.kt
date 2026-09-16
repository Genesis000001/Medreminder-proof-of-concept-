package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material.icons.filled.MedicalInformation
import androidx.compose.material.icons.filled.Remove
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Medication
import com.example.data.model.MedicationForm
import com.example.data.model.ScheduleType
import com.example.ui.MedUiState
import com.example.ui.MedicationViewModel
import com.example.ui.components.MedicalDisclaimerBanner
import com.example.ui.components.MedicationFormBadge
import com.example.ui.components.SupplyProgressBar

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MedicationsScreen(
    viewModel: MedicationViewModel,
    uiState: MedUiState,
    modifier: Modifier = Modifier
) {
    val isLargeText = uiState.settings.isLargeText
    var showAddEditDialog by remember { mutableStateOf(false) }
    var selectedMedForEdit by remember { mutableStateOf<Medication?>(null) }
    var medToDelete by remember { mutableStateOf<Medication?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    selectedMedForEdit = null
                    showAddEditDialog = true
                },
                modifier = Modifier.testTag("fab_add_medication"),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Medication")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("medications_list"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "My Medications",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontSize = if (isLargeText) 28.sp else 24.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = "${uiState.medications.count { it.isActive }} active prescriptions",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = if (isLargeText) 16.sp else 13.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (uiState.medications.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 20.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalPharmacy,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No medications added yet",
                                fontWeight = FontWeight.Bold,
                                fontSize = if (isLargeText) 18.sp else 16.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Tap the '+' button below to add your first prescription or over-the-counter medicine.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = if (isLargeText) 14.sp else 12.sp,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                        }
                    }
                }
            } else {
                items(uiState.medications, key = { it.id }) { med ->
                    MedicationListItem(
                        medication = med,
                        isLargeText = isLargeText,
                        onEdit = {
                            selectedMedForEdit = med
                            showAddEditDialog = true
                        },
                        onDelete = {
                            medToDelete = med
                        },
                        onUpdateRemaining = { newCount ->
                            viewModel.updateRemainingSupply(med.id, newCount)
                        },
                        onRequestRefill = {
                            viewModel.requestRefillStub(med)
                        }
                    )
                }
            }

            item {
                MedicalDisclaimerBanner(isLargeText = isLargeText)
            }
        }
    }

    // Add / Edit Dialog
    if (showAddEditDialog) {
        AddEditMedicationDialog(
            initialMedication = selectedMedForEdit,
            isLargeText = isLargeText,
            onDismiss = { showAddEditDialog = false },
            onSave = { newMed ->
                viewModel.saveMedication(newMed)
                showAddEditDialog = false
            }
        )
    }

    // Delete Confirmation Dialog
    medToDelete?.let { med ->
        AlertDialog(
            onDismissRequest = { medToDelete = null },
            title = { Text("Delete Medication") },
            text = { Text("Are you sure you want to delete ${med.name}? All scheduled reminders for this medication will be cancelled.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteMedication(med)
                        medToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { medToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun MedicationListItem(
    medication: Medication,
    isLargeText: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onUpdateRemaining: (Int) -> Unit,
    onRequestRefill: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("med_item_${medication.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                MedicationFormBadge(
                    form = medication.form,
                    colorHex = medication.colorHex
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = medication.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = if (isLargeText) 20.sp else 16.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${medication.dosage} • ${medication.form.displayName}",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = if (isLargeText) 15.sp else 13.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    val scheduleDesc = when (medication.scheduleType) {
                        ScheduleType.DAILY -> "Daily at ${medication.times.joinToString(", ")}"
                        ScheduleType.SPECIFIC_DAYS -> "Days: ${medication.times.joinToString(", ")}"
                        ScheduleType.INTERVAL_HOURS -> "Every ${medication.intervalHours} hours"
                        ScheduleType.AS_NEEDED -> "As needed (PRN)"
                    }

                    Text(
                        text = "⏰ $scheduleDesc",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = if (isLargeText) 14.sp else 12.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )

                    if (medication.instructions.isNotBlank()) {
                        Text(
                            text = medication.instructions,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = if (isLargeText) 13.sp else 11.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Supply Progress Bar
            SupplyProgressBar(
                remaining = medication.remainingDoses,
                total = medication.totalQuantity,
                threshold = medication.refillThreshold,
                isLargeText = isLargeText
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Supply quick counter adjustments & Refill button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { onUpdateRemaining((medication.remainingDoses - 1).coerceAtLeast(0)) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "Decrement Dose", modifier = Modifier.size(16.dp))
                    }
                    Text(
                        text = "${medication.remainingDoses} / ${medication.totalQuantity}",
                        fontSize = if (isLargeText) 14.sp else 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                    IconButton(
                        onClick = { onUpdateRemaining(medication.remainingDoses + 1) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Increment Dose", modifier = Modifier.size(16.dp))
                    }
                }

                if (medication.remainingDoses <= medication.refillThreshold) {
                    OutlinedButton(
                        onClick = onRequestRefill,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.LocalPharmacy, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Order Refill", fontSize = if (isLargeText) 13.sp else 11.sp)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddEditMedicationDialog(
    initialMedication: Medication?,
    isLargeText: Boolean,
    onDismiss: () -> Unit,
    onSave: (Medication) -> Unit
) {
    var name by remember { mutableStateOf(initialMedication?.name ?: "") }
    var dosage by remember { mutableStateOf(initialMedication?.dosage ?: "") }
    var form by remember { mutableStateOf(initialMedication?.form ?: MedicationForm.PILL) }
    var scheduleType by remember { mutableStateOf(initialMedication?.scheduleType ?: ScheduleType.DAILY) }
    var instructions by remember { mutableStateOf(initialMedication?.instructions ?: "") }
    var remainingDoses by remember { mutableIntStateOf(initialMedication?.remainingDoses ?: 30) }
    var totalQuantity by remember { mutableIntStateOf(initialMedication?.totalQuantity ?: 30) }
    var refillThreshold by remember { mutableIntStateOf(initialMedication?.refillThreshold ?: 5) }
    var intervalHours by remember { mutableIntStateOf(initialMedication?.intervalHours ?: 8) }

    val times = remember {
        mutableStateListOf<String>().apply {
            if (initialMedication != null && initialMedication.times.isNotEmpty()) {
                addAll(initialMedication.times)
            } else {
                add("08:00")
            }
        }
    }

    var newTimeInput by remember { mutableStateOf("20:00") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (initialMedication == null) "Add Medication" else "Edit Medication",
                    fontWeight = FontWeight.Bold,
                    fontSize = if (isLargeText) 22.sp else 18.sp
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // OCR / Label Scanner Simulator Helper
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            // Simulate scanning a pill bottle prescription label
                            name = "Amoxicillin"
                            dosage = "500 mg"
                            form = MedicationForm.CAPSULE
                            instructions = "Take 3 times daily with water until finished"
                            remainingDoses = 21
                            totalQuantity = 21
                            times.clear()
                            times.addAll(listOf("08:00", "14:00", "20:00"))
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Scan Label",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Scan Rx Label (Demo Autofill)",
                                fontWeight = FontWeight.Bold,
                                fontSize = if (isLargeText) 14.sp else 12.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "Simulate optical label scan to fill details in 2 seconds",
                                fontSize = if (isLargeText) 12.sp else 10.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }
                }

                // Medication Name
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Medication Name *") },
                    placeholder = { Text("e.g. Lisinopril, Metformin") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_med_name"),
                    singleLine = true
                )

                // Dosage
                OutlinedTextField(
                    value = dosage,
                    onValueChange = { dosage = it },
                    label = { Text("Dosage / Strength *") },
                    placeholder = { Text("e.g. 10 mg, 500 mg, 1 tablet") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_med_dosage"),
                    singleLine = true
                )

                // Medication Form Selector Chips
                Text(
                    text = "Medication Form",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    MedicationForm.entries.forEach { f ->
                        val selected = form == f
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.clickable { form = f }
                        ) {
                            Text(
                                text = f.displayName,
                                color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = if (isLargeText) 13.sp else 11.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                // Schedule Type
                Text(
                    text = "Schedule Frequency",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ScheduleType.entries.forEach { s ->
                        val selected = scheduleType == s
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.clickable { scheduleType = s }
                        ) {
                            Text(
                                text = s.displayName,
                                color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = if (isLargeText) 13.sp else 11.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                // Scheduled Times (for Daily or Specific Days)
                if (scheduleType == ScheduleType.DAILY || scheduleType == ScheduleType.SPECIFIC_DAYS) {
                    Text(
                        text = "Scheduled Dose Times",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        times.forEach { t ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = t,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    if (times.size > 1) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Remove time",
                                            modifier = Modifier
                                                .size(16.dp)
                                                .clickable { times.remove(t) }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Add another time chip
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = newTimeInput,
                            onValueChange = { newTimeInput = it },
                            label = { Text("Time (HH:mm)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (newTimeInput.isNotBlank() && !times.contains(newTimeInput)) {
                                    times.add(newTimeInput)
                                }
                            }
                        ) {
                            Text("Add")
                        }
                    }
                }

                // Instructions
                OutlinedTextField(
                    value = instructions,
                    onValueChange = { instructions = it },
                    label = { Text("Special Instructions (Optional)") },
                    placeholder = { Text("e.g. Take with food, before bedtime") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Remaining Supply & Total
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = remainingDoses.toString(),
                        onValueChange = { remainingDoses = it.toIntOrNull() ?: remainingDoses },
                        label = { Text("Current Supply") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = totalQuantity.toString(),
                        onValueChange = { totalQuantity = it.toIntOrNull() ?: totalQuantity },
                        label = { Text("Total Bottle") },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Refill Threshold
                OutlinedTextField(
                    value = refillThreshold.toString(),
                    onValueChange = { refillThreshold = it.toIntOrNull() ?: refillThreshold },
                    label = { Text("Refill Alert Threshold (Doses)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val toSave = (initialMedication ?: Medication(name = name, dosage = dosage)).copy(
                            name = name.trim(),
                            dosage = dosage.trim(),
                            form = form,
                            scheduleType = scheduleType,
                            times = if (times.isEmpty()) listOf("08:00") else times.toList(),
                            instructions = instructions.trim(),
                            remainingDoses = remainingDoses,
                            totalQuantity = totalQuantity,
                            refillThreshold = refillThreshold,
                            intervalHours = intervalHours
                        )
                        onSave(toSave)
                    }
                },
                modifier = Modifier.testTag("btn_save_medication"),
                enabled = name.isNotBlank()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
