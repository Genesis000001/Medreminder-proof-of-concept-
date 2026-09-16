package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.DoseLog
import kotlinx.coroutines.flow.Flow

@Dao
interface DoseLogDao {
    @Query("SELECT * FROM dose_logs ORDER BY actionTime DESC")
    fun getAllLogs(): Flow<List<DoseLog>>

    @Query("SELECT * FROM dose_logs WHERE medicationId = :medicationId ORDER BY actionTime DESC")
    fun getLogsForMedication(medicationId: Long): Flow<List<DoseLog>>

    @Query("SELECT * FROM dose_logs WHERE actionTime >= :startTime AND actionTime <= :endTime ORDER BY actionTime DESC")
    fun getLogsBetween(startTime: Long, endTime: Long): Flow<List<DoseLog>>

    @Query("SELECT * FROM dose_logs ORDER BY actionTime DESC LIMIT :limit")
    fun getRecentLogs(limit: Int = 50): Flow<List<DoseLog>>

    @Query("SELECT * FROM dose_logs WHERE medicationId = :medicationId AND scheduledTime = :scheduledTime LIMIT 1")
    suspend fun getLogForScheduledDose(medicationId: Long, scheduledTime: Long): DoseLog?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: DoseLog): Long

    @Query("DELETE FROM dose_logs WHERE id = :id")
    suspend fun deleteLogById(id: Long)
}
