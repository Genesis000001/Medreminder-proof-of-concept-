package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.model.DoseStatus
import com.example.data.model.Medication
import com.example.data.model.MedicationForm
import com.example.data.model.ScheduleType
import com.example.data.repository.MedicationRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    private lateinit var database: AppDatabase
    private lateinit var repository: MedicationRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = MedicationRepository(database.medicationDao(), database.doseLogDao(), database.caregiverDao())
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `read string from context matches MedReminder`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("MedReminder", appName)
    }

    @Test
    fun `database and repository add medication and compute adherence`() = runBlocking {
        // Add a test medication
        val medId = repository.addMedication(
            Medication(
                name = "Metformin",
                dosage = "500 mg",
                form = MedicationForm.CAPSULE,
                scheduleType = ScheduleType.DAILY,
                times = listOf("08:00", "20:00"),
                instructions = "Take with meal",
                remainingDoses = 20,
                totalQuantity = 30,
                refillThreshold = 5
            )
        )

        val retrievedMed = repository.getMedicationById(medId)
        assertNotNull(retrievedMed)
        assertEquals("Metformin", retrievedMed?.name)
        assertEquals(20, retrievedMed?.remainingDoses)

        // Log a taken dose
        repository.logDose(
            medicationId = medId,
            medicationName = "Metformin",
            dosage = "500 mg",
            scheduledTime = System.currentTimeMillis(),
            status = DoseStatus.TAKEN
        )

        // Remaining supply should be decremented from 20 to 19
        val updatedMed = repository.getMedicationById(medId)
        assertEquals(19, updatedMed?.remainingDoses)

        // Compute adherence stats
        val logs = repository.allLogs.first()
        val stats = repository.computeAdherenceStats(logs)
        assertEquals(1, stats.takenCount)
        assertEquals(0, stats.skippedCount)
        assertEquals(100, stats.percentage)
        assertEquals(1, stats.currentStreakDays)
    }
}
