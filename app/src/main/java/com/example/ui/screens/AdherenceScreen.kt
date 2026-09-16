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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.data.model.DoseLog
import com.example.data.model.DoseStatus
import com.example.ui.MedUiState
import com.example.ui.components.MedicalDisclaimerBanner
import com.example.ui.components.StatusBadge
import com.example.ui.theme.StatusMissed
import com.example.ui.theme.StatusSkipped
import com.example.ui.theme.StatusSnoozed
import com.example.ui.theme.StatusTaken
import com.example.ui.theme.StatusTakenBg
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun AdherenceScreen(
    uiState: MedUiState,
    modifier: Modifier = Modifier
) {
    val isLargeText = uiState.settings.isLargeText
    val stats = uiState.adherenceStats

    var selectedStatusFilter by remember { mutableStateOf<DoseStatus?>(null) }

    val filteredLogs = remember(uiState.doseLogs, selectedStatusFilter) {
        if (selectedStatusFilter == null) {
            uiState.doseLogs
        } else {
            uiState.doseLogs.filter { it.status == selectedStatusFilter }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("adherence_screen_list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text(
                    text = "Adherence & Health",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontSize = if (isLargeText) 28.sp else 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = "Track your consistency and share progress with your care team",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = if (isLargeText) 16.sp else 13.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Summary Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("adherence_summary_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Weekly Adherence",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontSize = if (isLargeText) 15.sp else 13.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${stats.percentage}%",
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    fontSize = if (isLargeText) 42.sp else 36.sp,
                                    fontWeight = FontWeight.ExtraBold
                                ),
                                color = if (stats.percentage >= 80) StatusTaken else MaterialTheme.colorScheme.primary
                            )
                        }

                        // Streak Indicator
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = StatusTakenBg
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "🔥 ${stats.currentStreakDays} Days",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = if (isLargeText) 18.sp else 16.sp,
                                    color = StatusTaken
                                )
                                Text(
                                    text = "Current Streak",
                                    fontSize = if (isLargeText) 12.sp else 10.sp,
                                    color = StatusTaken
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Progress bar
                    LinearProgressIndicator(
                        progress = { (stats.percentage / 100f).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = StatusTaken,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Metrics Row: Taken, Skipped, Missed
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        MetricItem(
                            label = "Taken",
                            value = "${stats.takenCount}",
                            color = StatusTaken,
                            isLargeText = isLargeText
                        )
                        MetricItem(
                            label = "Skipped",
                            value = "${stats.skippedCount}",
                            color = StatusSkipped,
                            isLargeText = isLargeText
                        )
                        MetricItem(
                            label = "Missed",
                            value = "${stats.missedCount}",
                            color = StatusMissed,
                            isLargeText = isLargeText
                        )
                    }
                }
            }
        }

        // 7-Day Calendar Streak Visualization
        item {
            WeeklyCalendarStrip(
                isLargeText = isLargeText,
                doseLogs = uiState.doseLogs
            )
        }

        // Log History Filter Chips
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Filter:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )

                FilterChip(
                    selected = selectedStatusFilter == null,
                    onClick = { selectedStatusFilter = null },
                    label = { Text("All") }
                )

                FilterChip(
                    selected = selectedStatusFilter == DoseStatus.TAKEN,
                    onClick = { selectedStatusFilter = if (selectedStatusFilter == DoseStatus.TAKEN) null else DoseStatus.TAKEN },
                    label = { Text("Taken") }
                )

                FilterChip(
                    selected = selectedStatusFilter == DoseStatus.SKIPPED,
                    onClick = { selectedStatusFilter = if (selectedStatusFilter == DoseStatus.SKIPPED) null else DoseStatus.SKIPPED },
                    label = { Text("Skipped") }
                )
            }
        }

        // Dose Activity Log Entries
        if (filteredLogs.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.EventNote,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No dose logs recorded yet",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = if (isLargeText) 15.sp else 13.sp
                        )
                    }
                }
            }
        } else {
            items(filteredLogs, key = { it.id }) { log ->
                DoseLogItem(log = log, isLargeText = isLargeText)
            }
        }

        item {
            MedicalDisclaimerBanner(isLargeText = isLargeText)
        }
    }
}

