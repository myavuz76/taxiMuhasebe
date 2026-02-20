package com.example.taximuhasebe.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "umsatz_entries",
    foreignKeys = [ForeignKey(
        entity = WorkDay::class,
        parentColumns = ["id"],
        childColumns = ["work_day_id"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class Umsatz(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0L,

    @ColumnInfo(name = "work_day_id", index = true)
    var workDayId: Long,

    @ColumnInfo(name = "source")
    val source: String,

    @ColumnInfo(name = "payment_type")
    val paymentType: String,

    @ColumnInfo(name = "umsatz_amount")
    val umsatzAmount: Double,

    @ColumnInfo(name = "netto_amount")
    val nettoAmount: Double,

    @ColumnInfo(name = "bahsis_amount")
    val bahsisAmount: Double, // Tip

    @ColumnInfo(name = "fatura_amount")
    val faturaAmount: Double,

    @ColumnInfo(name = "timestamp")
    val timestamp: Long = System.currentTimeMillis()
)
