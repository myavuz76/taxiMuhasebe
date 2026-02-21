package com.example.taximuhasebe

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.taximuhasebe.adapter.UmsatzAdapter
import com.example.taximuhasebe.adapter.WeeklySummaryAdapter
import com.example.taximuhasebe.database.AppDatabase
import com.example.taximuhasebe.database.Umsatz
import com.example.taximuhasebe.database.WorkDay
import com.example.taximuhasebe.database.WorkDayWithUmsatz
import com.example.taximuhasebe.model.WeeklyReportRow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase
    private lateinit var umsatzAdapter: UmsatzAdapter
    private lateinit var weeklyAdapter: WeeklySummaryAdapter

    // --- Arayüz Elemanları ---
    private lateinit var timerTextView: TextView
    private lateinit var playPauseButton: ImageButton
    private lateinit var resetButton: ImageButton
    private lateinit var workDaySpinner: Spinner
    private lateinit var umsatzRecyclerView: RecyclerView
    private lateinit var weeklyRecyclerView: RecyclerView
    private lateinit var titleTextView: TextView
    private lateinit var showMoreButton: ImageView

    // --- Liste Yönetimi ---
    private var isShowingAll = false
    private var fullUmsatzList: List<Umsatz> = emptyList()

    private var isRunning = false
    private var startTime: Long = 0
    private var elapsedTime: Long = 0
    private var currentWorkDay: WorkDay? = null

    private var testDataCounter = 0
    private val testCases = listOf(
        Triple("Bolt", "bar", null),
        Triple("Bolt", "app", null),
        Triple("Uber", "bar", null),
        Triple("Uber", "app", null),
        Triple("F-Now", "bar", null),
        Triple("F-Now", "app", null),
        Triple("Funk", "bar", true),
        Triple("Funk", "bar", false),
        Triple("Funk", "karte", null),
        Triple("Funk", "inkasso", null),
        Triple("Einst", "bar", true),
        Triple("Einst", "bar", false),
        Triple("Einst", "karte", null)
    )

    private val handler = Handler(Looper.getMainLooper())
    private val runnable = object : Runnable {
        override fun run() {
            if (isRunning) {
                val currentTime = SystemClock.uptimeMillis()
                elapsedTime = currentTime - startTime
                updateTimerText()
                handler.postDelayed(this, 300)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        db = AppDatabase.getDatabase(this)

        bindViews()
        setupRecyclerViews()
        setupWorkDaySpinner()
        setupTestButton()

        showMoreButton.setOnClickListener { 
            isShowingAll = !isShowingAll
            updateUmsatzAdapter()
        }

        playPauseButton.setOnClickListener { if (isRunning) pauseTimer() else startTimer() }
        resetButton.setOnClickListener { resetTimer() }
    }

    private fun bindViews() {
        timerTextView = findViewById(R.id.timerTextView)
        playPauseButton = findViewById(R.id.playPauseButton)
        resetButton = findViewById(R.id.resetButton)
        workDaySpinner = findViewById(R.id.workDaySpinner)
        umsatzRecyclerView = findViewById(R.id.umsatz_recycler_view)
        weeklyRecyclerView = findViewById(R.id.weekly_summary_recyclerview)
        titleTextView = findViewById(R.id.titleTextView)
        showMoreButton = findViewById(R.id.show_more_button)
    }

    private fun setupRecyclerViews() {
        // Günlük Liste Adapter
        umsatzAdapter = UmsatzAdapter(
            onEditClick = { umsatz ->
                Toast.makeText(this@MainActivity, "Düzenle tıklandı: ${umsatz.id}", Toast.LENGTH_SHORT).show()
            },
            onDeleteClick = { umsatz ->
                lifecycleScope.launch{
                    db.umsatzDao().delete(umsatz)
                    refreshUmsatzList()
                }
            }
        )
        umsatzRecyclerView.adapter = umsatzAdapter
        umsatzRecyclerView.layoutManager = LinearLayoutManager(this)

        // Haftalık Liste Adapter
        weeklyAdapter = WeeklySummaryAdapter()
        weeklyRecyclerView.adapter = weeklyAdapter
        weeklyRecyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun setupWorkDaySpinner() {
        val dateList = ArrayList<String>()
        val calendar = Calendar.getInstance()
        val displaySdf = SimpleDateFormat("dd.MM - EEE", Locale("de", "DE"))
        for (i in 0..6) {
            dateList.add(displaySdf.format(calendar.time))
            calendar.add(Calendar.DAY_OF_YEAR, -1)
        }
        val adapter = ArrayAdapter(this, R.layout.custom_spinner_item, dateList)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        workDaySpinner.adapter = adapter
        workDaySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p: AdapterView<*>, v: View?, pos: Int, id: Long) {
                loadWorkDayData(convertDisplayDateToDbDate(p.getItemAtPosition(pos) as String))
            }
            override fun onNothingSelected(p: AdapterView<*>) {}
        }
    }

    private fun loadWorkDayData(date: String) {
        lifecycleScope.launch {
            currentWorkDay = db.workDayDao().getWorkDayByDate(date)
            isShowingAll = false 
            if (currentWorkDay != null) {
                elapsedTime = currentWorkDay!!.totalWorkTimeMs
                updateTimerText()
                refreshUmsatzList()
            } else {
                elapsedTime = 0
                updateTimerText()
                fullUmsatzList = emptyList()
                updateUmsatzAdapter()
            }
        }
    }

    private fun startTimer() {
        if (!isRunning) {
            lifecycleScope.launch {
                if (currentWorkDay == null) {
                    val dateForDb = convertDisplayDateToDbDate(workDaySpinner.selectedItem.toString())
                    val newWorkDay = WorkDay(date = dateForDb, shiftType = "GÜNDÜZ")
                    val newId = db.workDayDao().insert(newWorkDay)
                    currentWorkDay = db.workDayDao().getWorkDayById(newId)
                    refreshUmsatzList()
                }
                actuallyStartTimer()
            }
        }
    }

    private suspend fun refreshUmsatzList() {
        currentWorkDay?.let {
            fullUmsatzList = db.umsatzDao().getUmsatzForWorkDayAsList(it.id)
            updateUmsatzAdapter()
            loadWeeklyData()
        } ?: run {
            fullUmsatzList = emptyList()
            updateUmsatzAdapter()
            loadWeeklyData()
        }
    }
    
    private fun updateUmsatzAdapter(){
        umsatzAdapter.setTotalItemCount(fullUmsatzList.size)

        val listToShow = if (isShowingAll) fullUmsatzList else fullUmsatzList.take(5)
        umsatzAdapter.submitList(listToShow)

        if (fullUmsatzList.size > 5) {
            showMoreButton.visibility = View.VISIBLE
            if(isShowingAll) {
                showMoreButton.setImageResource(android.R.drawable.arrow_up_float)
            } else {
                showMoreButton.setImageResource(android.R.drawable.arrow_down_float)
            }
        } else {
            showMoreButton.visibility = View.GONE
        }
    }

    // --- Haftalık Özet Fonksiyonları ---
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

    private fun actuallyStartTimer(){
        startTime = SystemClock.uptimeMillis() - elapsedTime
        handler.post(runnable)
        isRunning = true
        playPauseButton.setImageResource(android.R.drawable.ic_media_pause)
        resetButton.visibility = View.VISIBLE
    }

    private fun pauseTimer() {
        if (isRunning) {
            handler.removeCallbacks(runnable)
            isRunning = false
            playPauseButton.setImageResource(android.R.drawable.ic_media_play)

            lifecycleScope.launch {
                currentWorkDay?.let {
                    it.totalWorkTimeMs = elapsedTime
                    db.workDayDao().update(it)
                }
            }
        }
    }

    private fun resetTimer() {
        if (isRunning) {
            handler.removeCallbacks(runnable)
            isRunning = false
            playPauseButton.setImageResource(android.R.drawable.ic_media_play)
        }
        elapsedTime = 0
        updateTimerText()
        resetButton.visibility = View.GONE

        lifecycleScope.launch {
            currentWorkDay?.let {
                val updatedWorkDay = it.copy(totalWorkTimeMs = 0)
                db.workDayDao().update(updatedWorkDay)
                Toast.makeText(this@MainActivity, "Süre sıfırlandı ve kaydedildi!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateTimerText() {
        val seconds = (elapsedTime / 1000) % 60
        val minutes = (elapsedTime / (1000 * 60)) % 60
        val hours = (elapsedTime / (1000 * 60 * 60)) % 24
        val timeString = String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)
        timerTextView.text = timeString
    }

    private fun convertDisplayDateToDbDate(displayDate: String): String {
        val datePart = displayDate.split(" - ")[0]
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        return "$datePart.$currentYear"
    }

    private fun setupTestButton() {
        titleTextView.setOnClickListener {
            currentWorkDay?.let { workDay ->
                lifecycleScope.launch {
                    val testCase = testCases[testDataCounter % testCases.size]
                    testDataCounter++

                    val source = testCase.first
                    val paymentType = testCase.second
                    val isFaturaExplicit = testCase.third

                    val umsatz = Random.nextDouble(10.0, 80.0)
                    val bahsis = if (Random.nextBoolean()) Random.nextDouble(1.0, 10.0) else 0.0
                    val netto = umsatz - bahsis

                    val faturaAmount = if (source == "Bolt" || source == "Uber" || source == "F-Now" ||
                        (source == "Funk" && (paymentType == "karte" || paymentType == "inkasso")) ||
                        (source == "Einst" && paymentType == "karte") ||
                        ((source == "Funk" || source == "Einst") && paymentType == "bar" && isFaturaExplicit == true)) {
                        netto
                    } else {
                        0.0
                    }

                    val newUmsatz = Umsatz(
                        workDayId = workDay.id,
                        source = source,
                        paymentType = paymentType,
                        umsatzAmount = umsatz,
                        nettoAmount = netto,
                        bahsisAmount = bahsis,
                        faturaAmount = faturaAmount
                    )
                    db.umsatzDao().insert(newUmsatz)
                    refreshUmsatzList()

                    val faturaText = if(faturaAmount > 0) " (Faturalı)" else " (Faturasız)"
                    Toast.makeText(this@MainActivity, "Test: ${source}/${paymentType}${faturaText} eklendi", Toast.LENGTH_SHORT).show()
                }
            } ?: Toast.makeText(this@MainActivity, "Lütfen önce bir iş günü başlatın", Toast.LENGTH_SHORT).show()
        }
    }
}
