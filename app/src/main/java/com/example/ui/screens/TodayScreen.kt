package com.example.ui.screens

import android.content.Context
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Snooze
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DoseStatus
import com.example.data.model.Medication
import com.example.notifications.MedicationAlarmReceiver
import com.example.notifications.ReminderScheduler
import com.example.ui.MedUiState
import com.example.ui.MedicationViewModel
import com.example.ui.ScheduledDoseItem
import com.example.ui.components.MedicalDisclaimerBanner
import com.example.ui.components.MedicationFormBadge
import com.example.ui.components.StatusBadge
import com.example.ui.theme.StatusMissed
import com.example.ui.theme.StatusMissedBg
import com.example.ui.theme.StatusSnoozed
import com.example.ui.theme.StatusTaken
import com.example.ui.theme.StatusTakenBg
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TodayScreen(
    viewModel: MedicationViewModel,
    uiState: MedUiState,
    onNavigateToMedications: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isLargeText = uiState.settings.isLargeText

    val todayDateFormatted = remember {
        SimpleDateFormat("EEEE, MMMM d", Locale.getDefault()).format(Date())
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("today_screen_list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header
        item {
            Column {
                Text(
                    text = todayDateFormatted,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontSize = if (isLargeText) 16.sp else 13.sp
                    ),
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Today's Schedule",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontSize = if (isLargeText) 28.sp else 24.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    // Streak Badge
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.testTag("streak_badge")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "🔥 ${uiState.adherenceStats.currentStreakDays}d streak",
                                fontWeight = FontWeight.Bold,
                                fontSize = if (isLargeText) 14.sp else 12.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }
        }

        // Refill Warning Banner
        if (uiState.refillAlerts.isNotEmpty()) {
            item {
                RefillAlertBanner(
                    alerts = uiState.refillAlerts,
                    onRequestRefill = { med -> viewModel.requestRefillStub(med) },
                    isLargeText = isLargeText
                )
            }
        }

        // Notification Message Toast / Alert
        if (uiState.refillSuccessMessage != null) {
            item {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = StatusTakenBg,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = StatusTaken
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = uiState.refillSuccessMessage,
                            color = StatusTaken,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = if (isLargeText) 14.sp else 12.sp
                        )
                    }
                }
            }
        }

        // Test Offline Notification Action Bar
        item {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Test Offline Reminder",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = if (isLargeText) 15.sp else 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }

                    Button(
                        onClick = {
                            triggerImmediateTestNotification(context, uiState.medications.firstOrNull())
                        },
                        modifier = Modifier.testTag("test_reminder_button"),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Trigger Now",
                            fontSize = if (isLargeText) 13.sp else 11.sp
                        )
                    }
                }
            }
        }

        // Dose Schedule Timeline
        if (uiState.todayDoses.isEmpty()) {
            item {
                EmptyTodayScheduleCard(
                    onAddMedication = onNavigateToMedications,
                    isLargeText = isLargeText
                )
            }
        } else {
            item {
                Text(
                    text = "Doses Due (${uiState.todayDoses.count { it.log == null }} remaining)",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = if (isLargeText) 18.sp else 15.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            items(uiState.todayDoses, key = { "${it.medication.id}_${it.timeString}" }) { doseItem ->
                DoseCard(
                    doseItem = doseItem,
                    isLargeText = isLargeText,
                    onMarkTaken = {
                        viewModel.logDose(
                            medication = doseItem.medication,
                            scheduledTime = doseItem.scheduledTimeMillis,
                            status = DoseStatus.TAKEN
                        )
                    },
                    onMarkSkipped = {
                        viewModel.logDose(
                            medication = doseItem.medication,
                            scheduledTime = doseItem.scheduledTimeMillis,
                            status = DoseStatus.SKIPPED
                        )
                    },
                    onSnooze = {
                        viewModel.logDose(
                            medication = doseItem.medication,
                            scheduledTime = doseItem.scheduledTimeMillis,
                            status = DoseStatus.SNOOZED
                        )
                    }
                )
            }
        }

        // Persistent Medical Disclaimer Banner
        item {
            MedicalDisclaimerBanner(isLargeText = isLargeText)
        }
    }
}

