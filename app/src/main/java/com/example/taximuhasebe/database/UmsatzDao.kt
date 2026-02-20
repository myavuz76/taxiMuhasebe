package com.example.taximuhasebe.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface UmsatzDao {

    @Insert
    suspend fun insert(umsatz: Umsatz)

    @Delete
    suspend fun delete(umsatz: Umsatz)

    @Query("SELECT * FROM umsatz_entries WHERE work_day_id = :workDayId ORDER BY timestamp DESC")
    fun getUmsatzForWorkDay(workDayId: Long): Flow<List<Umsatz>>

    // Manuel yenileme ve teşhis için eklendi
    @Query("SELECT * FROM umsatz_entries WHERE work_day_id = :workDayId ORDER BY timestamp DESC")
    suspend fun getUmsatzForWorkDayAsList(workDayId: Long): List<Umsatz>

}
