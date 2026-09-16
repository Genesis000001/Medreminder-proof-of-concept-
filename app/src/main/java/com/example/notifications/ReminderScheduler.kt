package com.example.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.data.model.Medication
import com.example.data.model.ScheduleType
import java.util.Calendar

object ReminderScheduler {

    const val EXTRA_MEDICATION_ID = "extra_medication_id"
    const val EXTRA_MEDICATION_NAME = "extra_medication_name"
    const val EXTRA_DOSAGE = "extra_dosage"
    const val EXTRA_INSTRUCTIONS = "extra_instructions"
    const val EXTRA_SCHEDULED_TIME = "extra_scheduled_time"
    const val EXTRA_IS_ESCALATION = "extra_is_escalation"

    fun scheduleMedicationReminders(context: Context, medication: Medication) {
        if (!medication.isActive || medication.scheduleType == ScheduleType.AS_NEEDED) {
            return
        }

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        medication.times.forEachIndexed { index, timeStr ->
            val nextTriggerMillis = calculateNextTriggerTime(timeStr, medication.scheduleType, medication.daysOfWeek)
            if (nextTriggerMillis > System.currentTimeMillis()) {
                val requestCode = generateRequestCode(medication.id, index)
                val intent = Intent(context, MedicationAlarmReceiver::class.java).apply {
                    putExtra(EXTRA_MEDICATION_ID, medication.id)
                    putExtra(EXTRA_MEDICATION_NAME, medication.name)
                    putExtra(EXTRA_DOSAGE, medication.dosage)
                    putExtra(EXTRA_INSTRUCTIONS, medication.instructions)
                    putExtra(EXTRA_SCHEDULED_TIME, nextTriggerMillis)
                    putExtra(EXTRA_IS_ESCALATION, false)
                }

                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    requestCode,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                scheduleExactAlarm(alarmManager, nextTriggerMillis, pendingIntent)
            }
        }
    }

    fun scheduleEscalationAlarm(
        context: Context,
        medicationId: Long,
        medicationName: String,
        dosage: String,
        instructions: String,
        scheduledTime: Long,
        delayMinutes: Int = 30
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val triggerAt = System.currentTimeMillis() + (delayMinutes * 60 * 1000L)
        val requestCode = (medicationId * 1000 + 999).toInt()

        val intent = Intent(context, MedicationAlarmReceiver::class.java).apply {
            putExtra(EXTRA_MEDICATION_ID, medicationId)
            putExtra(EXTRA_MEDICATION_NAME, medicationName)
            putExtra(EXTRA_DOSAGE, dosage)
            putExtra(EXTRA_INSTRUCTIONS, instructions)
            putExtra(EXTRA_SCHEDULED_TIME, scheduledTime)
            putExtra(EXTRA_IS_ESCALATION, true)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        scheduleExactAlarm(alarmManager, triggerAt, pendingIntent)
    }

    fun scheduleSnoozeAlarm(
        context: Context,
        medicationId: Long,
        medicationName: String,
        dosage: String,
        instructions: String,
        scheduledTime: Long,
        snoozeMinutes: Int = 10
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val triggerAt = System.currentTimeMillis() + (snoozeMinutes * 60 * 1000L)
        val requestCode = (medicationId * 1000 + 888).toInt()

        val intent = Intent(context, MedicationAlarmReceiver::class.java).apply {
            putExtra(EXTRA_MEDICATION_ID, medicationId)
            putExtra(EXTRA_MEDICATION_NAME, medicationName)
            putExtra(EXTRA_DOSAGE, dosage)
            putExtra(EXTRA_INSTRUCTIONS, instructions)
            putExtra(EXTRA_SCHEDULED_TIME, scheduledTime)
            putExtra(EXTRA_IS_ESCALATION, false)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        scheduleExactAlarm(alarmManager, triggerAt, pendingIntent)
    }

    fun cancelRemindersForMedication(context: Context, medication: Medication) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        for (i in 0..10) {
            val requestCode = generateRequestCode(medication.id, i)
            val intent = Intent(context, MedicationAlarmReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent)
                pendingIntent.cancel()
            }
        }
    }

    fun cancelEscalationAlarm(context: Context, medicationId: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val requestCode = (medicationId * 1000 + 999).toInt()
        val intent = Intent(context, MedicationAlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }

    private fun scheduleExactAlarm(
        alarmManager: AlarmManager,
        triggerAtMillis: Long,
        pendingIntent: PendingIntent
    ) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            }
        } catch (e: SecurityException) {
            // Fallback if SCHEDULE_EXACT_ALARM is restricted
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        }
    }

    fun calculateNextTriggerTime(timeStr: String, scheduleType: ScheduleType, daysOfWeek: List<Int>): Long {
        val parts = timeStr.split(":")
        val hour = parts.getOrNull(0)?.toIntOrNull() ?: 8
        val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0

        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // If time already passed today, advance by at least 1 day
        if (target.timeInMillis <= now.timeInMillis) {
            target.add(Calendar.DAY_OF_YEAR, 1)
        }

        // If specific days of week (1=Monday to 7=Sunday)
        if (scheduleType == ScheduleType.SPECIFIC_DAYS && daysOfWeek.isNotEmpty()) {
            for (i in 0..7) {
                val calDay = target.get(Calendar.DAY_OF_WEEK)
                // Convert Calendar.DAY_OF_WEEK (Sunday=1, Monday=2) to 1=Mon..7=Sun
                val isoDay = if (calDay == Calendar.SUNDAY) 7 else calDay - 1
                if (daysOfWeek.contains(isoDay)) {
                    break
                }
                target.add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        return target.timeInMillis
    }

    private fun generateRequestCode(medicationId: Long, index: Int): Int {
        return ((medicationId % 10000) * 100 + index).toInt()
    }
}
