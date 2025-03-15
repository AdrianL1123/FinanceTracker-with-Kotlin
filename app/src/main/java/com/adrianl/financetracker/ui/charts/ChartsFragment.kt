package com.adrianl.financetracker.ui.charts

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.adrianl.financetracker.databinding.FragmentChartsBinding
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.MPPointF
import com.github.mikephil.charting.formatter.PercentFormatter
import android.graphics.Typeface
import androidx.core.content.ContextCompat
import com.adrianl.financetracker.R
import com.adrianl.financetracker.data.model.Transaction
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import kotlinx.coroutines.launch

class ChartsFragment : Fragment() {
    private lateinit var binding: FragmentChartsBinding
    private lateinit var pieChart: PieChart
    private lateinit var barChart: BarChart
    private val viewModel: ChartsViewModel by viewModels { ChartsViewModel.Factory }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentChartsBinding.inflate(inflater, container, false)
        pieChart = binding.pieChart
        barChart = binding.barChart
        val chartTypeGroup = binding.chartTypeGroup

        chartTypeGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.radioPieChart -> {
                    pieChart.visibility = View.VISIBLE
                    barChart.visibility = View.GONE
                    binding.tvHead.text = "All Time Expenses"
                    pieChart.animateY(500)
                }

                R.id.radioBarChart -> {
                    pieChart.visibility = View.GONE
                    barChart.visibility = View.VISIBLE
                    binding.tvHead.text = "Overall Daily Expenditures"
                    barChart.animateY(500)
                }
            }
        }

        lifecycleScope.launch {
            viewModel.state.collect { state ->
                updatePieChart(state.transactions)
                updateBarChart(state.transactions)
            }
        }

        return binding.root
    }

    // pie chart
    private fun updatePieChart(transactions: List<Transaction>) {
        val expenseTransactions = transactions.filter { it.transactionType == "Expenses" }

        if (expenseTransactions.isEmpty()) {
            pieChart.visibility = View.GONE
            return
        }

        pieChart.visibility = View.VISIBLE

        pieChart.animateY(500)

        // groupby category to total up the expenses
        val categoryTotals = expenseTransactions.groupBy { it.category }
            .mapValues { (_, expenseTransactions) ->
                expenseTransactions.sumOf { it.amount }
            }

        val entries: ArrayList<PieEntry> = ArrayList()
        categoryTotals.forEach { (category, total) ->
            entries.add(PieEntry(total.toFloat(), category))
        }

        val dataSet = PieDataSet(entries, null)
        dataSet.setDrawIcons(false)
        dataSet.sliceSpace = 3f
        dataSet.iconsOffset = MPPointF(0f, 40f)
        dataSet.selectionShift = 5f

        // Set colors based on category
        val colors = ArrayList<Int>()
        categoryTotals.keys.forEach { category ->
            colors.add(getColorForCategory(category))
        }
        dataSet.colors = colors

        val data = PieData(dataSet)
        data.setValueFormatter(PercentFormatter())
        data.setValueTextSize(15f)
        data.setValueTypeface(Typeface.DEFAULT_BOLD)
        data.setValueTextColor(Color.WHITE)

        pieChart.data = data

        // Set center text in pieChart
        pieChart.setDrawCenterText(true)
        val expenses = transactions.filter { it.transactionType == "Expenses" }
            .sumOf { it.amount }
        pieChart.centerText = "$expenses"
        pieChart.setCenterTextSize(16f)
        pieChart.setCenterTextColor(Color.BLACK)


        // customize legend manually
        pieChart.legend.apply {
            verticalAlignment =
                com.github.mikephil.charting.components.Legend.LegendVerticalAlignment.BOTTOM
            horizontalAlignment =
                com.github.mikephil.charting.components.Legend.LegendHorizontalAlignment.CENTER
            orientation =
                com.github.mikephil.charting.components.Legend.LegendOrientation.HORIZONTAL
            textSize = 15f
            textColor = Color.WHITE
            form =
                com.github.mikephil.charting.components.Legend.LegendForm.CIRCLE
            formSize = 10f
        }
        pieChart.description.isEnabled = false
        pieChart.highlightValues(null)
        pieChart.invalidate()
    }

    private fun updateBarChart(transactions: List<Transaction>) {
        val expenseTransactions = transactions.filter { it.transactionType == "Expenses" }

        if (expenseTransactions.isEmpty()) {
            barChart.visibility = View.GONE
            return
        }

        barChart.visibility = View.VISIBLE

        // Group transactions by formatted date and sum the amounts for each date
        val dateTotals = mutableMapOf<String, Double>()

        expenseTransactions.forEach { transaction ->
            val formattedDate =
                transaction.dateAdded.toString().split(" ").drop(1).take(2).joinToString(" ")

            // Add the transaction amount to the corresponding date entry
            dateTotals[formattedDate] =
                    // (self note) getOrDefault returns a value corresponding to the given key or a defaultValue
                dateTotals.getOrDefault(formattedDate, 0.0) + transaction.amount
        }

        // Get the list of labels (dates) for the X-axis
        val dateLabels = dateTotals.keys.toList()


        // Create BarEntries with index-based X values
        val entries = dateTotals.entries.mapIndexed { index, (date, total) ->
            BarEntry(index.toFloat(), total.toFloat()) // X-axis uses index
        }

        // colors for the bars
        val colorsList = listOf(
            ContextCompat.getColor(requireContext(), R.color.pink),
            ContextCompat.getColor(requireContext(), R.color.orange),
            ContextCompat.getColor(requireContext(), R.color.purple),
            ContextCompat.getColor(requireContext(), R.color.darkBlue),
            ContextCompat.getColor(requireContext(), R.color.darkYellow),
            ContextCompat.getColor(requireContext(), R.color.skyblue),
            ContextCompat.getColor(requireContext(), R.color.green),
            ContextCompat.getColor(requireContext(), R.color.others)
        )

        val barDataSet = BarDataSet(entries, "Daily Expenses").apply {
            valueTextSize = 12f
            valueTextColor = Color.WHITE
            // runs for each index from 0 to entries.size - 1 and assigns a color to each index
            // Uses the modulus operator (%) to cycle through the colors when entries.size is larger than colorsList.size
            /*
            Example:
            index 0 -> "Red"    (0 % 3 = 0)
            index 1 -> "Blue"   (1 % 3 = 1)
            index 2 -> "Green"  (2 % 3 = 2)
            index 3 -> "Red"    (3 % 3 = 0)
            index 4 -> "Blue"   (4 % 3 = 1)
             */
            colors = List(entries.size) { index -> colorsList[index % colorsList.size] }
        }

        val barData = BarData(barDataSet).apply {
            barWidth = 0.6f
        }

        barChart.apply {
            data = barData
            description.isEnabled = false

            // Set X-axis properties
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                granularity = 1f
                labelRotationAngle = -45f
                textColor = Color.WHITE
                valueFormatter = IndexAxisValueFormatter(dateLabels)
            }

            // Y-Axis Styling
            axisLeft.apply {
                axisMinimum = 0f
                textColor = Color.WHITE
                gridColor = Color.WHITE
                axisLineColor = Color.WHITE
            }
            legend.isEnabled = false
            axisRight.isEnabled = false
            invalidate()
        }
    }

    private fun getColorForCategory(category: String): Int {
        return when (category) {
            "Food" -> ContextCompat.getColor(requireContext(), R.color.pink)
            "Transport" -> ContextCompat.getColor(requireContext(), R.color.orange)
            "Shopping" -> ContextCompat.getColor(requireContext(), R.color.purple)
            "Salary" -> ContextCompat.getColor(requireContext(), R.color.darkBlue)
            "Bill" -> ContextCompat.getColor(requireContext(), R.color.darkYellow)
            "Health" -> ContextCompat.getColor(requireContext(), R.color.skyblue)
            "Entertainment" -> ContextCompat.getColor(requireContext(), R.color.green)
            "Others" -> ContextCompat.getColor(requireContext(), R.color.others)
            else -> ContextCompat.getColor(requireContext(), R.color.others)
        }
    }

}
