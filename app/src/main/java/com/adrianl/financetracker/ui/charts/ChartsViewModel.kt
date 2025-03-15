package com.adrianl.financetracker.ui.charts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.adrianl.financetracker.App
import com.adrianl.financetracker.data.model.Transaction
import com.adrianl.financetracker.data.repo.TransactionRepo
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class ChartsViewModel(
    private val repo: TransactionRepo
) : ViewModel() {

    val state: StateFlow<State> = repo.getTransactions()
        .map { transactions -> State((transactions), transactions.isEmpty()) }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            State()
        )

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val repo = (this[APPLICATION_KEY] as App).repo
                ChartsViewModel(repo)
            }
        }
    }
}

data class State(
    val transactions: List<Transaction> = emptyList(),
    val isEmpty: Boolean = true
)