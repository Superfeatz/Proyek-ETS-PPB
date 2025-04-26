package com.example.mymoneynotes.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mymoneynotes.R
import com.example.mymoneynotes.model.Transaction
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TransactionAdapter : ListAdapter<Transaction, TransactionAdapter.TransactionViewHolder>(DIFF_CALLBACK) {

    private var onItemClickListener: ((Transaction) -> Unit)? = null

    fun setOnItemClickListener(listener: (Transaction) -> Unit) {
        onItemClickListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaction, parent, false)
        return TransactionViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val transaction = getItem(position)
        holder.bind(transaction)
        holder.itemView.setOnClickListener {
            onItemClickListener?.invoke(transaction)
        }
    }

    class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvCategory: TextView = itemView.findViewById(R.id.tvCategory)
        private val tvAmount: TextView = itemView.findViewById(R.id.tvAmount)
        private val tvDescription: TextView = itemView.findViewById(R.id.tvDescription)
        private val tvDate: TextView = itemView.findViewById(R.id.tvDate)

        private val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        private val dateFormatter = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))

        fun bind(transaction: Transaction) {
            tvCategory.text = transaction.category

            val amount = formatter.format(transaction.amount)
            if (transaction.type == "INCOME") {
                tvAmount.text = itemView.context.getString(R.string.format_positive_amount, amount)
                tvAmount.setTextColor(itemView.context.getColor(android.R.color.holo_green_dark))
            } else {
                tvAmount.text = itemView.context.getString(R.string.format_negative_amount, amount)
                tvAmount.setTextColor(itemView.context.getColor(android.R.color.holo_red_dark))
            }

            tvDescription.text = transaction.description
            tvDate.text = dateFormatter.format(Date(transaction.date))
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Transaction>() {
            override fun areItemsTheSame(oldItem: Transaction, newItem: Transaction): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Transaction, newItem: Transaction): Boolean {
                return oldItem == newItem
            }
        }
    }
}