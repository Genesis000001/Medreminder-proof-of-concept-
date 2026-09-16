package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.MedReminderApp
import com.example.data.model.CaregiverLink
import com.example.data.model.DoseLog
import com.example.data.model.DoseStatus
import com.example.data.model.Medication
import com.example.data.model.ScheduleType
import com.example.data.model.UserSettings
import com.example.data.repository.AdherenceStats
import com.example.notifications.ReminderScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale

data class ScheduledDoseItem(
    val medication: Medication,
    val timeString: String,
    val scheduledTimeMillis: Long,
    val log: DoseLog?,
    val isOverdue: Boolean
)

data class MedUiState(
    val medications: List<Medication> = emptyList(),
    val doseLogs: List<DoseLog> = emptyList(),
    val caregivers: List<CaregiverLink> = emptyList(),
    val todayDoses: List<ScheduledDoseItem> = emptyList(),
    val refillAlerts: List<Medication> = emptyList(),
    val adherenceStats: AdherenceStats = AdherenceStats(0, 0, 0, 0, 100, 0),
    val settings: UserSettings = UserSettings(),
    val isSyncing: Boolean = false,
    val syncSuccessMessage: String? = null,
    val refillSuccessMessage: String? = null
)

class MedicationViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = MedReminderApp.repository
    private val context = application.applicationContext

    private val _settings = MutableStateFlow(UserSettings())
    val settings: StateFlow<UserSettings> = _settings.asStateFlow()

    private val _syncMessage = MutableStateFlow<String?>(null)
    private val _refillMessage = MutableStateFlow<String?>(null)
    private val _isSyncing = MutableStateFlow(false)

    val uiState: StateFlow<MedUiState> = combine(
        repository.allMedications,
        repository.allLogs,
        repository.caregivers,
        _settings
    ) { meds, logs, caregivers, settings ->
        val activeMeds = meds.filter { it.isActive }
        val todayDoses = computeTodayDoses(activeMeds, logs)
        val refillAlerts = activeMeds.filter { it.remainingDoses <= it.refillThreshold }
        val adherence = repository.computeAdherenceStats(logs)

        MedUiState(
            medications = meds,
            doseLogs = logs,
            caregivers = caregivers,
            todayDoses = todayDoses,
            refillAlerts = refillAlerts,
            adherenceStats = adherence,
            settings = settings
        )
    }.combine(_isSyncing) { state, syncing ->
        state.copy(isSyncing = syncing)
    }.combine(_syncMessage) { state, syncMsg ->
        state.copy(syncSuccessMessage = syncMsg)
    }.combine(_refillMessage) { state, refillMsg ->
        state.copy(refillSuccessMessage = refillMsg)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MedUiState()
    )

    fun logDose(
        medication: Medication,
        scheduledTime: Long,
        status: DoseStatus,
        notes: String = ""
    ) {
        viewModelScope.launch {
            repository.logDose(
                medicationId = medication.id,
                medicationName = medication.name,
                dosage = medication.dosage,
                scheduledTime = scheduledTime,
                status = status,
                notes = notes
            )
            // Cancel escalation alarm if taken or skipped
            if (status == DoseStatus.TAKEN || status == DoseStatus.SKIPPED) {
                ReminderScheduler.cancelEscalationAlarm(context, medication.id)
            } else if (status == DoseStatus.SNOOZED) {
                ReminderScheduler.scheduleSnoozeAlarm(
                    context,
                    medication.id,
                    medication.name,
                    medication.dosage,
                    medication.instructions,
                    scheduledTime,
                    snoozeMinutes = 10
                )
            }
        }
    }

    fun saveMedication(medication: Medication) {
        viewModelScope.launch {
            val id = if (medication.id == 0L) {
                repository.addMedication(medication)
            } else {
                repository.updateMedication(medication)
                medication.id
            }

            val savedMed = repository.getMedicationById(id)
            if (savedMed != null) {
                ReminderScheduler.cancelRemindersForMedication(context, savedMed)
                if (savedMed.isActive) {
                    ReminderScheduler.scheduleMedicationReminders(context, savedMed)
                }
            }
        }
    }

    fun deleteMedication(medication: Medication) {
        viewModelScope.launch {
            ReminderScheduler.cancelRemindersForMedication(context, medication)
            ReminderScheduler.cancelEscalationAlarm(context, medication.id)
            repository.deleteMedication(medication.id)
        }
    }

    fun updateRemainingSupply(medicationId: Long, newCount: Int) {
        viewModelScope.launch {
            repository.updateRemainingSupply(medicationId, newCount)
        }
    }

    fun requestRefillStub(medication: Medication) {
        viewModelScope.launch {
            _refillMessage.value = "Prescription refill request sent to Pharmacy for ${medication.name} (${medication.dosage})!"
            kotlinx.coroutines.delay(4000)
            _refillMessage.value = null
        }
    }

    fun generateCaregiverInvite(patientName: String) {
        viewModelScope.launch {
            repository.generateCaregiverInvite(patientName)
        }
    }

    fun revokeCaregiver(id: String) {
        viewModelScope.launch {
            repository.revokeCaregiver(id)
        }
    }

    fun simulateCaregiverMissedAlert() {
        viewModelScope.launch {
            repository.incrementMissedAlerts()
        }
    }

    fun updateSettings(newSettings: UserSettings) {
        _settings.value = newSettings
    }

    fun triggerCloudSync() {
        viewModelScope.launch {
            _isSyncing.value = true
            kotlinx.coroutines.delay(1200) // Simulates network sync roundtrip
            val timestamp = System.currentTimeMillis()
            _settings.value = _settings.value.copy(lastSyncTimestamp = timestamp)
            _isSyncing.value = false
            _syncMessage.value = "Medications & adherence successfully synced!"
            kotlinx.coroutines.delay(3500)
            _syncMessage.value = null
        }
    }

    private fun computeTodayDoses(
        activeMeds: List<Medication>,
        allLogs: List<DoseLog>
    ): List<ScheduledDoseItem> {
        val now = System.currentTimeMillis()
        val calendar = Calendar.getInstance()
        val currentDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        val isoDay = if (currentDayOfWeek == Calendar.SUNDAY) 7 else currentDayOfWeek - 1

        val startOfDay = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val endOfDay = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis

        val todayLogs = allLogs.filter { it.actionTime in startOfDay..endOfDay }

        val result = mutableListOf<ScheduledDoseItem>()

        for (med in activeMeds) {
            when (med.scheduleType) {
                ScheduleType.DAILY, ScheduleType.SPECIFIC_DAYS -> {
                    if (med.scheduleType == ScheduleType.SPECIFIC_DAYS && !med.daysOfWeek.contains(isoDay)) {
                        continue
                    }
                    for (timeStr in med.times) {
                        val parts = timeStr.split(":")
                        val hour = parts.getOrNull(0)?.toIntOrNull() ?: 8
                        val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0

                        val doseTime = Calendar.getInstance().apply {
                            set(Calendar.HOUR_OF_DAY, hour)
                            set(Calendar.MINUTE, minute)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }.timeInMillis

                        val matchingLog = todayLogs.firstOrNull { it.medicationId == med.id }
                        val isOverdue = matchingLog == null && now > (doseTime + 15 * 60 * 1000L)

                        result.add(
                            ScheduledDoseItem(
                                medication = med,
                                timeString = timeStr,
                                scheduledTimeMillis = doseTime,
                                log = matchingLog,
                                isOverdue = isOverdue
                            )
                        )
                    }
                }
                ScheduleType.INTERVAL_HOURS -> {
                    val interval = if (med.intervalHours > 0) med.intervalHours else 8
                    var currentHour = 8
                    while (currentHour < 24) {
                        val timeStr = String.format(Locale.getDefault(), "%02d:00", currentHour)
                        val doseTime = Calendar.getInstance().apply {
                            set(Calendar.HOUR_OF_DAY, currentHour)
                            set(Calendar.MINUTE, 0)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }.timeInMillis

                        val matchingLog = todayLogs.firstOrNull {
                            it.medicationId == med.id && Math.abs(it.scheduledTime - doseTime) < 2 * 60 * 60 * 1000L
                        }
                        val isOverdue = matchingLog == null && now > (doseTime + 15 * 60 * 1000L)

                        result.add(
                            ScheduledDoseItem(
                                medication = med,
                                timeString = timeStr,
                                scheduledTimeMillis = doseTime,
                                log = matchingLog,
                                isOverdue = isOverdue
                            )
                        )
                        currentHour += interval
                    }
                }
                ScheduleType.AS_NEEDED -> {
                    // PRN medications can be logged anytime
                }
            }
        }

        return result.sortedBy { it.timeString }
    }
}
