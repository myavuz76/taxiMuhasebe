package com.example.taximuhasebe.database

import androidx.room.Embedded
import androidx.room.Relation

data class WorkDayWithUmsatz(
    @Embedded val workDay: WorkDay,
    @Relation(
        parentColumn = "id",
        entityColumn = "work_day_id"
    )
    val umsatzList: List<Umsatz>
)
