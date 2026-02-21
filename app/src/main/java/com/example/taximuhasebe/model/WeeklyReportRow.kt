package com.example.taximuhasebe.model

data class WeeklyReportRow(
    val dayName: String,
    val brutto: Double,
    val netto: Double,
    val bahsis: Double,
    val nakit: Double,
    val fatura: Double,
    val tmstr: Int,
    val tapp: Double,
    val tkarte: Double,
    val isTotalRow: Boolean = false
)
