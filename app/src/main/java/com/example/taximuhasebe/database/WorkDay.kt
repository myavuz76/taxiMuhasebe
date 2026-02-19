package com.example.taximuhasebe.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "work_days")
data class WorkDay(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0L,

    @ColumnInfo(name = "date")
    val date: String,

    @ColumnInfo(name = "total_work_time_ms")
    var totalWorkTimeMs: Long = 0L,

    @ColumnInfo(name = "shift_type")
    val shiftType: String = "GÜNDÜZ"

    // --- Kazanç Paneli Sütunları GEÇİCİ OLARAK DEVRE DIŞI BIRAKILDI ---
    /*
    // Sol Taraf
    @ColumnInfo(name = "funk_amount")
    var funkAmount: Double = 0.0,
    @ColumnInfo(name = "einst_amount")
    var einstAmount: Double = 0.0,
    @ColumnInfo(name = "customer_count")
    var customerCount: Int = 0,

    // Orta Kısım
    @ColumnInfo(name = "gercek_amount")
    var gercekAmount: Double = 0.0,
    @ColumnInfo(name = "tip_amount")
    var tipAmount: Double = 0.0,
    @ColumnInfo(name = "vergi_amount")
    var vergiAmount: Double = 0.0,

    // Sağ Taraf
    @ColumnInfo(name = "bolt_amount")
    var boltAmount: Double = 0.0,
    @ColumnInfo(name = "uber_amount")
    var uberAmount: Double = 0.0,
    @ColumnInfo(name = "fnow_amount")
    var fnowAmount: Double = 0.0,

    // Alt Kartlar
    @ColumnInfo(name = "app_amount")
    var appAmount: Double = 0.0,
    @ColumnInfo(name = "app_count")
    var appCount: Int = 0,

    @ColumnInfo(name = "card_amount")
    var cardAmount: Double = 0.0,
    @ColumnInfo(name = "card_count")
    var cardCount: Int = 0,

    @ColumnInfo(name = "fatura_amount")
    var faturaAmount: Double = 0.0,
    @ColumnInfo(name = "fatura_count")
    var faturaCount: Int = 0,

    @ColumnInfo(name = "inkasso_amount")
    var inkassoAmount: Double = 0.0,
    @ColumnInfo(name = "inkasso_count")
    var inkassoCount: Int = 0
    */
)
