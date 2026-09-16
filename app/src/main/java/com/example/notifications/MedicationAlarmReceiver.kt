package com.example.notifications

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.MedReminderApp
import com.example.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

class MedicationAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val medicationId = intent.getLongExtra(ReminderScheduler.EXTRA_MEDICATION_ID, -1L)
        if (medicationId == -1L) return

        val medicationName = intent.getStringExtra(ReminderScheduler.EXTRA_MEDICATION_NAME) ?: "Medication"
        val dosage = intent.getStringExtra(ReminderScheduler.EXTRA_DOSAGE) ?: ""
        val instructions = intent.getStringExtra(ReminderScheduler.EXTRA_INSTRUCTIONS) ?: ""
        val scheduledTime = intent.getLongExtra(ReminderScheduler.EXTRA_SCHEDULED_TIME, System.currentTimeMillis())
        val isEscalation = intent.getBooleanExtra(ReminderScheduler.EXTRA_IS_ESCALATION, false)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Content intent to open app
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("EXTRA_MEDICATION_ID", medicationId)
        }
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            medicationId.toInt(),
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action: Taken
        val takenIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = NotificationActionReceiver.ACTION_TAKEN
            putExtra(ReminderScheduler.EXTRA_MEDICATION_ID, medicationId)
            putExtra(ReminderScheduler.EXTRA_MEDICATION_NAME, medicationName)
            putExtra(ReminderScheduler.EXTRA_DOSAGE, dosage)
            putExtra(ReminderScheduler.EXTRA_SCHEDULED_TIME, scheduledTime)
        }
        val takenPendingIntent = PendingIntent.getBroadcast(
            context,
            (medicationId * 10 + 1).toInt(),
            takenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action: Skip
        val skipIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = NotificationActionReceiver.ACTION_SKIP
            putExtra(ReminderScheduler.EXTRA_MEDICATION_ID, medicationId)
            putExtra(ReminderScheduler.EXTRA_MEDICATION_NAME, medicationName)
            putExtra(ReminderScheduler.EXTRA_DOSAGE, dosage)
            putExtra(ReminderScheduler.EXTRA_SCHEDULED_TIME, scheduledTime)
        }
        val skipPendingIntent = PendingIntent.getBroadcast(
            context,
            (medicationId * 10 + 2).toInt(),
            skipIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action: Snooze (10 min)
        val snoozeIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = NotificationActionReceiver.ACTION_SNOOZE
            putExtra(ReminderScheduler.EXTRA_MEDICATION_ID, medicationId)
            putExtra(ReminderScheduler.EXTRA_MEDICATION_NAME, medicationName)
            putExtra(ReminderScheduler.EXTRA_DOSAGE, dosage)
            putExtra(ReminderScheduler.EXTRA_INSTRUCTIONS, instructions)
            putExtra(ReminderScheduler.EXTRA_SCHEDULED_TIME, scheduledTime)
        }
        val snoozePendingIntent = PendingIntent.getBroadcast(
            context,
            (medicationId * 10 + 3).toInt(),
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val channelId = if (isEscalation) MedReminderApp.CHANNEL_ESCALATION else MedReminderApp.CHANNEL_REMINDERS
        val title = if (isEscalation) "⚠️ Missed Dose Alert: $medicationName" else "💊 Time for $medicationName ($dosage)"
        val contentText = if (instructions.isNotBlank()) instructions else "Scheduled dose due now"

        val notificationBuilder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(title)
            .setContentText(contentText)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("$dosage • $contentText\nTap 'Taken' once taken to track adherence and update your pill count.")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setContentIntent(contentPendingIntent)
            .setAutoCancel(true)
            .addAction(android.R.drawable.ic_menu_agenda, "Taken", takenPendingIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Skip", skipPendingIntent)
            .addAction(android.R.drawable.ic_lock_idle_alarm, "Snooze 10m", snoozePendingIntent)

        val notificationId = medicationId.toInt()
        notificationManager.notify(notificationId, notificationBuilder.build())

        // If this is the initial reminder, schedule escalation reminder in 30 minutes in case no response
        if (!isEscalation) {
            ReminderScheduler.scheduleEscalationAlarm(
                context,
                medicationId,
                medicationName,
                dosage,
                instructions,
                scheduledTime,
                delayMinutes = 30
            )

            // Reschedule next recurrence for this medication
            CoroutineScope(Dispatchers.IO).launch {
                val med = MedReminderApp.database.medicationDao().getMedicationById(medicationId)
                if (med != null && med.isActive) {
                    ReminderScheduler.scheduleMedicationReminders(context, med)
                }
            }
        } else {
            // Escalation fired without resolution: notify caregiver link if enabled
            CoroutineScope(Dispatchers.IO).launch {
                MedReminderApp.repository.incrementMissedAlerts()
            }
        }
    }
}
