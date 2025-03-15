package com.adrianl.financetracker.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Entity("transaction")
data class Transaction(
    @PrimaryKey(autoGenerate = true)
    val id: Int? = null,
    val amount: Double,
    var category: String,
    val description: String,
    val transactionType: String,
    override val dateAdded: Date
) : BaseTransaction()

abstract class BaseTransaction {
    abstract val dateAdded: Date
}

data class TransactionHeader(
    override val dateAdded: Date
) : BaseTransaction()