@Composable
fun DoseCard(
    doseItem: ScheduledDoseItem,
    isLargeText: Boolean,
    onMarkTaken: () -> Unit,
    onMarkSkipped: () -> Unit,
    onSnooze: () -> Unit,
    modifier: Modifier = Modifier
) {
    val med = doseItem.medication
    val log = doseItem.log
    val isLogged = log != null

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("dose_card_${med.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isLogged) MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
            else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isLogged) 1.dp else 3.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = if (doseItem.isOverdue) 1.5.dp else 1.dp,
            color = if (doseItem.isOverdue) StatusMissed.copy(alpha = 0.6f)
            else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )
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
                    form = med.form,
                    colorHex = med.colorHex
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = med.name,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontSize = if (isLargeText) 20.sp else 16.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        // Scheduled Time Pill
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (doseItem.isOverdue) StatusMissedBg else MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Schedule,
                                    contentDescription = null,
                                    tint = if (doseItem.isOverdue) StatusMissed else MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = doseItem.timeString,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = if (isLargeText) 13.sp else 11.sp,
                                    color = if (doseItem.isOverdue) StatusMissed else MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "${med.dosage} • ${med.form.displayName}",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = if (isLargeText) 16.sp else 13.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (med.instructions.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = med.instructions,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = if (isLargeText) 14.sp else 11.sp
                            ),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action or Logged Status State
            if (isLogged) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    StatusBadge(
                        status = log!!.status,
                        isLargeText = isLargeText
                    )

                    val timeFormatted = remember(log.actionTime) {
                        SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(log.actionTime))
                    }
                    Text(
                        text = "Logged at $timeFormatted",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = if (isLargeText) 13.sp else 11.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                // Action Buttons: Taken, Skip, Snooze
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onMarkTaken,
                        modifier = Modifier
                            .weight(1.5f)
                            .testTag("action_taken_${med.id}"),
                        colors = ButtonDefaults.buttonColors(containerColor = StatusTaken),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Taken",
                            fontWeight = FontWeight.Bold,
                            fontSize = if (isLargeText) 15.sp else 13.sp
                        )
                    }

                    OutlinedButton(
                        onClick = onMarkSkipped,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("action_skip_${med.id}"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = "Skip",
                            fontSize = if (isLargeText) 14.sp else 12.sp
                        )
                    }

                    OutlinedButton(
                        onClick = onSnooze,
                        modifier = Modifier
                            .weight(1.2f)
                            .testTag("action_snooze_${med.id}"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Snooze,
                            contentDescription = null,
                            tint = StatusSnoozed,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "10m",
                            color = StatusSnoozed,
                            fontWeight = FontWeight.Bold,
                            fontSize = if (isLargeText) 14.sp else 12.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RefillAlertBanner(
    alerts: List<Medication>,
    onRequestRefill: (Medication) -> Unit,
    isLargeText: Boolean,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("refill_alert_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = StatusMissedBg)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = StatusMissed,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Low Supply Alert (${alerts.size} medication)",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = if (isLargeText) 18.sp else 15.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = StatusMissed
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            alerts.forEach { med ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = med.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = if (isLargeText) 16.sp else 14.sp,
                            color = Color(0xFF7F1D1D)
                        )
                        Text(
                            text = "Only ${med.remainingDoses} doses left (threshold: ${med.refillThreshold})",
                            fontSize = if (isLargeText) 13.sp else 11.sp,
                            color = Color(0xFF991B1B)
                        )
                    }

                    Button(
                        onClick = { onRequestRefill(med) },
                        colors = ButtonDefaults.buttonColors(containerColor = StatusMissed),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("request_refill_button_${med.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalPharmacy,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Refill",
                            fontSize = if (isLargeText) 13.sp else 11.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyTodayScheduleCard(
    onAddMedication: () -> Unit,
    isLargeText: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("empty_schedule_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "No doses scheduled for today",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = if (isLargeText) 18.sp else 16.sp,
                    fontWeight = FontWeight.Bold
                )
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Add your medications with their scheduled times to see them here.",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = if (isLargeText) 14.sp else 12.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onAddMedication,
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Add Medication")
            }
        }
    }
}

private fun triggerImmediateTestNotification(context: Context, sampleMed: Medication?) {
    val med = sampleMed ?: Medication(
        id = 1,
        name = "Lisinopril",
        dosage = "10 mg",
        instructions = "Take with food or water"
    )

    val intent = Intent(context, MedicationAlarmReceiver::class.java).apply {
        putExtra(ReminderScheduler.EXTRA_MEDICATION_ID, med.id)
        putExtra(ReminderScheduler.EXTRA_MEDICATION_NAME, med.name)
        putExtra(ReminderScheduler.EXTRA_DOSAGE, med.dosage)
        putExtra(ReminderScheduler.EXTRA_INSTRUCTIONS, med.instructions)
        putExtra(ReminderScheduler.EXTRA_SCHEDULED_TIME, System.currentTimeMillis())
        putExtra(ReminderScheduler.EXTRA_IS_ESCALATION, false)
    }

    context.sendBroadcast(intent)
}
