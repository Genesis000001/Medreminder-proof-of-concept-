package com.example.notifications

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.MedReminderApp
import com.example.data.model.DoseStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NotificationActionReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_TAKEN = "com.example.medreminder.ACTION_TAKEN"
        const val ACTION_SKIP = "com.example.medreminder.ACTION_SKIP"
        const val ACTION_SNOOZE = "com.example.medreminder.ACTION_SNOOZE"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val medicationId = intent.getLongExtra(ReminderScheduler.EXTRA_MEDICATION_ID, -1L)
        if (medicationId == -1L) return

        val medicationName = intent.getStringExtra(ReminderScheduler.EXTRA_MEDICATION_NAME) ?: "Medication"
        val dosage = intent.getStringExtra(ReminderScheduler.EXTRA_DOSAGE) ?: ""
        val scheduledTime = intent.getLongExtra(ReminderScheduler.EXTRA_SCHEDULED_TIME, System.currentTimeMillis())
        val instructions = intent.getStringExtra(ReminderScheduler.EXTRA_INSTRUCTIONS) ?: ""

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        // Cancel the dose reminder notification
        notificationManager.cancel(medicationId.toInt())

        // Also cancel the pending escalation alarm since user took action
        ReminderScheduler.cancelEscalationAlarm(context, medicationId)

        val repository = MedReminderApp.repository

        when (intent.action) {
            ACTION_TAKEN -> {
                CoroutineScope(Dispatchers.IO).launch {
                    repository.logDose(
                        medicationId = medicationId,
                        medicationName = medicationName,
                        dosage = dosage,
                        scheduledTime = scheduledTime,
                        status = DoseStatus.TAKEN
                    )

                    // Check if supply is running low
                    val updatedMed = repository.getMedicationById(medicationId)
                    if (updatedMed != null && updatedMed.remainingDoses <= updatedMed.refillThreshold) {
                        showLowSupplyNotification(context, updatedMed.name, updatedMed.remainingDoses)
                    }
                }
            }

            ACTION_SKIP -> {
                CoroutineScope(Dispatchers.IO).launch {
                    repository.logDose(
                        medicationId = medicationId,
                        medicationName = medicationName,
                        dosage = dosage,
                        scheduledTime = scheduledTime,
                        status = DoseStatus.SKIPPED
                    )
                }
            }

            ACTION_SNOOZE -> {
                CoroutineScope(Dispatchers.IO).launch {
                    repository.logDose(
                        medicationId = medicationId,
                        medicationName = medicationName,
                        dosage = dosage,
                        scheduledTime = scheduledTime,
                        status = DoseStatus.SNOOZED,
                        notes = "Snoozed for 10 minutes"
                    )
                }
                // Schedule snooze alarm for 10 minutes
                ReminderScheduler.scheduleSnoozeAlarm(
                    context,
                    medicationId,
                    medicationName,
                    dosage,
                    instructions,
                    scheduledTime,
                    snoozeMinutes = 10
                )
            }
        }
    }

    private fun showLowSupplyNotification(context: Context, medName: String, remaining: Int) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val builder = NotificationCompat.Builder(context, MedReminderApp.CHANNEL_REFILLS)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("⚠️ Refill Reminder: $medName")
            .setContentText("Only $remaining doses left. Consider requesting a refill soon.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        notificationManager.notify((medName.hashCode() % 10000) + 20000, builder.build())
    }
}
