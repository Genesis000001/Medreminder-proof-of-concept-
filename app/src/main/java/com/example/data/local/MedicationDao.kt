package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Medication
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicationDao {
    @Query("SELECT * FROM medications ORDER BY name ASC")
    fun getAllMedications(): Flow<List<Medication>>

    @Query("SELECT * FROM medications WHERE isActive = 1 ORDER BY name ASC")
    fun getActiveMedications(): Flow<List<Medication>>

    @Query("SELECT * FROM medications WHERE id = :id LIMIT 1")
    suspend fun getMedicationById(id: Long): Medication?

    @Query("SELECT * FROM medications WHERE id = :id LIMIT 1")
    fun getMedicationByIdFlow(id: Long): Flow<Medication?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedication(medication: Medication): Long

    @Update
    suspend fun updateMedication(medication: Medication)

    @Delete
    suspend fun deleteMedication(medication: Medication)

    @Query("DELETE FROM medications WHERE id = :id")
    suspend fun deleteMedicationById(id: Long)

    @Query("UPDATE medications SET remainingDoses = CASE WHEN remainingDoses > 0 THEN remainingDoses - 1 ELSE 0 END WHERE id = :id")
    suspend fun decrementRemainingDoses(id: Long)

    @Query("UPDATE medications SET remainingDoses = :newCount WHERE id = :id")
    suspend fun updateRemainingDoses(id: Long, newCount: Int)
}
