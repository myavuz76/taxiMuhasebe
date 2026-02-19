package com.example.taximuhasebe.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

/**
 * DAO (Data Access Object)
 * Bu arayüz, veritabanı üzerinde hangi işlemlerin (ekleme, silme, güncelleme, sorgulama)
 * yapılabileceğini tanımlar. Room, bu arayüzü kullanarak gerekli tüm kodları
 * arka planda otomatik olarak oluşturur.
 */
@Dao
interface WorkDayDao {

    /**
     * Veritabanına yeni bir WorkDay (iş günü) kaydı ekler.
     * Eğer aynı ID'ye sahip bir kayıt zaten varsa, üzerine yazar.
     */
    @Insert(onConflict = androidx.room.OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(workDay: WorkDay): Long

    /**
     * Belirli bir tarihe ait WorkDay kaydını getirir.
     * @param date Sorgulanacak tarih ("dd.MM.yyyy" formatında).
     * @return O tarihe ait bir WorkDay nesnesi veya bulunamazsa null.
     */
    @Query("SELECT * FROM work_days WHERE date = :date LIMIT 1")
    suspend fun getWorkDayByDate(date: String): WorkDay?

    /**
     * Veritabanındaki tüm WorkDay kayıtlarını tarihe göre tersten sıralayarak (en yeniden en eskiye)
     * bir liste halinde getirir.
     */
    @Query("SELECT * FROM work_days ORDER BY date DESC")
    suspend fun getAllWorkDays(): List<WorkDay>

    /**
     * Mevcut bir WorkDay kaydını günceller.
     */
    @Update
    suspend fun update(workDay: WorkDay)

}
