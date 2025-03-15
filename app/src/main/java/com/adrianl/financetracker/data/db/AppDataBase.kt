package com.adrianl.financetracker.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.adrianl.financetracker.data.Converters
import com.adrianl.financetracker.data.model.Transaction

@Database(entities = [Transaction::class], version = 2)
@TypeConverters(Converters::class)
abstract class AppDataBase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao

    companion object {
        const val NAME = "app_database"
    }
}