package com.example.data.local

import androidx.room.TypeConverter
import com.example.data.model.DoseStatus
import com.example.data.model.MedicationForm
import com.example.data.model.ScheduleType

class Converters {
    @TypeConverter
    fun fromStringList(value: List<String>?): String {
        return value?.joinToString(",") ?: ""
    }

    @TypeConverter
    fun toStringList(value: String?): List<String> {
        if (value.isNullOrBlank()) return emptyList()
        return value.split(",").map { it.trim() }.filter { it.isNotEmpty() }
    }

    @TypeConverter
    fun fromIntList(value: List<Int>?): String {
        return value?.joinToString(",") ?: ""
    }

    @TypeConverter
    fun toIntList(value: String?): List<Int> {
        if (value.isNullOrBlank()) return emptyList()
        return value.split(",").mapNotNull { it.trim().toIntOrNull() }
    }

    @TypeConverter
    fun fromMedicationForm(value: MedicationForm?): String {
        return value?.name ?: MedicationForm.PILL.name
    }

    @TypeConverter
    fun toMedicationForm(value: String?): MedicationForm {
        return try {
            if (value != null) MedicationForm.valueOf(value) else MedicationForm.PILL
        } catch (e: Exception) {
            MedicationForm.PILL
        }
    }

    @TypeConverter
    fun fromScheduleType(value: ScheduleType?): String {
        return value?.name ?: ScheduleType.DAILY.name
    }

    @TypeConverter
    fun toScheduleType(value: String?): ScheduleType {
        return try {
            if (value != null) ScheduleType.valueOf(value) else ScheduleType.DAILY
        } catch (e: Exception) {
            ScheduleType.DAILY
        }
    }

    @TypeConverter
    fun fromDoseStatus(value: DoseStatus?): String {
        return value?.name ?: DoseStatus.TAKEN.name
    }

    @TypeConverter
    fun toDoseStatus(value: String?): DoseStatus {
        return try {
            if (value != null) DoseStatus.valueOf(value) else DoseStatus.TAKEN
        } catch (e: Exception) {
            DoseStatus.TAKEN
        }
    }
}
