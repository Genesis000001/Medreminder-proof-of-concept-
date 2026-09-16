package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class MedicationForm(val displayName: String, val unitLabel: String) {
    PILL("Pill", "pills"),
    CAPSULE("Capsule", "capsules"),
    LIQUID("Liquid", "ml"),
    INJECTION("Injection", "units"),
    INHALER("Inhaler", "puffs"),
    DROPS("Drops", "drops"),
    PATCH("Patch", "patches"),
    TOPICAL("Topical Cream", "applications"),
    OTHER("Other", "doses")
}

enum class ScheduleType(val displayName: String) {
    DAILY("Daily"),
    SPECIFIC_DAYS("Specific Days"),
    INTERVAL_HOURS("Every X Hours"),
    AS_NEEDED("As Needed (PRN)")
}

@Entity(tableName = "medications")
data class Medication(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val dosage: String,
    val form: MedicationForm = MedicationForm.PILL,
    val scheduleType: ScheduleType = ScheduleType.DAILY,
    val times: List<String> = listOf("08:00"), // Stored as comma separated or list
    val daysOfWeek: List<Int> = listOf(1, 2, 3, 4, 5, 6, 7), // 1=Mon, 7=Sun
    val intervalHours: Int = 0,
    val instructions: String = "",
    val remainingDoses: Int = 30,
    val totalQuantity: Int = 30,
    val refillThreshold: Int = 5,
    val colorHex: Long = 0xFF0284C7,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
