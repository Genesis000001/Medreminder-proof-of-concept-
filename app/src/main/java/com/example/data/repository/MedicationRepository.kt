package com.example.data.repository

import com.example.data.local.CaregiverDao
import com.example.data.local.DoseLogDao
import com.example.data.local.MedicationDao
import com.example.data.model.CaregiverLink
import com.example.data.model.DoseLog
import com.example.data.model.DoseStatus
import com.example.data.model.Medication
import kotlinx.coroutines.flow.Flow
import java.util.Calendar
import java.util.UUID

data class AdherenceStats(
    val totalDoses: Int,
    val takenCount: Int,
    val skippedCount: Int,
    val missedCount: Int,
    val percentage: Int, // 0 - 100
    val currentStreakDays: Int
)

class MedicationRepository(
    private val medicationDao: MedicationDao,
    private val doseLogDao: DoseLogDao,
    private val caregiverDao: CaregiverDao
) {
    val allMedications: Flow<List<Medication>> = medicationDao.getAllMedications()
    val activeMedications: Flow<List<Medication>> = medicationDao.getActiveMedications()
    val allLogs: Flow<List<DoseLog>> = doseLogDao.getAllLogs()
    val recentLogs: Flow<List<DoseLog>> = doseLogDao.getRecentLogs(60)
    val caregivers: Flow<List<CaregiverLink>> = caregiverDao.getAllCaregivers()

    suspend fun getMedicationById(id: Long): Medication? {
        return medicationDao.getMedicationById(id)
    }

    suspend fun addMedication(medication: Medication): Long {
        return medicationDao.insertMedication(medication)
    }

    suspend fun updateMedication(medication: Medication) {
        medicationDao.updateMedication(medication)
    }

    suspend fun deleteMedication(id: Long) {
        medicationDao.deleteMedicationById(id)
    }

    suspend fun logDose(
        medicationId: Long,
        medicationName: String,
        dosage: String,
        scheduledTime: Long,
        status: DoseStatus,
        notes: String = ""
    ) {
        // Record dose log
        doseLogDao.insertLog(
            DoseLog(
                medicationId = medicationId,
                medicationName = medicationName,
                dosage = dosage,
                scheduledTime = scheduledTime,
                actionTime = System.currentTimeMillis(),
                status = status,
                notes = notes
            )
        )

        // If taken, decrement remaining supply
        if (status == DoseStatus.TAKEN) {
            medicationDao.decrementRemainingDoses(medicationId)
        }
    }

    suspend fun updateRemainingSupply(medicationId: Long, newCount: Int) {
        medicationDao.updateRemainingDoses(medicationId, newCount)
    }

    suspend fun generateCaregiverInvite(patientName: String, caregiverName: String = "Caregiver"): CaregiverLink {
        val randomSuffix = (1000..9999).random()
        val inviteCode = "MED-$randomSuffix-CARE"
        val link = CaregiverLink(
            id = UUID.randomUUID().toString(),
            patientName = patientName,
            caregiverName = caregiverName,
            inviteCode = inviteCode,
            status = "ACTIVE",
            linkedAt = System.currentTimeMillis(),
            missedAlertCount = 0
        )
        caregiverDao.insertCaregiver(link)
        return link
    }

    suspend fun revokeCaregiver(id: String) {
        caregiverDao.deleteCaregiverById(id)
    }

    suspend fun incrementMissedAlerts() {
        caregiverDao.incrementMissedAlerts()
    }

    fun computeAdherenceStats(logs: List<DoseLog>): AdherenceStats {
        if (logs.isEmpty()) {
            return AdherenceStats(0, 0, 0, 0, 100, 0)
        }

        val taken = logs.count { it.status == DoseStatus.TAKEN }
        val skipped = logs.count { it.status == DoseStatus.SKIPPED }
        val missed = logs.count { it.status == DoseStatus.MISSED }
        val total = logs.count { it.status != DoseStatus.SNOOZED }

        val percentage = if (total > 0) ((taken.toDouble() / total.toDouble()) * 100).toInt() else 100

        // Calculate consecutive days streak
        val calendar = Calendar.getInstance()
        var streak = 0
        val daysWithTaken = mutableSetOf<String>()

        logs.filter { it.status == DoseStatus.TAKEN }.forEach { log ->
            calendar.timeInMillis = log.actionTime
            val dayKey = "${calendar.get(Calendar.YEAR)}-${calendar.get(Calendar.DAY_OF_YEAR)}"
            daysWithTaken.add(dayKey)
        }

        // Check recent days
        val checkCal = Calendar.getInstance()
        for (i in 0..60) {
            val key = "${checkCal.get(Calendar.YEAR)}-${checkCal.get(Calendar.DAY_OF_YEAR)}"
            if (daysWithTaken.contains(key)) {
                streak++
            } else if (i > 0) { // Allow today to not be logged yet
                break
            }
            checkCal.add(Calendar.DAY_OF_YEAR, -1)
        }

        return AdherenceStats(
            totalDoses = total,
            takenCount = taken,
            skippedCount = skipped,
            missedCount = missed,
            percentage = percentage,
            currentStreakDays = streak
        )
    }
}
