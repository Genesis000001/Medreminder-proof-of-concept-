package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Medication
import com.example.data.model.MedicationForm
import com.example.data.model.ScheduleType
import com.example.ui.components.MedicalDisclaimerBanner
import com.example.ui.theme.StatusTaken

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OnboardingDialog(
    onComplete: (Medication?, String?) -> Unit,
    onDismiss: () -> Unit
) {
    var step by remember { mutableIntStateOf(1) } // 1: Med info, 2: Reminder time, 3: Caregiver

    var medName by remember { mutableStateOf("Metformin") }
    var dosage by remember { mutableStateOf("500 mg") }
    var form by remember { mutableStateOf(MedicationForm.CAPSULE) }
    var timeString by remember { mutableStateOf("08:00") }
    var instructions by remember { mutableStateOf("Take with breakfast") }
    var caregiverName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Quick Setup (Step $step of 3)",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "< 2 min",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { step / 3f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                when (step) {
                    1 -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Medication,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "What is your primary medication?",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }

                        OutlinedTextField(
                            value = medName,
                            onValueChange = { medName = it },
                            label = { Text("Medication Name") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = dosage,
                            onValueChange = { dosage = it },
                            label = { Text("Dosage (e.g. 10 mg)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Text("Form", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(MedicationForm.PILL, MedicationForm.CAPSULE, MedicationForm.LIQUID, MedicationForm.INJECTION).forEach { f ->
                                val sel = form == f
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (sel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier.clickable { form = f }
                                ) {
                                    Text(
                                        text = f.displayName,
                                        color = if (sel) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }

                    2 -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "When should we remind you?",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }

                        Text(
                            text = "MedReminder will schedule an exact offline alarm on your device clock for $medName.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        OutlinedTextField(
                            value = timeString,
                            onValueChange = { timeString = it },
                            label = { Text("Reminder Time (HH:mm)") },
                            placeholder = { Text("08:00") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = instructions,
                            onValueChange = { instructions = it },
                            label = { Text("Instructions (e.g. With food)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }

                    3 -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.PersonAdd,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Optional Caregiver Link",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }

                        Text(
                            text = "You can optionally invite a caregiver to monitor adherence and receive missed-dose alerts. (No account needed)",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        OutlinedTextField(
                            value = caregiverName,
                            onValueChange = { caregiverName = it },
                            label = { Text("Caregiver Name or Relation (Optional)") },
                            placeholder = { Text("e.g. Dr. Michael / Daughter Emma") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = StatusTaken,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Ready to start immediately 100% offline.",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                MedicalDisclaimerBanner()
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (step < 3) {
                        step++
                    } else {
                        val med = if (medName.isNotBlank()) {
                            Medication(
                                name = medName.trim(),
                                dosage = dosage.trim(),
                                form = form,
                                scheduleType = ScheduleType.DAILY,
                                times = listOf(timeString.ifBlank { "08:00" }),
                                instructions = instructions.trim(),
                                remainingDoses = 30,
                                totalQuantity = 30
                            )
                        } else null
                        onComplete(med, caregiverName.ifBlank { null })
                    }
                }
            ) {
                Text(if (step < 3) "Continue" else "Finish Setup")
            }
        },
        dismissButton = {
            if (step > 1) {
                TextButton(onClick = { step-- }) {
                    Text("Back")
                }
            } else {
                TextButton(onClick = onDismiss) {
                    Text("Skip")
                }
            }
        }
    )
}
