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
import com.example.taximuhasebe.database.AppDatabase
import com.example.taximuhasebe.database.Umsatz
import com.example.taximuhasebe.database.WorkDay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase
    private lateinit var umsatzAdapter: UmsatzAdapter

    // --- Arayüz Elemanları ---
    private lateinit var timerTextView: TextView
    private lateinit var playPauseButton: ImageButton
    private lateinit var resetButton: ImageButton
    private lateinit var workDaySpinner: Spinner
    private lateinit var umsatzRecyclerView: RecyclerView
    private lateinit var titleTextView: TextView
    private lateinit var showMoreButton: ImageView
    private lateinit var customerCountTextView: TextView
    // Özet Paneli TextViews
    private lateinit var funkAmountTextView: TextView
    private lateinit var einstAmountTextView: TextView
    private lateinit var gercekAmountTextView: TextView
    private lateinit var tipAmountTextView: TextView
    private lateinit var vergiAmountTextView: TextView
    private lateinit var boltAmountTextView: TextView
    private lateinit var uberAmountTextView: TextView
    private lateinit var fnowAmountTextView: TextView
    // Alt Kartlar TextViews
    private lateinit var appAmountTextView: TextView
    private lateinit var appCountTextView: TextView
    private lateinit var cardAmountTextView: TextView
    private lateinit var cardCountTextView: TextView
    private lateinit var faturaAmountTextView: TextView
    private lateinit var faturaCountTextView: TextView
    private lateinit var inkassoAmountTextView: TextView
    private lateinit var inkassoCountTextView: TextView

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
        setupRecyclerView()
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
        titleTextView = findViewById(R.id.titleTextView)
        showMoreButton = findViewById(R.id.show_more_button)
        customerCountTextView = findViewById(R.id.customer_count_textview)
        // Özet Paneli
        funkAmountTextView = findViewById(R.id.funkAmountTextView)
        einstAmountTextView = findViewById(R.id.einstAmountTextView)
        gercekAmountTextView = findViewById(R.id.gercekAmountTextView)
        tipAmountTextView = findViewById(R.id.tipAmountTextView)
        vergiAmountTextView = findViewById(R.id.vergiAmountTextView)
        boltAmountTextView = findViewById(R.id.boltAmountTextView)
        uberAmountTextView = findViewById(R.id.uberAmountTextView)
        fnowAmountTextView = findViewById(R.id.fnowAmountTextView)
        // Alt Kartlar
        appAmountTextView = findViewById(R.id.appAmountTextView)
        appCountTextView = findViewById(R.id.appCountTextView)
        cardAmountTextView = findViewById(R.id.cardAmountTextView)
        cardCountTextView = findViewById(R.id.cardCountTextView)
        faturaAmountTextView = findViewById(R.id.faturaAmountTextView)
        faturaCountTextView = findViewById(R.id.faturaCountTextView)
        inkassoAmountTextView = findViewById(R.id.inkassoAmountTextView)
        inkassoCountTextView = findViewById(R.id.inkassoCountTextView)
    }

    private fun setupRecyclerView() {
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
            updateSummaryPanel()
        } ?: run {
            fullUmsatzList = emptyList()
            updateUmsatzAdapter()
            updateSummaryPanel()
        }
    }
    
    private fun updateUmsatzAdapter(){
        customerCountTextView.text = fullUmsatzList.size.toString()

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

     private fun updateSummaryPanel() {
        val funkTotal = fullUmsatzList.filter { it.source.equals("Funk", true) }.sumOf { it.umsatzAmount }
        val einstTotal = fullUmsatzList.filter { it.source.equals("Einst", true) }.sumOf { it.umsatzAmount }
        val boltTotal = fullUmsatzList.filter { it.source.equals("Bolt", true) }.sumOf { it.umsatzAmount }
        val uberTotal = fullUmsatzList.filter { it.source.equals("Uber", true) }.sumOf { it.umsatzAmount }
        val fnowTotal = fullUmsatzList.filter { it.source.equals("F-Now", true) }.sumOf { it.umsatzAmount }

        val totalBahsis = fullUmsatzList.sumOf { it.bahsisAmount }
        val totalVergiGenel = fullUmsatzList.filter { !it.source.equals("Bolt", true) }.sumOf { it.nettoAmount } * 0.19

        // Bolt için vergi hesaplaması
        val boltNetto = fullUmsatzList.filter { it.source.equals("Bolt", true) }.sumOf { it.nettoAmount }
        val boltVergi = boltNetto * 0.18
        val boltSonrasiNetto = boltNetto - boltVergi
        
        // Diğerleri için netto toplamı
        val digerleriNetto = fullUmsatzList.filter { !it.source.equals("Bolt", true) }.sumOf { it.nettoAmount }
        
        val gercekTotal = boltSonrasiNetto + digerleriNetto + totalBahsis

        val appCount = fullUmsatzList.count { it.paymentType.equals("app", true) }
        val appAmount = fullUmsatzList.filter { it.paymentType.equals("app", true) }.sumOf { it.umsatzAmount }
        val cardCount = fullUmsatzList.count { it.paymentType.equals("karte", true) }
        val cardAmount = fullUmsatzList.filter { it.paymentType.equals("karte", true) }.sumOf { it.umsatzAmount }
        val faturaCount = fullUmsatzList.count { it.faturaAmount > 0 }
        val faturaAmount = fullUmsatzList.sumOf { it.faturaAmount }
        val inkassoCount = fullUmsatzList.count { it.paymentType.equals("inkasso", true) }
        val inkassoAmount = fullUmsatzList.filter { it.paymentType.equals("inkasso", true) }.sumOf { it.umsatzAmount }

        // Update UI
        funkAmountTextView.text = String.format(Locale.GERMANY, "%.2f€", funkTotal)
        einstAmountTextView.text = String.format(Locale.GERMANY, "%.2f€", einstTotal)
        boltAmountTextView.text = String.format(Locale.GERMANY, "%.2f€", boltTotal)
        uberAmountTextView.text = String.format(Locale.GERMANY, "%.2f€", uberTotal)
        fnowAmountTextView.text = String.format(Locale.GERMANY, "%.2f€", fnowTotal)
        
        gercekAmountTextView.text = String.format(Locale.GERMANY, "%.2f", gercekTotal)
        tipAmountTextView.text = String.format(Locale.GERMANY, "Tip: %.1f", totalBahsis)
        vergiAmountTextView.text = String.format(Locale.GERMANY, "VERGİ: -%.2f€", totalVergiGenel + boltVergi)

        appAmountTextView.text = String.format(Locale.GERMANY, "%.2f€", appAmount)
        appCountTextView.text = "$appCount Adet"
        cardAmountTextView.text = String.format(Locale.GERMANY, "%.2f€", cardAmount)
        cardCountTextView.text = "$cardCount Adet"
        faturaAmountTextView.text = String.format(Locale.GERMANY, "%.2f€", faturaAmount)
        faturaCountTextView.text = "$faturaCount Adet"
        inkassoAmountTextView.text = String.format(Locale.GERMANY, "%.2f€", inkassoAmount)
        inkassoCountTextView.text = "$inkassoCount Adet"
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

        lifecycleScope.launch {
            currentWorkDay?.let {
                it.totalWorkTimeMs = 0
                db.workDayDao().update(it)
                refreshUmsatzList()
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
