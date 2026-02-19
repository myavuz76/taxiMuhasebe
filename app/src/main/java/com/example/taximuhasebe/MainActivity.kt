package com.example.taximuhasebe

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MainActivity : AppCompatActivity() {

    // --- Arayüz Elemanları ---
    private lateinit var timerTextView: TextView
    private lateinit var playPauseButton: ImageButton
    private lateinit var resetButton: ImageButton
    private lateinit var workDaySpinner: Spinner

    // --- Kronometre Değişkenleri ---
    private var isRunning = false
    private var startTime: Long = 0
    private var elapsedTime: Long = 0

    // --- Handler ve Runnable: Zamanlayıcı Mekanizması ---
    private val handler = Handler(Looper.getMainLooper())
    private val runnable = object : Runnable {
        override fun run() {
            if (isRunning) {
                val currentTime = SystemClock.uptimeMillis()
                elapsedTime = currentTime - startTime
                updateTimerText()
                handler.postDelayed(this, 1000)
            }
        }
    }

    /**
     * Bu fonksiyon, Activity (ekran) ilk oluşturulduğunda çağrılır.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // --- Arayüz Elemanlarını Koda Bağlama ---
        timerTextView = findViewById(R.id.timerTextView)
        playPauseButton = findViewById(R.id.playPauseButton)
        resetButton = findViewById(R.id.resetButton)
        workDaySpinner = findViewById(R.id.workDaySpinner)

        // --- Kurulum Fonksiyonlarını Çağırma ---
        setupWorkDaySpinner()

        // --- Butonlara Tıklama Olayları Atama ---
        playPauseButton.setOnClickListener {
            if (isRunning) {
                pauseTimer()
            } else {
                startTimer()
            }
        }

        resetButton.setOnClickListener {
            resetTimer()
        }
    }

    /**
     * "İŞ GÜNÜ" Spinner'ını (açılır liste) son 7 günün tarihleriyle doldurur ve metin rengini ayarlar.
     */
    private fun setupWorkDaySpinner() {
        val dateList = ArrayList<String>()
        val calendar = Calendar.getInstance()
        val sdf = SimpleDateFormat("dd.MM - EEE", Locale("de", "DE"))

        for (i in 0..6) {
            dateList.add(sdf.format(calendar.time))
            calendar.add(Calendar.DAY_OF_YEAR, -1)
        }

        // Spinner'a verileri bağlamak için bir ArrayAdapter oluştur.
        // Android'in hazır sunduğu simple_spinner_item yerine, kendi oluşturduğumuz custom_spinner_item'ı kullanıyoruz.
        val adapter = ArrayAdapter(this, R.layout.custom_spinner_item, dateList)
        // Açılır listenin görünüm stilini standart olarak ayarla (açılan liste siyah arka planlı ve beyaz yazılı olacak).
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        // Hazırladığımız adaptörü Spinner'a ata.
        workDaySpinner.adapter = adapter
    }

    /**
     * Kronometreyi başlatır ve Sıfırla butonunu görünür yapar.
     */
    private fun startTimer() {
        if (!isRunning) {
            startTime = SystemClock.uptimeMillis() - elapsedTime
            handler.post(runnable)
            isRunning = true
            playPauseButton.setImageResource(android.R.drawable.ic_media_pause)
            resetButton.visibility = View.VISIBLE
        }
    }

    /**
     * Kronometreyi duraklatır.
     */
    private fun pauseTimer() {
        if (isRunning) {
            handler.removeCallbacks(runnable)
            isRunning = false
            playPauseButton.setImageResource(android.R.drawable.ic_media_play)
        }
    }

    /**
     * Kronometreyi tamamen durdurur, süreyi sıfırlar ve Sıfırla butonunu gizler.
     */
    private fun resetTimer() {
        pauseTimer()
        elapsedTime = 0
        updateTimerText()
        resetButton.visibility = View.GONE
    }

    /**
     * Geçen süreyi (elapsedTime) alıp "saat:dakika:saniye" formatına çevirir
     * ve ekrandaki metni günceller.
     */
    private fun updateTimerText() {
        val seconds = (elapsedTime / 1000) % 60
        val minutes = (elapsedTime / (1000 * 60)) % 60
        val hours = (elapsedTime / (1000 * 60 * 60)) % 24

        val timeString = String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)
        timerTextView.text = timeString
    }
}