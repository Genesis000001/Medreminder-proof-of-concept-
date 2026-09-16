package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "caregiver_links")
data class CaregiverLink(
    @PrimaryKey
    val id: String,
    val patientName: String,
    val caregiverName: String,
    val inviteCode: String,
    val status: String = "ACTIVE", // "ACTIVE" or "REVOKED"
    val linkedAt: Long = System.currentTimeMillis(),
    val missedAlertCount: Int = 0
)

data class UserSettings(
    val isLargeText: Boolean = false,
    val isHighContrast: Boolean = false,
    val quietHoursEnabled: Boolean = false,
    val quietHoursStart: String = "22:00",
    val quietHoursEnd: String = "07:00",
    val escalationWindowMinutes: Int = 30,
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val patientName: String = "Sarah Jenkins",
    val accountEmail: String? = null,
    val lastSyncTimestamp: Long? = null,
    val onboardingCompleted: Boolean = false
)
