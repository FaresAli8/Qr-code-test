package com.smartqr.app

import android.app.Application
import androidx.room.Room
import com.smartqr.app.data.AppDatabase

class SmartQRApplication : Application() {
    lateinit var database: AppDatabase
        private set

    override fun onCreate() {
        super.onCreate()
        database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "smartqr-db"
        ).build()
    }
}