package com.example.rigswap.ui.chat

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.rigswap.data.model.Product
import com.example.rigswap.data.repository.RigswapRepository
import com.example.rigswap.databinding.FragmentChatBinding
import com.example.rigswap.ui.adapter.ChatAdapter
import com.example.rigswap.ui.adapter.ProductGridAdapter
import com.example.rigswap.ui.profile.CartActivity

class ChatFragment : Fragment() {

    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!

    private lateinit var repository: RigswapRepository
    private lateinit var chatAdapter: ChatAdapter
    private var product: Product? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        repository = RigswapRepository.getInstance(requireContext())
        product = repository.getProductById(1L) ?: repository.getAllProducts().firstOrNull()

        com.example.rigswap.util.InsetUtils.applyTopSystemBarInsets(binding.topBar)

        setupViews()
        setupChatRecycler()
    }

    private fun setupViews() {
        val p = product ?: return

        binding.tvAttachedProductTitle.text = p.title
        binding.tvAttachedProductPrice.text = p.getFormattedPrice()
        binding.tvSellerName.text = p.sellerName

        ProductGridAdapter.bindProductImage(binding.ivAttachedProductThumb, p.imageResName)

        binding.btnQuickBuy.setOnClickListener {
            repository.addToCart(p.id, 1)
            Toast.makeText(requireContext(), "${p.title} added to cart!", Toast.LENGTH_SHORT).show()
            val intent = Intent(requireContext(), CartActivity::class.java)
            startActivity(intent)
        }

        binding.btnAttachImage.setOnClickListener {
            Toast.makeText(requireContext(), "Image attachment ready", Toast.LENGTH_SHORT).show()
        }

        binding.btnSendMessage.setOnClickListener {
            sendUserMessage()
        }
    }

    private fun setupChatRecycler() {
        chatAdapter = ChatAdapter()
        binding.rvChatMessages.adapter = chatAdapter

        val messages = repository.getMessagesForProduct(1L)
        chatAdapter.submitList(messages)
        if (messages.isNotEmpty()) {
            binding.rvChatMessages.scrollToPosition(messages.size - 1)
        }
    }

    private fun sendUserMessage() {
        val text = binding.etMessageInput.text.toString().trim()
        if (text.isEmpty()) return

        val newMessage = repository.sendMessage(1L, text, senderName = "Me", isFromMe = true)
        chatAdapter.addMessage(newMessage)
        binding.rvChatMessages.smoothScrollToPosition(chatAdapter.itemCount - 1)
        binding.etMessageInput.setText("")

        Handler(Looper.getMainLooper()).postDelayed({
            if (_binding != null) {
                val sellerReply = repository.sendMessage(
                    1L,
                    "Sounds great! Let me know if you want local pickup or fast shipping.",
                    senderName = product?.sellerName ?: "SolderSlinger_99",
                    isFromMe = false
                )
                chatAdapter.addMessage(sellerReply)
                binding.rvChatMessages.smoothScrollToPosition(chatAdapter.itemCount - 1)
            }
        }, 1500)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
