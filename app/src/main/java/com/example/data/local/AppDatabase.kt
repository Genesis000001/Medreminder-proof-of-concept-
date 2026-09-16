package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.CaregiverLink
import com.example.data.model.DoseLog
import com.example.data.model.DoseStatus
import com.example.data.model.Medication
import com.example.data.model.MedicationForm
import com.example.data.model.ScheduleType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [Medication::class, DoseLog::class, CaregiverLink::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun medicationDao(): MedicationDao
    abstract fun doseLogDao(): DoseLogDao
    abstract fun caregiverDao(): CaregiverDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "med_reminder_database"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Populate initial starter medications and history
                            CoroutineScope(Dispatchers.IO).launch {
                                populateInitialData(getDatabase(context))
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private suspend fun populateInitialData(database: AppDatabase) {
            val medDao = database.medicationDao()
            val logDao = database.doseLogDao()
            val caregiverDao = database.caregiverDao()

            val lisinopril = Medication(
                id = 1,
                name = "Lisinopril",
                dosage = "10 mg",
                form = MedicationForm.PILL,
                scheduleType = ScheduleType.DAILY,
                times = listOf("08:00"),
                daysOfWeek = listOf(1, 2, 3, 4, 5, 6, 7),
                instructions = "Take in the morning with a full glass of water",
                remainingDoses = 24,
                totalQuantity = 30,
                refillThreshold = 5,
                colorHex = 0xFF0284C7
            )

            val metformin = Medication(
                id = 2,
                name = "Metformin",
                dosage = "500 mg",
                form = MedicationForm.CAPSULE,
                scheduleType = ScheduleType.DAILY,
                times = listOf("08:30", "19:30"),
                daysOfWeek = listOf(1, 2, 3, 4, 5, 6, 7),
                instructions = "Take with meals to reduce stomach upset",
                remainingDoses = 12,
                totalQuantity = 60,
                refillThreshold = 8,
                colorHex = 0xFF0D9488
            )

            val atorvastatin = Medication(
                id = 3,
                name = "Atorvastatin",
                dosage = "20 mg",
                form = MedicationForm.PILL,
                scheduleType = ScheduleType.DAILY,
                times = listOf("21:00"),
                daysOfWeek = listOf(1, 2, 3, 4, 5, 6, 7),
                instructions = "Take at bedtime",
                remainingDoses = 4, // Triggers refill low-supply alert!
                totalQuantity = 30,
                refillThreshold = 5,
                colorHex = 0xFF7C3AED
            )

            medDao.insertMedication(lisinopril)
            medDao.insertMedication(metformin)
            medDao.insertMedication(atorvastatin)

            // Add sample recent dose logs for adherence calculations
            val now = System.currentTimeMillis()
            val oneDayMillis = 24 * 60 * 60 * 1000L

            logDao.insertLog(
                DoseLog(
                    medicationId = 1,
                    medicationName = "Lisinopril",
                    dosage = "10 mg",
                    scheduledTime = now - oneDayMillis,
                    actionTime = now - oneDayMillis + 5 * 60 * 1000L,
                    status = DoseStatus.TAKEN
                )
            )
            logDao.insertLog(
                DoseLog(
                    medicationId = 2,
                    medicationName = "Metformin",
                    dosage = "500 mg",
                    scheduledTime = now - oneDayMillis,
                    actionTime = now - oneDayMillis + 10 * 60 * 1000L,
                    status = DoseStatus.TAKEN
                )
            )
            logDao.insertLog(
                DoseLog(
                    medicationId = 3,
                    medicationName = "Atorvastatin",
                    dosage = "20 mg",
                    scheduledTime = now - oneDayMillis,
                    actionTime = now - oneDayMillis,
                    status = DoseStatus.TAKEN
                )
            )

            // Seed sample caregiver link
            caregiverDao.insertCaregiver(
                CaregiverLink(
                    id = "caregiver_demo_1",
                    patientName = "Sarah Jenkins",
                    caregiverName = "Dr. Michael Chen (Family)",
                    inviteCode = "MED-7829-CARE",
                    status = "ACTIVE",
                    linkedAt = now - (7 * oneDayMillis),
                    missedAlertCount = 0
                )
            )
        }
    }
}

