package com.example.taximuhasebe.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [WorkDay::class], version = 2, exportSchema = false) // Versiyonu 2'ye yükselttik
abstract class AppDatabase : RoomDatabase() {

    abstract fun workDayDao(): WorkDayDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "taxi_database"
                )
                // Veritabanı şeması değiştiğinde (versiyon yükseldiğinde), eski veritabanını
                // silip yeniden oluştur. Bu, geliştirme aşamasında çökmemizi engeller.
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
