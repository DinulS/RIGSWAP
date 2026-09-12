package com.example.rigswap.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.rigswap.R
import com.example.rigswap.databinding.ItemCategoryChipBinding

class CategoryChipAdapter(
    private val categories: List<String>,
    private val onCategorySelected: (String) -> Unit
) : RecyclerView.Adapter<CategoryChipAdapter.ChipViewHolder>() {

    private var selectedIndex = 0

    fun setSelectedCategory(category: String) {
        val index = categories.indexOf(category)
        if (index != -1 && index != selectedIndex) {
            val prev = selectedIndex
            selectedIndex = index
            notifyItemChanged(prev)
            notifyItemChanged(selectedIndex)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChipViewHolder {
        val binding = ItemCategoryChipBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ChipViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChipViewHolder, position: Int) {
        holder.bind(categories[position], position == selectedIndex)
    }

    override fun getItemCount(): Int = categories.size

    inner class ChipViewHolder(private val binding: ItemCategoryChipBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(category: String, isSelected: Boolean) {
            val context = binding.root.context
            binding.tvChip.text = category
            if (isSelected) {
                binding.tvChip.setBackgroundResource(R.drawable.bg_chip_selected)
                binding.tvChip.setTextColor(ContextCompat.getColor(context, R.color.text_dark))
            } else {
                binding.tvChip.setBackgroundResource(R.drawable.bg_chip_unselected)
                binding.tvChip.setTextColor(ContextCompat.getColor(context, R.color.text_primary))
            }

            binding.root.setOnClickListener {
                val pos = bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    val prev = selectedIndex
                    selectedIndex = pos
                    notifyItemChanged(prev)
                    notifyItemChanged(selectedIndex)
                    onCategorySelected(category)
                }
            }
        }
    }
}
