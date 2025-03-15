package com.adrianl.financetracker.ui.home

import android.annotation.SuppressLint
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageView
import android.widget.SearchView
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.adrianl.financetracker.MainActivity
import com.adrianl.financetracker.R
import com.adrianl.financetracker.data.model.Transaction
import com.adrianl.financetracker.databinding.FragmentHomeBinding
import com.adrianl.financetracker.ui.adapter.TransactionAdapter
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch
import nl.bryanderidder.themedtogglebuttongroup.ThemedToggleButtonGroup
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar

class HomeFragment : Fragment() {
    private val viewModel: HomeViewModel by viewModels { HomeViewModel.Factory }
    private lateinit var binding: FragmentHomeBinding
    private lateinit var adapter: TransactionAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as MainActivity).showBtmNav()
        setupAdapter()

        lifecycleScope.launch {
            viewModel.state.collect { state ->
                if (state.isEmpty) {
                    binding.tvIfEmpty.visibility = View.VISIBLE
                    binding.rvExpensesAndIncome.visibility = View.GONE
                } else {
                    binding.tvIfEmpty.visibility = View.GONE
                    binding.rvExpensesAndIncome.visibility = View.VISIBLE
                    adapter.update(state.transactions)
                }
            }
        }

        lifecycleScope.launch {
            viewModel.rawTransactions.collect { transactions ->
                val totalIncome = transactions.filter { it.transactionType == "Income" }
                    .sumOf { it.amount }
                val totalExpenses = transactions.filter { it.transactionType == "Expenses" }
                    .sumOf { it.amount }
                val totalBalance = totalIncome - totalExpenses

                val calendar = Calendar.getInstance()
                val currentYear = calendar.get(Calendar.YEAR)
                val currentMonth =
                    LocalDate.now().format(DateTimeFormatter.ofPattern("MMM")).uppercase()

                binding.tvIncome.text = "$$totalIncome"
                binding.tvExpenses.text = "$$totalExpenses"
                binding.tvBalance.text = "$$totalBalance"
                binding.tvYear.text = currentYear.toString()
                binding.tvMonth.text = currentMonth
            }
        }


        // Update UI when income, expenses, and balance is filtered
        lifecycleScope.launch {
            viewModel.filteredIncome.collect { income ->
                binding.tvIncome.text = "$$income"
            }
        }

        lifecycleScope.launch {
            viewModel.filteredExpenses.collect { expenses ->
                binding.tvExpenses.text = "$$expenses"
            }
        }

        lifecycleScope.launch {
            viewModel.filteredBalance.collect { balance ->
                binding.tvBalance.text = "$$balance"
            }
        }

        binding.tvMonth.setOnClickListener {
            showYearMonthDialog()
        }
        binding.ivMonth.setOnClickListener {
            showYearMonthDialog()
        }

        // Search UI
        val header = binding.tvHeader
        val searchView = binding.searchView
        searchView.setOnQueryTextFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                header.visibility = View.GONE
            } else {
                header.visibility = View.VISIBLE
            }
        }
        binding.searchView.setOnQueryTextListener(
            object : SearchView.OnQueryTextListener,
                androidx.appcompat.widget.SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String): Boolean {
                    return true
                }

                override fun onQueryTextChange(query: String): Boolean {
                    viewModel.setQuery(query)
                    return true
                }
            },
        )
    }

    private fun setupAdapter() {
        adapter = TransactionAdapter(emptyList())
        binding.rvExpensesAndIncome.adapter = adapter
        binding.rvExpensesAndIncome.layoutManager = LinearLayoutManager(requireContext())
        adapter.listener = object : TransactionAdapter.Listener {
            override fun onClickItem(item: Transaction) {
                val action = HomeFragmentDirections.actionHomeFragmentToDetailsFragment(item.id!!)
                findNavController().navigate(action)
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun showYearMonthDialog() {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.month_year_picker_dialog)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val btnMinus = dialog.findViewById<ImageView>(R.id.btnMinus)
        val btnPlus = dialog.findViewById<ImageView>(R.id.btnPlus)
        val tvCurrentYear = dialog.findViewById<TextView>(R.id.tvCurrentYear)
        val btnCancel = dialog.findViewById<MaterialButton>(R.id.btnCancel)
        val btnConfirm = dialog.findViewById<MaterialButton>(R.id.btnConfirm)
        val btnShowAll = dialog.findViewById<MaterialButton>(R.id.btnShowAll)
        val monthlyButtons = dialog.findViewById<ThemedToggleButtonGroup>(R.id.monthGroup)
        val currentMonth =
            LocalDate.now().format(DateTimeFormatter.ofPattern("MMM")).uppercase()

        val calendar = Calendar.getInstance()
        var currentYear = calendar.get(Calendar.YEAR)

        tvCurrentYear.text = currentYear.toString()

        // Preselect current month
        for (button in monthlyButtons.buttons) {
            if (button.text == currentMonth) {
                monthlyButtons.selectButton(button)
            }
        }

        btnMinus.setOnClickListener {
            currentYear--
            tvCurrentYear.text = currentYear.toString()
        }

        btnPlus.setOnClickListener {
            currentYear++
            tvCurrentYear.text = currentYear.toString()
        }

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        btnConfirm.setOnClickListener {
            val selectedMonth = monthlyButtons.selectedButtons
            val selectedMonthText = selectedMonth.joinToString("") { it.text }

            binding.tvMonth.text = selectedMonthText
            binding.tvYear.text = tvCurrentYear.text

            val selectedYear = currentYear
            viewModel.filterSelectedMonth(selectedMonthText, selectedYear)
            dialog.dismiss()
        }

        btnShowAll.setOnClickListener {
            binding.tvMonth.text = currentMonth
            binding.tvYear.text = currentYear.toString()
            viewModel.clearFilters()
            dialog.dismiss()
        }

        dialog.show()
    }
}


