package com.adrianl.financetracker.data.repo

import androidx.room.Query
import com.adrianl.financetracker.data.db.TransactionDao
import com.adrianl.financetracker.data.model.Transaction
import kotlinx.coroutines.flow.Flow


class TransactionRepo(
    private val dao: TransactionDao
) {
    fun getTransactions(): Flow<List<Transaction>> {
        return dao.getTransactions()
    }

    fun getTransactionByQuery(query: String = ""): Flow<List<Transaction>> {
        return dao.getTransactionWithQuery("$query%")
    }

    fun addTransaction(transaction: Transaction) {
        return dao.addTransaction(transaction)
    }

    fun getTransactionById(id: Int): Transaction? {
        return dao.getTransactionById(id)
    }

    fun updateTx(transaction: Transaction) {
        return dao.update(transaction)
    }

    fun deleteTx(id: Int) {
        return dao.delete(id)
    }
}