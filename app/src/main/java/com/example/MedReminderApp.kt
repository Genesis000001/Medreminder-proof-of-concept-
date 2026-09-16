package com.example

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import com.example.data.local.AppDatabase
import com.example.data.repository.MedicationRepository

class MedReminderApp : Application() {

    companion object {
        const val CHANNEL_REMINDERS = "medication_reminders"
        const val CHANNEL_ESCALATION = "escalation_reminders"
        const val CHANNEL_REFILLS = "refill_alerts"

        lateinit var instance: MedReminderApp
            private set

        val database: AppDatabase by lazy {
            AppDatabase.getDatabase(instance)
        }

        val repository: MedicationRepository by lazy {
            MedicationRepository(database.medicationDao(), database.doseLogDao(), database.caregiverDao())
        }
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .build()

            // Main Medication Reminder Channel
            val reminderChannel = NotificationChannel(
                CHANNEL_REMINDERS,
                "Medication Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Urgent notifications when scheduled medications are due"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500)
                setSound(defaultSoundUri, audioAttributes)
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
            }

            // Escalation Reminder Channel
            val escalationChannel = NotificationChannel(
                CHANNEL_ESCALATION,
                "Escalation & Missed Dose Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Follow-up alerts when a dose hasn't been logged within the response window"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 700, 300, 700)
                setSound(defaultSoundUri, audioAttributes)
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
            }

            // Refill Alerts Channel
            val refillChannel = NotificationChannel(
                CHANNEL_REFILLS,
                "Refill Supply Alerts",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Reminders when your pill or medication supply is running low"
            }

            notificationManager.createNotificationChannels(listOf(reminderChannel, escalationChannel, refillChannel))
        }
    }
}
