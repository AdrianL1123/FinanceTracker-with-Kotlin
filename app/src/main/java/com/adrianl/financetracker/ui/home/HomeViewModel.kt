package com.adrianl.financetracker.ui.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.adrianl.financetracker.App
import com.adrianl.financetracker.data.model.BaseTransaction
import com.adrianl.financetracker.data.model.Transaction
import com.adrianl.financetracker.data.model.TransactionHeader
import com.adrianl.financetracker.data.repo.TransactionRepo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class HomeViewModel(
    private val repo: TransactionRepo
) : ViewModel() {
    private val _query = MutableStateFlow("")
    private val _filteredTransactions = MutableStateFlow<List<Transaction>>(emptyList())

    // updates the card ui
    private val _filteredIncome = MutableStateFlow(0.0)
    val filteredIncome: StateFlow<Double> = _filteredIncome
    private val _filteredExpenses = MutableStateFlow(0.0)
    val filteredExpenses: StateFlow<Double> = _filteredExpenses
    private val _filteredBalance = MutableStateFlow(0.0)
    val filteredBalance: StateFlow<Double> = _filteredBalance

    val rawTransactions: StateFlow<List<Transaction>> = repo.getTransactions().stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    private val _transactionsFlow: StateFlow<List<Transaction>> = combine(
        _query, rawTransactions, _filteredTransactions
    ) { query, allTransactions, filteredTransactions ->
        // determine which list to use first either filtered or not
        val baseList = if (filteredTransactions.isNotEmpty()) {
            filteredTransactions
        } else {
            allTransactions
        }
        // Then apply search on the selected list
        val searchedList = if (query.isBlank()) {
            baseList
        } else {
            baseList.filter { it.matchesQuery(query) }
        }
        searchedList.sortedByDescending { it.dateAdded }
    }.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(3000), emptyList()
    )

    val state: StateFlow<State> =
        combine(_transactionsFlow, _filteredTransactions) { transactions, _ ->
            State(
                transactions = prepareTransaction(transactions),
                isEmpty = transactions.isEmpty(),
            )
        }.stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), State()
        )


    fun setQuery(query: String) {
        _query.update { query }
    }


    // Prepare transactions by validating them by date and adding headers that contains dates
    fun prepareTransaction(list: List<Transaction>): List<BaseTransaction> {
        val trs: MutableList<BaseTransaction> = mutableListOf()
        var lastDate: String? = null
        list.sortedByDescending { it.dateAdded }.forEach { transaction ->
            val currentDate = transaction.dateAdded.toString().split(" ").take(3).joinToString(" ")
            if (currentDate != lastDate) {
                trs.add(TransactionHeader(transaction.dateAdded))
                lastDate = currentDate
            }
            trs.add(transaction)
        }
        return trs
    }

    fun filterSelectedMonth(selectedMonth: String, selectedYear: Int) {

        viewModelScope.launch {
            val monthFormatter = DateTimeFormatter.ofPattern("MMM")
            val txs = rawTransactions.value
            val filteredTx = txs.filter { tx ->
                val txDate = LocalDate.parse(
                    tx.dateAdded.toString(),
                    DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss zzz yyyy")
                )
                val txMonth = txDate.format(monthFormatter).uppercase()
                val txYear = txDate.year
                txMonth == selectedMonth.uppercase() && txYear == selectedYear
            }
            _filteredTransactions.value = filteredTx
            val income = filteredTx.filter { it.transactionType == "Income" }.sumOf { it.amount }
            val expenses =
                filteredTx.filter { it.transactionType == "Expenses" }.sumOf { it.amount }

            _filteredIncome.value = income
            _filteredExpenses.value = expenses
            _filteredBalance.value = income - expenses
        }
    }


    fun clearFilters() {
        viewModelScope.launch {
            // Reset to empty list so that it returns all transactions
            _filteredTransactions.value = emptyList()

            // Reset to total values
            val txs = rawTransactions.value
            val totalIncome = txs.filter { it.transactionType == "Income" }.sumOf { it.amount }
            val totalExpenses = txs.filter { it.transactionType == "Expenses" }.sumOf { it.amount }
            val totalBalance = totalIncome - totalExpenses

            _filteredIncome.value = totalIncome
            _filteredExpenses.value = totalExpenses
            _filteredBalance.value = totalBalance
        }
    }


    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val repo = (this[APPLICATION_KEY] as App).repo
                HomeViewModel(repo)
            }
        }
    }
}

data class State(
    val transactions: List<BaseTransaction> = emptyList(), val isEmpty: Boolean = true
)

// search function that checks if a transaction matches a search query by category
fun Transaction.matchesQuery(query: String): Boolean {
    return (this.category.contains(query, ignoreCase = true))
}