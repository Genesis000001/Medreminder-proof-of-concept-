package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CaregiverLink
import com.example.ui.MedUiState
import com.example.ui.MedicationViewModel
import com.example.ui.components.MedicalDisclaimerBanner
import com.example.ui.components.StatusBadge
import com.example.ui.theme.StatusMissed
import com.example.ui.theme.StatusMissedBg
import com.example.ui.theme.StatusTaken
import com.example.ui.theme.StatusTakenBg
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CaregiverScreen(
    viewModel: MedicationViewModel,
    uiState: MedUiState,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isLargeText = uiState.settings.isLargeText

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Patient View, 1: Caregiver View

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("caregiver_screen_list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text(
                    text = "Caregiver Support",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontSize = if (isLargeText) 28.sp else 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = "Connect family or medical caregivers for safety and adherence monitoring",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = if (isLargeText) 16.sp else 13.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Mode Switch Tabs
        item {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.clip(RoundedCornerShape(12.dp))
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Text(
                            text = "Patient Controls",
                            fontWeight = FontWeight.Bold,
                            fontSize = if (isLargeText) 15.sp else 13.sp
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Text(
                            text = "Caregiver Dashboard",
                            fontWeight = FontWeight.Bold,
                            fontSize = if (isLargeText) 15.sp else 13.sp
                        )
                    }
                )
            }
        }

        if (selectedTab == 0) {
            // Patient View: Generate invite code & manage caregivers
            item {
                PatientInviteCard(
                    patientName = uiState.settings.patientName,
                    onGenerateCode = {
                        viewModel.generateCaregiverInvite(uiState.settings.patientName)
                    },
                    isLargeText = isLargeText,
                    context = context
                )
            }

            item {
                Text(
                    text = "Active Caregiver Links (${uiState.caregivers.size})",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = if (isLargeText) 18.sp else 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            if (uiState.caregivers.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.PersonAdd,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No caregivers linked yet",
                                fontWeight = FontWeight.Bold,
                                fontSize = if (isLargeText) 16.sp else 14.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Generate an invite link above to share adherence with a loved one or nurse.",
                                fontSize = if (isLargeText) 13.sp else 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(uiState.caregivers, key = { it.id }) { link ->
                    CaregiverLinkItem(
                        link = link,
                        isLargeText = isLargeText,
                        onRevoke = { viewModel.revokeCaregiver(link.id) }
                    )
                }
            }

            // Test missed dose escalation
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Escalation & Safety Test",
                                fontWeight = FontWeight.Bold,
                                fontSize = if (isLargeText) 16.sp else 14.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Test how the app flags missed doses to the caregiver if unacknowledged after ${uiState.settings.escalationWindowMinutes} minutes.",
                            fontSize = if (isLargeText) 13.sp else 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = {
                                viewModel.simulateCaregiverMissedAlert()
                                Toast.makeText(context, "Simulated missed dose alert sent to caregiver dashboard!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.testTag("simulate_escalation_button")
                        ) {
                            Text("Simulate Missed Dose Alert")
                        }
                    }
                }
            }
        } else {
            // Caregiver Read-Only Dashboard View
            item {
                CaregiverDashboardView(
                    patientName = uiState.settings.patientName,
                    uiState = uiState,
                    isLargeText = isLargeText,
                    onContactPatient = {
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, "Hi ${uiState.settings.patientName}, just checking in on your medication schedule today.")
                        }
                        context.startActivity(Intent.createChooser(intent, "Check in with patient"))
                    }
                )
            }
        }

        item {
            MedicalDisclaimerBanner(isLargeText = isLargeText)
        }
    }
}

