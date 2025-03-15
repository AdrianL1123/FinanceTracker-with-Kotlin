package com.adrianl.financetracker.ui.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.adrianl.financetracker.App
import com.adrianl.financetracker.data.model.Transaction
import com.adrianl.financetracker.data.repo.TransactionRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import java.util.Date

class AddTransactionViewModel(
    private val repo: TransactionRepo
) : ViewModel() {
    val finish = MutableSharedFlow<Unit>()

    fun addTx(
        amount: Double,
        category: String,
        description: String,
        transactionType: String,
    ) {
        val tx = Transaction(
            amount = amount,
            category = category,
            description = description,
            transactionType = transactionType,
            dateAdded = Date()
        )
        viewModelScope.launch(Dispatchers.IO) {
            repo.addTransaction(tx)
            finish.emit(Unit)
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val repo = (this[APPLICATION_KEY] as App).repo
                AddTransactionViewModel(repo)
            }
        }
    }
}