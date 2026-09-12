package com.example.rigswap.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.rigswap.data.model.CartItem
import com.example.rigswap.databinding.ItemCartItemBinding

class CartAdapter(
    private val onRemoveClick: (CartItem) -> Unit
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    private val items = mutableListOf<CartItem>()

    fun submitList(newItems: List<CartItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val binding = ItemCartItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CartViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class CartViewHolder(private val binding: ItemCartItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: CartItem) {
            val context = binding.root.context
            val product = item.product

            binding.tvCartItemTitle.text = product.title
            binding.tvCartItemPrice.text = product.getFormattedPrice()
            binding.tvCartQty.text = "QTY: ${item.quantity}"

            ProductGridAdapter.bindProductImage(binding.ivCartThumb, product.imageResName)

            binding.btnRemoveCartItem.setOnClickListener {
                onRemoveClick(item)
            }
        }
    }
}
