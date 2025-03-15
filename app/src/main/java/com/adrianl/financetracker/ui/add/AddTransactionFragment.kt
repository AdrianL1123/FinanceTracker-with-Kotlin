package com.adrianl.financetracker.ui.add

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.size
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.adrianl.financetracker.R
import com.adrianl.financetracker.ui.home.BaseManageTxFragment
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch
import nl.bryanderidder.themedtogglebuttongroup.ThemedButton

class AddTransactionFragment : BaseManageTxFragment() {
    private val viewModel: AddTransactionViewModel by viewModels { AddTransactionViewModel.Factory }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupTxType()

        binding.tvAddorEdit.text = "Add"

        binding.btnSave.setOnClickListener {
            val categories = binding.categoryGroup.selectedButtons
            val categoryList = mutableListOf<String>()
            for (category in categories) {
                categoryList.add(category.text)
            }
            // capitalize is deprecated took this from stackOverflow
            val categoryString = categoryList.joinToString(", ").replaceFirstChar(Char::titlecase)
            val transactionType =
                if (binding.btnExpense.isChecked) {
                    "Expenses"
                } else {
                    "Income"
                }
            val amountText = binding.etAmount.text.toString()
            val description = binding.etDescription.text.toString()
            val category = categoryString
            if (amountText.isBlank() || category.isBlank()) {
                Toast.makeText(
                    requireContext(),
                    "Please fill in all the details.",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            val amount: Double = try {
                amountText.toDouble()
            } catch (e: NumberFormatException) {
                Toast.makeText(requireContext(), "Invalid amount entered.", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }
            viewModel.addTx(amount, category, description, transactionType)
        }
        lifecycleScope.launch {
            viewModel.finish.collect {
                val action =
                    AddTransactionFragmentDirections.actionAddExpensesFragmentToHomeFragment()
                findNavController().navigate(action)
                Toast.makeText(context, "Transaction Added.", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun setupTxType() {
        val toggleGroup = binding.toggleTransactionType
        val btnIncome = binding.btnIncome
        val btnExpense = binding.btnExpense

        // Set btnIncome as checked by default
        toggleGroup.check(btnIncome.id)

        // Set style for the checked button (Income)
        btnIncome.setBackgroundColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.darkYellow
            )
        )
        btnIncome.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.white
            )
        )
        // make sure expenses style is default
        setDefaultButtonStyle(btnExpense)

        val incomeCategories = listOf(
            "salary",
            "investment",
            "bonus",
            "gift",
            "pocket Money",
            "others"
        )
        val expenseCategories =
            listOf("food", "transport", "shopping", "health", "entertainment", "others")

        // change category based on tx type
        updateCategoryText(incomeCategories)

        // _ buttonToggleGroup (self note)
        toggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                when (checkedId) {
                    R.id.btnIncome -> {
                        // Highlight Income button
                        btnIncome.setBackgroundColor(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.darkYellow
                            )
                        )
                        btnIncome.setTextColor(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.white
                            )
                        )

                        // Reset Expense button
                        btnExpense.setBackgroundColor(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.white
                            )
                        )
                        btnExpense.setTextColor(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.darkYellow
                            )
                        )

                        // Update category text for income
                        updateCategoryText(incomeCategories)
                    }

                    R.id.btnExpense -> {
                        // Highlight Expense button
                        btnExpense.setBackgroundColor(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.darkYellow
                            )
                        )
                        btnExpense.setTextColor(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.white
                            )
                        )

                        // Reset Income button
                        btnIncome.setBackgroundColor(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.white
                            )
                        )
                        btnIncome.setTextColor(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.darkYellow
                            )
                        )

                        // Update category for expense
                        updateCategoryText(expenseCategories)
                    }
                }
            }
        }
    }

    // set default style
    private fun setDefaultButtonStyle(button: MaterialButton) {
        button.setTextColor(ContextCompat.getColor(requireContext(), R.color.darkYellow))
        button.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
    }

    private fun updateCategoryText(categories: List<String>) {
        val categoryGroup = binding.categoryGroup
        // Update the text dynamically for the first N buttons based on categories
        for (i in 0 until categoryGroup.size) {
            Log.d("debugging", i.toString())
            // getChildAt is a viewGroup method which returns the view at the specified position in the group which is (i)
            val button = categoryGroup.getChildAt(i) as? ThemedButton
            if (i < categories.size) {
                // after using getChildAt we can freely customize its props
                button?.text = categories[i] // Update text to match the category
            }
        }
    }
}