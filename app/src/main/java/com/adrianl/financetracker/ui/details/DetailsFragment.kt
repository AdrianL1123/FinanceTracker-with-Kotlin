package com.adrianl.financetracker.ui.details

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.adrianl.financetracker.MainActivity
import com.adrianl.financetracker.R
import com.adrianl.financetracker.databinding.FragmentDetailsBinding
import kotlinx.coroutines.launch

class DetailsFragment : Fragment() {
    private lateinit var binding: FragmentDetailsBinding
    private val args: DetailsFragmentArgs by navArgs()
    private val viewModel: DetailsViewModel by viewModels {
        DetailsViewModel.Factory
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as MainActivity).hideBtmNav()

        val txId = args.transactionId
        viewModel.getTxById(txId)

        lifecycleScope.launch {
            viewModel.transaction.collect { tx ->
                val (categoryIconResId, bgColorResId) = when (tx?.category) {
                    "Food" -> Pair(R.drawable.food_icon, R.color.pink)
                    "Transport" -> Pair(R.drawable.transportation_icon, R.color.orange)
                    "Shopping" -> Pair(R.drawable.shopping_icon, R.color.purple)
                    "Salary" -> Pair(R.drawable.salary_icon, R.color.darkBlue)
                    "Bill" -> Pair(R.drawable.money_icon, R.color.darkYellow)
                    "Health" -> Pair(R.drawable.health_icon, R.color.skyblue)
                    "Entertainment" -> Pair(R.drawable.entertainment_icon, R.color.green)
                    "Others" -> Pair(R.drawable.other_icon, R.color.others)
                    else -> Pair(R.drawable.default_icon, R.color.others)
                }

                // Get the color
                val bgColor = ContextCompat.getColor(binding.ivCategoryIcon.context, bgColorResId)

                val circleDrawable = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setColor(bgColor) // Dynamically set the color
                }
                binding.ivCategoryIcon.background = circleDrawable

                // Set the category icon
                binding.ivCategoryIcon.setImageResource(categoryIconResId)
                binding.tvCategory.text = tx?.category
                binding.tvTransactionType.text = tx?.transactionType
                binding.tvAmount.text = tx?.amount.toString()
                val getDate = tx?.dateAdded.toString().split(" ").drop(1).take(2).joinToString(" ")
                val getYear = tx?.dateAdded.toString().split(" ").takeLast(1).joinToString(" ")
                binding.tvDate.text =
                    "$getDate, $getYear"
                if (tx?.description?.isEmpty() == true) {
                    binding.layoutDescription.visibility = View.GONE
                } else {
                    binding.layoutDescription.visibility = View.VISIBLE
                    binding.tvDesc.text = tx?.description
                }
            }
        }
        binding.btnBack.setOnClickListener {
            val action = DetailsFragmentDirections.actionDetailsFragmentToHomeFragment()
            findNavController().navigate(action)
        }
        binding.btnDelete.setOnClickListener {
            showDeleteDialogBox(txId)
        }
        binding.btnEdit.setOnClickListener {
            val action =
                DetailsFragmentDirections.actionDetailsFragmentToEditTransactionFragment(txId)
            findNavController().navigate(action)
        }
    }

    // delete dialog
    private fun showDeleteDialogBox(transactionId: Int) {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.delete_alert_dialog)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val btnCancel = dialog.findViewById<Button>(R.id.btnCancel)
        val btnDelete = dialog.findViewById<Button>(R.id.btnDelete)

        btnCancel.setOnClickListener { dialog.dismiss() }

        btnDelete.setOnClickListener {
            viewModel.deleteTxById(transactionId)
            Toast.makeText(requireContext(), "Transaction Removed.", Toast.LENGTH_SHORT).show()
            val action = DetailsFragmentDirections.actionDetailsFragmentToHomeFragment()
            findNavController().navigate(action)
            dialog.dismiss()
        }
        dialog.show()
    }
}
