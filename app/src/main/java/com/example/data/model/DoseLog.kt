package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class DoseStatus(val displayName: String) {
    TAKEN("Taken"),
    SKIPPED("Skipped"),
    SNOOZED("Snoozed"),
    MISSED("Missed")
}

@Entity(tableName = "dose_logs")
data class DoseLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val medicationId: Long,
    val medicationName: String,
    val dosage: String,
    val scheduledTime: Long,
    val actionTime: Long = System.currentTimeMillis(),
    val status: DoseStatus,
    val notes: String = ""
)
