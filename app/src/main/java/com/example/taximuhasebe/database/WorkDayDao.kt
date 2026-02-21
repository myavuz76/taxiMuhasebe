package com.example.taximuhasebe.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update

@Dao
interface WorkDayDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(workDay: WorkDay): Long

    @Update
    suspend fun update(workDay: WorkDay)

    @Query("SELECT * FROM work_days WHERE date = :date LIMIT 1")
    suspend fun getWorkDayByDate(date: String): WorkDay?

    @Query("SELECT * FROM work_days WHERE id = :id LIMIT 1")
    suspend fun getWorkDayById(id: Long): WorkDay?

    @Query("SELECT * FROM work_days ORDER BY date DESC")
    suspend fun getAllWorkDays(): List<WorkDay>

    @Transaction
    @Query("SELECT * FROM work_days WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    suspend fun getWorkDaysWithUmsatz(startDate: String, endDate: String): List<WorkDayWithUmsatz>
}
