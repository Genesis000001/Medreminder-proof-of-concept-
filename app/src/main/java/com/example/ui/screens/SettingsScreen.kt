package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.ui.MedUiState
import com.example.ui.MedicationViewModel
import com.example.ui.components.MedicalDisclaimerBanner
import com.example.ui.theme.StatusTaken
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SettingsScreen(
    viewModel: MedicationViewModel,
    uiState: MedUiState,
    modifier: Modifier = Modifier
) {
    val settings = uiState.settings
    val isLargeText = settings.isLargeText

    var emailInput by remember { mutableStateOf(settings.accountEmail ?: "sarah.jenkins@example.com") }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("settings_screen_list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text(
                    text = "Settings & Accessibility",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontSize = if (isLargeText) 28.sp else 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = "Customize reminder behavior, appearance, and privacy",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = if (isLargeText) 16.sp else 13.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Accessibility Section
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Accessibility,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Elderly & Visual Accessibility",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontSize = if (isLargeText) 18.sp else 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Large Text Mode Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Large Text Mode",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = if (isLargeText) 16.sp else 14.sp
                            )
                            Text(
                                text = "Enlarges labels, buttons, and dose instructions for easier reading",
                                fontSize = if (isLargeText) 13.sp else 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = settings.isLargeText,
                            onCheckedChange = {
                                viewModel.updateSettings(settings.copy(isLargeText = it))
                            },
                            modifier = Modifier.testTag("switch_large_text")
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // High Contrast Mode Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "High Contrast Mode",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = if (isLargeText) 16.sp else 14.sp
                            )
                            Text(
                                text = "High-contrast dark palette with bold borders for improved visibility",
                                fontSize = if (isLargeText) 13.sp else 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = settings.isHighContrast,
                            onCheckedChange = {
                                viewModel.updateSettings(settings.copy(isHighContrast = it))
                            },
                            modifier = Modifier.testTag("switch_high_contrast")
                        )
                    }
                }
            }
        }

        // Notification & Quiet Hours Section
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Reminders & Quiet Hours",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontSize = if (isLargeText) 18.sp else 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Quiet Hours
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Quiet Hours (Sleep Mode)",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = if (isLargeText) 16.sp else 14.sp
                            )
                            Text(
                                text = "10:00 PM – 7:00 AM (silence non-urgent alarms)",
                                fontSize = if (isLargeText) 13.sp else 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = settings.quietHoursEnabled,
                            onCheckedChange = {
                                viewModel.updateSettings(settings.copy(quietHoursEnabled = it))
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Escalation Window Setting
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Escalation Reminder Window",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = if (isLargeText) 16.sp else 14.sp
                            )
                            Text(
                                text = "Follow-up alert if unacknowledged after ${settings.escalationWindowMinutes} minutes",
                                fontSize = if (isLargeText) 13.sp else 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = "${settings.escalationWindowMinutes} min",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                fontSize = if (isLargeText) 14.sp else 12.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Sound & Vibration
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Sound & Vibration",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = if (isLargeText) 16.sp else 14.sp
                        )
                        Switch(
                            checked = settings.soundEnabled,
                            onCheckedChange = {
                                viewModel.updateSettings(settings.copy(soundEnabled = it, vibrationEnabled = it))
                            }
                        )
                    }
                }
            }
        }

        // Offline-First & Sync Section
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = StatusTaken
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Privacy & Offline Reliability",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontSize = if (isLargeText) 18.sp else 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "• All dose alarms schedule directly on your device's hardware clock. They fire 100% offline, including in Airplane mode.\n• Medication health data is encrypted at rest in local Room database storage (HIPAA/GDPR privacy design).",
                        fontSize = if (isLargeText) 14.sp else 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Cloud Sync Account
                    Text(
                        text = "Optional Cloud Backup & Sync",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = if (isLargeText) 16.sp else 14.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = emailInput,
                        onValueChange = { emailInput = it },
                        label = { Text("Account Email (Optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val syncDate = if (settings.lastSyncTimestamp != null) {
                            SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()).format(Date(settings.lastSyncTimestamp))
                        } else {
                            "Never"
                        }

                        Text(
                            text = "Last synced: $syncDate",
                            fontSize = if (isLargeText) 13.sp else 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Button(
                            onClick = {
                                viewModel.updateSettings(settings.copy(accountEmail = emailInput))
                                viewModel.triggerCloudSync()
                            },
                            enabled = !uiState.isSyncing,
                            modifier = Modifier.testTag("btn_sync_now")
                        ) {
                            if (uiState.isSyncing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Syncing...")
                            } else {
                                Icon(Icons.Default.CloudSync, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Sync Now")
                            }
                        }
                    }

                    if (uiState.syncSuccessMessage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = uiState.syncSuccessMessage,
                            color = StatusTaken,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = if (isLargeText) 13.sp else 11.sp
                        )
                    }
                }
            }
        }

        // Persistent Disclaimer
        item {
            MedicalDisclaimerBanner(isLargeText = isLargeText)
        }
    }
}
