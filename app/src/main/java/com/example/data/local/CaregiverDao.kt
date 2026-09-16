package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.CaregiverLink
import kotlinx.coroutines.flow.Flow

@Dao
interface CaregiverDao {
    @Query("SELECT * FROM caregiver_links ORDER BY linkedAt DESC")
    fun getAllCaregivers(): Flow<List<CaregiverLink>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCaregiver(caregiver: CaregiverLink)

    @Update
    suspend fun updateCaregiver(caregiver: CaregiverLink)

    @Delete
    suspend fun deleteCaregiver(caregiver: CaregiverLink)

    @Query("DELETE FROM caregiver_links WHERE id = :id")
    suspend fun deleteCaregiverById(id: String)

    @Query("UPDATE caregiver_links SET missedAlertCount = missedAlertCount + 1 WHERE status = 'ACTIVE'")
    suspend fun incrementMissedAlerts()
}
