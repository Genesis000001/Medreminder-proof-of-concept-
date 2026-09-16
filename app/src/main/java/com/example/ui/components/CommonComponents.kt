package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Vaccines
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DoseStatus
import com.example.data.model.MedicationForm
import com.example.ui.theme.StatusMissed
import com.example.ui.theme.StatusMissedBg
import com.example.ui.theme.StatusSkipped
import com.example.ui.theme.StatusSkippedBg
import com.example.ui.theme.StatusSnoozed
import com.example.ui.theme.StatusSnoozedBg
import com.example.ui.theme.StatusTaken
import com.example.ui.theme.StatusTakenBg

@Composable
fun MedicalDisclaimerBanner(
    modifier: Modifier = Modifier,
    isLargeText: Boolean = false
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("medical_disclaimer_banner"),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "Medical Disclaimer",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(20.dp)
                    .padding(top = 2.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "MedReminder is an informational reminder and logging tool only. It does not provide medical advice, diagnosis, or treatment. Always consult your doctor or pharmacist.",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = if (isLargeText) 14.sp else 11.sp,
                    lineHeight = if (isLargeText) 18.sp else 15.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun MedicationFormBadge(
    form: MedicationForm,
    colorHex: Long,
    modifier: Modifier = Modifier
) {
    val icon = when (form) {
        MedicationForm.PILL, MedicationForm.CAPSULE -> Icons.Default.Medication
        MedicationForm.INJECTION -> Icons.Default.Vaccines
        else -> Icons.Default.MedicalServices
    }

    Box(
        modifier = modifier
            .size(44.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(colorHex).copy(alpha = 0.15f))
            .border(1.dp, Color(colorHex).copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = form.displayName,
            tint = Color(colorHex),
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
fun StatusBadge(
    status: DoseStatus,
    isLargeText: Boolean = false,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, text) = when (status) {
        DoseStatus.TAKEN -> Triple(StatusTakenBg, StatusTaken, "Taken")
        DoseStatus.SKIPPED -> Triple(StatusSkippedBg, StatusSkipped, "Skipped")
        DoseStatus.SNOOZED -> Triple(StatusSnoozedBg, StatusSnoozed, "Snoozed")
        DoseStatus.MISSED -> Triple(StatusMissedBg, StatusMissed, "Missed")
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = bgColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(textColor)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = text,
                color = textColor,
                fontWeight = FontWeight.Bold,
                fontSize = if (isLargeText) 14.sp else 12.sp
            )
        }
    }
}

@Composable
fun SupplyProgressBar(
    remaining: Int,
    total: Int,
    threshold: Int,
    isLargeText: Boolean = false,
    modifier: Modifier = Modifier
) {
    val progress = if (total > 0) (remaining.toFloat() / total.toFloat()).coerceIn(0f, 1f) else 0f
    val isLow = remaining <= threshold
    val barColor = if (isLow) StatusMissed else MaterialTheme.colorScheme.primary

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$remaining doses left",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = if (isLargeText) 14.sp else 12.sp,
                    fontWeight = if (isLow) FontWeight.Bold else FontWeight.Normal
                ),
                color = if (isLow) StatusMissed else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.weight(1f))
            if (isLow) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Low Supply",
                        tint = StatusMissed,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Low Supply",
                        color = StatusMissed,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = if (isLargeText) 13.sp else 11.sp
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = barColor,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}