@Composable
fun PatientInviteCard(
    patientName: String,
    onGenerateCode: () -> Unit,
    isLargeText: Boolean,
    context: Context,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("patient_invite_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Caregiver Invite",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = if (isLargeText) 18.sp else 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = "Invite a trusted family member or nurse",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = if (isLargeText) 13.sp else 11.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "MED-7829-CARE",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )

                    Row {
                        IconButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("Caregiver Code", "MED-7829-CARE"))
                                Toast.makeText(context, "Invite code copied to clipboard", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy code", modifier = Modifier.size(18.dp))
                        }

                        IconButton(
                            onClick = {
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(
                                        Intent.EXTRA_TEXT,
                                        "Hi, here is my MedReminder caregiver access code to monitor my medication adherence: MED-7829-CARE (Patient: $patientName)"
                                    )
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Share Caregiver Invite"))
                            }
                        ) {
                            Icon(Icons.Default.Share, contentDescription = "Share code", modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "• Caregivers get read-only access to your adherence logs\n• Caregivers receive urgent alerts if you miss a scheduled dose",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = if (isLargeText) 13.sp else 11.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = onGenerateCode,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Generate New Code & Link")
            }
        }
    }
}

@Composable
fun CaregiverLinkItem(
    link: CaregiverLink,
    isLargeText: Boolean,
    onRevoke: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(StatusTakenBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = StatusTaken,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = link.caregiverName,
                        fontWeight = FontWeight.Bold,
                        fontSize = if (isLargeText) 16.sp else 14.sp
                    )
                    Text(
                        text = "Status: ${link.status} • Code: ${link.inviteCode}",
                        fontSize = if (isLargeText) 13.sp else 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            OutlinedButton(
                onClick = onRevoke,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text("Revoke", fontSize = if (isLargeText) 13.sp else 11.sp)
            }
        }
    }
}

@Composable
fun CaregiverDashboardView(
    patientName: String,
    uiState: MedUiState,
    isLargeText: Boolean,
    onContactPatient: () -> Unit,
    modifier: Modifier = Modifier
) {
    val totalMissedAlerts = uiState.caregivers.sumOf { it.missedAlertCount }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Read-Only Dashboard Banner
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Read-Only Caregiver Mode",
                        fontWeight = FontWeight.Bold,
                        fontSize = if (isLargeText) 16.sp else 14.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "Monitoring Patient: $patientName",
                        fontSize = if (isLargeText) 14.sp else 12.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
                Button(
                    onClick = onContactPatient,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("Check In", fontSize = if (isLargeText) 13.sp else 11.sp)
                }
            }
        }

        // Missed Dose Escalation Alert (if any)
        if (totalMissedAlerts > 0) {
            ElevatedCard(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = StatusMissedBg),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = StatusMissed,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "⚠️ Urgent: Missed Dose Escalation",
                            fontWeight = FontWeight.Bold,
                            fontSize = if (isLargeText) 16.sp else 14.sp,
                            color = StatusMissed
                        )
                        Text(
                            text = "$patientName has $totalMissedAlerts unacknowledged dose reminder(s) exceeding the 30-minute escalation window.",
                            fontSize = if (isLargeText) 13.sp else 11.sp,
                            color = Color(0xFF7F1D1D)
                        )
                    }
                }
            }
        }

        // Patient Overview Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "$patientName's Adherence Health",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = if (isLargeText) 18.sp else 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${uiState.adherenceStats.percentage}%",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = if (isLargeText) 24.sp else 20.sp,
                            color = StatusTaken
                        )
                        Text("Adherence", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${uiState.adherenceStats.currentStreakDays} days",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = if (isLargeText) 24.sp else 20.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text("Streak", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${uiState.medications.count { it.isActive }}",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = if (isLargeText) 24.sp else 20.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text("Active Meds", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        // Patient's Active Prescriptions & Remaining Supplies
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Active Prescriptions & Supply",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = if (isLargeText) 18.sp else 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = Modifier.height(10.dp))
                uiState.medications.filter { it.isActive }.forEach { med ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "${med.name} (${med.dosage})",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = if (isLargeText) 15.sp else 13.sp
                            )
                            Text(
                                text = "Times: ${med.times.joinToString(", ")}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Text(
                            text = "${med.remainingDoses} doses left",
                            fontWeight = FontWeight.Bold,
                            fontSize = if (isLargeText) 14.sp else 12.sp,
                            color = if (med.remainingDoses <= med.refillThreshold) StatusMissed else StatusTaken
                        )
                    }
                }
            }
        }
    }
}
