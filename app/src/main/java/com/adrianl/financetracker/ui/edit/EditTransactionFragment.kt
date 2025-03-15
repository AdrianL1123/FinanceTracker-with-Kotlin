package com.adrianl.financetracker.ui.edit

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.size
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.adrianl.financetracker.R
import com.adrianl.financetracker.data.model.Transaction
import com.adrianl.financetracker.ui.home.BaseManageTxFragment
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch
import nl.bryanderidder.themedtogglebuttongroup.ThemedButton

class EditTransactionFragment : BaseManageTxFragment() {
    private val viewModel: EditTransactionViewModel by viewModels { EditTransactionViewModel.Factory }
    private val args: EditTransactionFragmentArgs by navArgs()

    private val incomeCategories = listOf(
        "salary",
        "investment",
        "bonus",
        "gift",
        "pocket Money",
        "lottery"
    )

    private val expenseCategories = listOf(
        "food",
        "transport",
        "shopping",
        "health",
        "entertainment",
        "others"
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        setupObservers(view)
        loadTransaction()
    }

    private fun setupUI() {
        binding.tvAddorEdit.text = "Update"
        setupTransactionTypeToggle()
    }

    private fun setupObservers(view: View) {
        lifecycleScope.launch {
            viewModel.finish.collect {
                findNavController().popBackStack()
            }
        }

        lifecycleScope.launch {
            viewModel.transaction.collect { tx ->
                if (tx != null) {
                    populateTransactionData(tx, view)
                }
            }
        }
    }

    private fun loadTransaction() {
        viewModel.getTxById(args.transactionId)
    }

    private fun populateTransactionData(tx: Transaction, view: View) {
        binding.etAmount.setText(tx.amount.toString())
        binding.etDescription.setText(tx.description)
        if (tx.transactionType == "Income") {
            binding.btnIncome.isChecked = true
            updateCategoryText(incomeCategories)
        } else {
            binding.btnExpense.isChecked = true
            updateCategoryText(expenseCategories)
        }

        val categoryButtons = binding.categoryGroup.buttons
        val oriCategory = tx.category

        // preselect buttons
        for (button in categoryButtons) {
            if (button.text.replaceFirstChar(Char::titlecase) == oriCategory) {
                binding.categoryGroup.selectButton(button)
            }
        }
        setupSaveButton(tx, view)
    }

    private fun setupSaveButton(tx: Transaction, view: View) {
        binding.btnSave.setOnClickListener {
            val selectedButtons = binding.categoryGroup.selectedButtons
            val categoryList = mutableListOf<String>()
            for (category in selectedButtons) {
                categoryList.add(category.text)
            }
            // capitalize is deprecated took this from stackOverflow
            val categoryString = categoryList.joinToString(", ").replaceFirstChar(Char::titlecase)

            val updatedTx = Transaction(
                id = tx.id,
                amount = binding.etAmount.text.toString().toDoubleOrNull() ?: 0.0,
                description = binding.etDescription.text.toString(),
                category = categoryString,
                transactionType = if (binding.btnIncome.isChecked) "Income" else "Expenses",
                dateAdded = tx.dateAdded
            )

            viewModel.updateTx(updatedTx)
            Toast.makeText(context, "Transaction Updated", Toast.LENGTH_SHORT).show()

            val action = EditTransactionFragmentDirections
                .actionEditTransactionFragmentToHomeFragment()
            findNavController().navigate(action)
        }
    }

    private fun setupTransactionTypeToggle() {
        val toggleGroup = binding.toggleTransactionType
        val btnIncome = binding.btnIncome
        val btnExpense = binding.btnExpense

        // Set default styling
        setDefaultButtonStyle(btnIncome)
        setDefaultButtonStyle(btnExpense)

        // Set default categories
        updateCategoryText(incomeCategories)

        toggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                when (checkedId) {
                    R.id.btnIncome -> {
                        updateButtonStyles(btnIncome, btnExpense)
                        updateCategoryText(incomeCategories)
                    }

                    R.id.btnExpense -> {
                        updateButtonStyles(btnExpense, btnIncome)
                        updateCategoryText(expenseCategories)
                    }
                }
            }
        }
    }

    private fun updateButtonStyles(activeButton: MaterialButton, inactiveButton: MaterialButton) {
        // active button
        activeButton.setBackgroundColor(
            ContextCompat.getColor(requireContext(), R.color.darkYellow)
        )
        activeButton.setTextColor(
            ContextCompat.getColor(requireContext(), R.color.white)
        )

        // inactive button
        inactiveButton.setBackgroundColor(
            ContextCompat.getColor(requireContext(), R.color.white)
        )
        inactiveButton.setTextColor(
            ContextCompat.getColor(requireContext(), R.color.darkYellow)
        )
    }

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