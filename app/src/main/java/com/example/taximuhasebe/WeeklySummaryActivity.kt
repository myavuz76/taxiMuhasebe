package com.example.taximuhasebe

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.taximuhasebe.adapter.WeeklySummaryAdapter
import com.example.taximuhasebe.database.AppDatabase
import com.example.taximuhasebe.database.WorkDayWithUmsatz
import com.example.taximuhasebe.model.WeeklyReportRow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class WeeklySummaryActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase
    private lateinit var weeklyAdapter: WeeklySummaryAdapter
    private lateinit var weeklyRecyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_weekly_summary)

        db = AppDatabase.getDatabase(this)
        weeklyRecyclerView = findViewById(R.id.weekly_summary_recyclerview)
        weeklyAdapter = WeeklySummaryAdapter()
        weeklyRecyclerView.adapter = weeklyAdapter
        weeklyRecyclerView.layoutManager = LinearLayoutManager(this)

        loadWeeklyData()
    }

    private fun loadWeeklyData() {
        lifecycleScope.launch {
            val (startOfWeek, endOfWeek) = getWeekDateRange()
            val weeklyData = db.workDayDao().getWorkDaysWithUmsatz(startOfWeek, endOfWeek)
            val reportRows = processWeeklyData(weeklyData)
            weeklyAdapter.submitList(reportRows)
        }
    }

    private fun getWeekDateRange(): Pair<String, String> {
        val calendar = Calendar.getInstance()
        calendar.firstDayOfWeek = Calendar.MONDAY
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.GERMANY)
        val startOfWeek = sdf.format(calendar.time)
        calendar.add(Calendar.DAY_OF_WEEK, 6)
        val endOfWeek = sdf.format(calendar.time)
        return Pair(startOfWeek, endOfWeek)
    }

    private fun processWeeklyData(data: List<WorkDayWithUmsatz>): List<WeeklyReportRow> {
        val rows = mutableListOf<WeeklyReportRow>()

        data.forEach { dayWithUmsatz ->
            val dayName = SimpleDateFormat("EEEE", Locale("de", "DE")).format(SimpleDateFormat("dd.MM.yyyy", Locale.GERMANY).parse(dayWithUmsatz.workDay.date)!!)
            val brutto = dayWithUmsatz.umsatzList.sumOf { it.umsatzAmount }
            val netto = dayWithUmsatz.umsatzList.sumOf { it.nettoAmount }
            val bahsis = dayWithUmsatz.umsatzList.sumOf { it.bahsisAmount }
            val fatura = dayWithUmsatz.umsatzList.sumOf { it.faturaAmount }
            val tmstr = dayWithUmsatz.umsatzList.size
            val tapp = dayWithUmsatz.umsatzList.filter { it.paymentType.equals("app", true) }.sumOf { it.umsatzAmount }
            val tkarte = dayWithUmsatz.umsatzList.filter { it.paymentType.equals("karte", true) }.sumOf { it.umsatzAmount }
            val inkasso = dayWithUmsatz.umsatzList.filter { it.paymentType.equals("inkasso", true) }.sumOf { it.umsatzAmount }
            val nakit = netto + bahsis - tapp - tkarte - inkasso

            rows.add(WeeklyReportRow(dayName, brutto, netto, bahsis, nakit, fatura, tmstr, tapp, tkarte))
        }

        if (rows.isNotEmpty()) {
            val totalBrutto = rows.sumOf { it.brutto }
            val totalNetto = rows.sumOf { it.netto }
            val totalBahsis = rows.sumOf { it.bahsis }
            val totalNakit = rows.sumOf { it.nakit }
            val totalFatura = rows.sumOf { it.fatura }
            val totalTmstr = rows.sumOf { it.tmstr.toDouble() }.toInt()
            val totalTapp = rows.sumOf { it.tapp }
            val totalTkarte = rows.sumOf { it.tkarte }

            rows.add(WeeklyReportRow("TOPLAM", totalBrutto, totalNetto, totalBahsis, totalNakit, totalFatura, totalTmstr, totalTapp, totalTkarte, isTotalRow = true))
        }

        return rows
    }
}
