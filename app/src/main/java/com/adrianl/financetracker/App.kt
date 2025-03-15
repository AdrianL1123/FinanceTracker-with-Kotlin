package com.adrianl.financetracker

import android.app.Application
import androidx.room.Room
import com.adrianl.financetracker.data.db.AppDataBase
import com.adrianl.financetracker.data.db.TransactionDao
import com.adrianl.financetracker.data.repo.TransactionRepo

class App : Application() {
    lateinit var repo: TransactionRepo

    override fun onCreate() {
        super.onCreate()
        val db = Room.databaseBuilder(
            this,
            AppDataBase::class.java,
            AppDataBase.NAME
        )
            .fallbackToDestructiveMigration()
            .build()

        val transactionDao: TransactionDao = db.transactionDao()
        repo = TransactionRepo(transactionDao)
    }
}