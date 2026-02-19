package com.example.taximuhasebe

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.taximuhasebe.database.AppDatabase
import com.example.taximuhasebe.database.WorkDay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase

    // --- Arayüz Elemanları ---
    private lateinit var timerTextView: TextView
    private lateinit var playPauseButton: ImageButton
    private lateinit var resetButton: ImageButton
    private lateinit var workDaySpinner: Spinner

    // --- Kronometre & Veri Değişkenleri ---
    private var isRunning = false
    private var startTime: Long = 0
    private var elapsedTime: Long = 0
    private var currentWorkDay: WorkDay? = null

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
        setupWorkDaySpinner()

        playPauseButton.setOnClickListener { if (isRunning) pauseTimer() else startTimer() }
        resetButton.setOnClickListener { resetTimer() }
    }

    private fun bindViews() {
        timerTextView = findViewById(R.id.timerTextView)
        playPauseButton = findViewById(R.id.playPauseButton)
        resetButton = findViewById(R.id.resetButton)
        workDaySpinner = findViewById(R.id.workDaySpinner)
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
            val workDay = db.workDayDao().getWorkDayByDate(date)
            currentWorkDay = workDay
            if (workDay != null) {
                elapsedTime = workDay.totalWorkTimeMs
                updateTimerText()
                // updateDashboard(workDay) // GEÇİCİ OLARAK DEVRE DIŞI
                Toast.makeText(this@MainActivity, "Kayıtlı veriler yüklendi!", Toast.LENGTH_SHORT).show()
            } else {
                elapsedTime = 0
                updateTimerText()
                // updateDashboard(null) // GEÇİCİ OLARAK DEVRE DIŞI
            }
        }
    }
    
    /* // BÜYÜK KAZANÇ PANELİ İÇİN OLAN TÜM KODLAR GEÇİCİ OLARAK DEVRE DIŞI BIRAKILDI
    private fun updateDashboard(workDay: WorkDay?) {
        val euro = "€"
        val adet = " Adet"
        if (workDay != null) {
            // ... tüm text atamaları
        } else {
            // ... tüm alanları sıfırlama
        }
    }
    */

    private fun startTimer() {
        if (!isRunning) {
            startTime = SystemClock.uptimeMillis() - elapsedTime
            handler.post(runnable)
            isRunning = true
            playPauseButton.setImageResource(android.R.drawable.ic_media_pause)
            resetButton.visibility = View.VISIBLE

            lifecycleScope.launch {
                val dateForDb = convertDisplayDateToDbDate(workDaySpinner.selectedItem.toString())
                var workDay = db.workDayDao().getWorkDayByDate(dateForDb)
                if (workDay == null) {
                    workDay = WorkDay(date = dateForDb, shiftType = "GÜNDÜZ")
                    db.workDayDao().insertOrUpdate(workDay)
                    currentWorkDay = db.workDayDao().getWorkDayByDate(dateForDb)
                } else {
                    currentWorkDay = workDay
                }
            }
        }
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
                    Toast.makeText(this@MainActivity, "Süre kaydedildi!", Toast.LENGTH_SHORT).show()
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
                it.totalWorkTimeMs = 0
                db.workDayDao().update(it)
                // updateDashboard(it) // GEÇİCİ OLARAK DEVRE DIŞI
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
}
