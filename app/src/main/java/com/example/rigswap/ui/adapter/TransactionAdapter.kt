package com.example.rigswap.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.rigswap.R
import com.example.rigswap.data.model.SellerTransaction
import com.example.rigswap.databinding.ItemTransactionBinding

class TransactionAdapter : RecyclerView.Adapter<TransactionAdapter.TxViewHolder>() {

    private val transactions = mutableListOf<SellerTransaction>()

    fun submitList(newTransactions: List<SellerTransaction>) {
        transactions.clear()
        transactions.addAll(newTransactions)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TxViewHolder {
        val binding = ItemTransactionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TxViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TxViewHolder, position: Int) {
        holder.bind(transactions[position])
    }

    override fun getItemCount(): Int = transactions.size

    inner class TxViewHolder(private val binding: ItemTransactionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(tx: SellerTransaction) {
            val context = binding.root.context
            binding.tvTxTitle.text = tx.title
            binding.tvTxOrderInfo.text = "${tx.orderNumber} • ${tx.priceStr}"
            binding.tvTxStatusBadge.text = tx.status

            when (tx.status.uppercase()) {
                "SHIPPED" -> {
                    binding.tvTxStatusBadge.setBackgroundResource(R.drawable.bg_badge_shipped)
                    binding.tvTxStatusBadge.setTextColor(ContextCompat.getColor(context, R.color.badge_shipped_blue))
                }
                "DELIVERED" -> {
                    binding.tvTxStatusBadge.setBackgroundResource(R.drawable.bg_badge_delivered)
                    binding.tvTxStatusBadge.setTextColor(ContextCompat.getColor(context, R.color.badge_new_green))
                }
                "PENDING" -> {
                    binding.tvTxStatusBadge.setBackgroundResource(R.drawable.bg_badge_pending)
                    binding.tvTxStatusBadge.setTextColor(ContextCompat.getColor(context, R.color.badge_used_orange))
                }
                else -> {
                    binding.tvTxStatusBadge.setBackgroundResource(R.drawable.bg_badge_shipped)
                    binding.tvTxStatusBadge.setTextColor(ContextCompat.getColor(context, R.color.badge_shipped_blue))
                }
            }
        }
    }
}
