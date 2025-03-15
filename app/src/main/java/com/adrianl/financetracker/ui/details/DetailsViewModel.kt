package com.adrianl.financetracker.ui.details

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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DetailsViewModel(private val repo: TransactionRepo) : ViewModel() {
    private val _transaction = MutableStateFlow<Transaction?>(null)
    val transaction: StateFlow<Transaction?> = _transaction

    fun getTxById(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val fetchTx = repo.getTransactionById(id)
            _transaction.value = fetchTx
        }
    }

    fun deleteTxById(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repo.deleteTx(id)
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val repo = (this[APPLICATION_KEY] as App).repo
                DetailsViewModel(repo)
            }
        }
    }
}