@Composable
fun MetricItem(
    label: String,
    value: String,
    color: Color,
    isLargeText: Boolean
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge.copy(
                fontSize = if (isLargeText) 22.sp else 18.sp,
                fontWeight = FontWeight.Bold
            ),
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(
                fontSize = if (isLargeText) 13.sp else 11.sp
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun WeeklyCalendarStrip(
    isLargeText: Boolean,
    doseLogs: List<DoseLog>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Past 7 Days",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = if (isLargeText) 18.sp else 15.sp,
                    fontWeight = FontWeight.Bold
                )
            )
            Spacer(modifier = Modifier.height(12.dp))

            val days = remember(doseLogs) {
                val list = mutableListOf<DaySummary>()
                val cal = Calendar.getInstance()
                for (i in 6 downTo 0) {
                    val dayCal = Calendar.getInstance().apply {
                        add(Calendar.DAY_OF_YEAR, -i)
                    }
                    val dayLetter = SimpleDateFormat("EEE", Locale.getDefault()).format(dayCal.time).take(1)
                    val dayNumber = SimpleDateFormat("d", Locale.getDefault()).format(dayCal.time)

                    val startOfDay = dayCal.apply {
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                    }.timeInMillis
                    val endOfDay = dayCal.apply {
                        set(Calendar.HOUR_OF_DAY, 23)
                        set(Calendar.MINUTE, 59)
                        set(Calendar.SECOND, 59)
                    }.timeInMillis

                    val logsForDay = doseLogs.filter { it.actionTime in startOfDay..endOfDay }
                    val hasTaken = logsForDay.any { it.status == DoseStatus.TAKEN }
                    val hasMissedOrSkipped = logsForDay.any { it.status == DoseStatus.MISSED || it.status == DoseStatus.SKIPPED }

                    list.add(
                        DaySummary(
                            dayLetter = dayLetter,
                            dayNumber = dayNumber,
                            isToday = i == 0,
                            hasTaken = hasTaken,
                            hasMissed = hasMissedOrSkipped && !hasTaken
                        )
                    )
                }
                list
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                days.forEach { day ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = day.dayLetter,
                            fontSize = if (isLargeText) 13.sp else 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        day.hasTaken -> StatusTakenBg
                                        day.hasMissed -> MaterialTheme.colorScheme.errorContainer
                                        day.isToday -> MaterialTheme.colorScheme.primaryContainer
                                        else -> MaterialTheme.colorScheme.surfaceVariant
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (day.hasTaken) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = StatusTaken,
                                    modifier = Modifier.size(16.dp)
                                )
                            } else {
                                Text(
                                    text = day.dayNumber,
                                    fontWeight = if (day.isToday) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = if (isLargeText) 13.sp else 11.sp,
                                    color = if (day.isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

data class DaySummary(
    val dayLetter: String,
    val dayNumber: String,
    val isToday: Boolean,
    val hasTaken: Boolean,
    val hasMissed: Boolean
)

@Composable
fun DoseLogItem(
    log: DoseLog,
    isLargeText: Boolean,
    modifier: Modifier = Modifier
) {
    val dateFormatted = remember(log.actionTime) {
        SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()).format(Date(log.actionTime))
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("log_item_${log.id}"),
        shape = RoundedCornerShape(12.dp),
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
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = log.medicationName,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = if (isLargeText) 18.sp else 15.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${log.dosage} • $dateFormatted",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = if (isLargeText) 14.sp else 12.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (log.notes.isNotBlank()) {
                    Text(
                        text = log.notes,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = if (isLargeText) 13.sp else 11.sp
                        ),
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }

            StatusBadge(
                status = log.status,
                isLargeText = isLargeText
            )
        }
    }
}
