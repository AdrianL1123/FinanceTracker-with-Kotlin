package com.adrianl.financetracker.ui.adapter

import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.adrianl.financetracker.R
import com.adrianl.financetracker.data.model.BaseTransaction
import com.adrianl.financetracker.data.model.Transaction
import com.adrianl.financetracker.data.model.TransactionHeader
import com.adrianl.financetracker.databinding.ItemLayoutDateBinding
import com.adrianl.financetracker.databinding.ItemLayoutTransactionsBinding

class TransactionAdapter(
    private var transactions: List<BaseTransaction>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var listener: Listener? = null

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        if (viewType == viewTypeItem) {
            return TransactionViewHolder(
                ItemLayoutTransactionsBinding.inflate(inflater, parent, false)
            )
        }
        return TransactionHeaderViewHolder(
            ItemLayoutDateBinding.inflate(inflater, parent, false)
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val tx = transactions[position]
        if (holder is TransactionViewHolder) {
            holder.bind(tx as Transaction)
        }
        if (holder is TransactionHeaderViewHolder) {
            holder.bind(tx as TransactionHeader)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (transactions[position]) {
            is TransactionHeader -> viewTypeHeader
            else -> viewTypeItem
        }
    }

    override fun getItemCount() = transactions.size

    fun update(transactions: List<BaseTransaction>) {
        this.transactions = transactions
        notifyDataSetChanged()
    }

    inner class TransactionViewHolder(
        private val binding: ItemLayoutTransactionsBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(transaction: Transaction) {
            binding.run {
                tvCategory.text = transaction.category

                tvAmount.text = when (transaction.transactionType) {
                    "Expenses" -> "-${transaction.amount}"
                    else -> transaction.amount.toString()
                }

                val (categoryIconResId, bgColorResId) = when (transaction.category) {
                    "Salary" -> Pair(R.drawable.salary_icon, R.color.darkBlue)
                    "Investment" -> Pair(R.drawable.investment_icon, R.color.teal_200)
                    "Bonus" -> Pair(R.drawable.bonus_icon, R.color.teal_700)
                    "Gift" -> Pair(R.drawable.gift_icon, R.color.olive)
                    "Pocket Money" -> Pair(R.drawable.pocket_money_icon, R.color.crimson)
                    "Food" -> Pair(R.drawable.food_icon, R.color.pink)
                    "Transport" -> Pair(R.drawable.transportation_icon, R.color.orange)
                    "Shopping" -> Pair(R.drawable.shopping_icon, R.color.purple)
                    "Health" -> Pair(R.drawable.health_icon, R.color.skyblue)
                    "Entertainment" -> Pair(R.drawable.entertainment_icon, R.color.green)
                    "Others" -> Pair(R.drawable.other_icon, R.color.others)
                    else -> Pair(R.drawable.default_icon, R.color.others)
                }

                // Get the color
                val bgColor = ContextCompat.getColor(tvCategoryIcon.context, bgColorResId)

                val circleDrawable = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setColor(bgColor) // Dynamically set the color
                }
                tvCategoryIcon.background = circleDrawable

                // Set the category icon
                tvCategoryIcon.setImageResource(categoryIconResId)
            }
            binding.llTransactions.setOnClickListener {
                listener?.onClickItem(transaction)
            }
        }
    }

    inner class TransactionHeaderViewHolder(
        private val binding: ItemLayoutDateBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(transactionHeader: TransactionHeader) {
            binding.tvDate.text =
                transactionHeader.dateAdded.toString().split(" ").take(3).joinToString(" ")
        }
    }


    companion object {
        const val viewTypeHeader = 100
        const val viewTypeItem = 101
    }

    interface Listener {
        fun onClickItem(item: Transaction)
    }
}