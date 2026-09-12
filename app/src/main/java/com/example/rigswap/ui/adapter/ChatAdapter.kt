package com.example.rigswap.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.rigswap.data.model.ChatMessage
import com.example.rigswap.databinding.ItemChatBuyerBinding
import com.example.rigswap.databinding.ItemChatSellerBinding

class ChatAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_SELLER = 1
        private const val VIEW_TYPE_BUYER = 2
    }

    private val messages = mutableListOf<ChatMessage>()

    fun submitList(newMessages: List<ChatMessage>) {
        messages.clear()
        messages.addAll(newMessages)
        notifyDataSetChanged()
    }

    fun addMessage(message: ChatMessage) {
        messages.add(message)
        notifyItemInserted(messages.size - 1)
    }

    override fun getItemViewType(position: Int): Int {
        return if (messages[position].isFromMe) VIEW_TYPE_BUYER else VIEW_TYPE_SELLER
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_BUYER) {
            val binding = ItemChatBuyerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            BuyerMessageViewHolder(binding)
        } else {
            val binding = ItemChatSellerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            SellerMessageViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val msg = messages[position]
        if (holder is BuyerMessageViewHolder) {
            holder.bind(msg)
        } else if (holder is SellerMessageViewHolder) {
            holder.bind(msg)
        }
    }

    override fun getItemCount(): Int = messages.size

    inner class BuyerMessageViewHolder(private val binding: ItemChatBuyerBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(message: ChatMessage) {
            binding.tvBuyerMessage.text = message.message
            binding.tvBuyerTimestamp.text = message.timestamp
        }
    }

    inner class SellerMessageViewHolder(private val binding: ItemChatSellerBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(message: ChatMessage) {
            binding.tvSellerMessage.text = message.message
            binding.tvSellerTimestamp.text = message.timestamp
        }
    }
}
