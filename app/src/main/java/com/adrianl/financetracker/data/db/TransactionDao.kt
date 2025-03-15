package com.adrianl.financetracker.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.adrianl.financetracker.data.model.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    // Get all transactions
    @Query("SELECT * FROM `transaction`")
    fun getTransactions(): Flow<List<Transaction>>

    // Get Tx with query
    @Query("SELECT * FROM `transaction` WHERE category LIKE :query ORDER BY category ASC")
    fun getTransactionWithQuery(query: String): Flow<List<Transaction>>

    // Insert a new transaction
    @Insert
    fun addTransaction(transaction: Transaction)

    // Get a transaction by ID
    @Query("SELECT * FROM `transaction` WHERE id = :id")
    fun getTransactionById(id: Int): Transaction?

    // Update an existing transaction
    @Update
    fun update(transaction: Transaction)

    // Delete a transaction by ID
    @Query("DELETE FROM `transaction` WHERE id = :id")
    fun delete(id: Int)
}