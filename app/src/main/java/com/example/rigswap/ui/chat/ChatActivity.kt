package com.example.rigswap.ui.chat

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.rigswap.data.model.Product
import com.example.rigswap.data.repository.RigswapRepository
import com.example.rigswap.databinding.ActivityChatBinding
import com.example.rigswap.ui.adapter.ChatAdapter
import com.example.rigswap.ui.adapter.ProductGridAdapter
import com.example.rigswap.ui.profile.CartActivity

class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding
    private lateinit var repository: RigswapRepository
    private lateinit var chatAdapter: ChatAdapter
    private var product: Product? = null

    companion object {
        const val EXTRA_PRODUCT_ID = "extra_product_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        repository = RigswapRepository.getInstance(this)
        val productId = intent.getLongExtra(EXTRA_PRODUCT_ID, 1L)
        product = repository.getProductById(productId) ?: repository.getAllProducts().firstOrNull()

        com.example.rigswap.util.InsetUtils.applyTopSystemBarInsets(binding.topBar)

        setupViews(productId)
        setupChatRecycler(productId)
    }

    private fun setupViews(productId: Long) {
        val p = product ?: return

        binding.tvAttachedProductTitle.text = p.title
        binding.tvAttachedProductPrice.text = p.getFormattedPrice()
        binding.tvSellerName.text = p.sellerName

        ProductGridAdapter.bindProductImage(binding.ivAttachedProductThumb, p.imageResName)

        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnQuickBuy.setOnClickListener {
            repository.addToCart(p.id, 1)
            Toast.makeText(this, "${p.title} added to cart!", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, CartActivity::class.java)
            startActivity(intent)
        }

        binding.btnAttachImage.setOnClickListener {
            Toast.makeText(this, "Photo attachment ready", Toast.LENGTH_SHORT).show()
        }

        binding.btnSendMessage.setOnClickListener {
            sendUserMessage(productId)
        }
    }

    private fun setupChatRecycler(productId: Long) {
        chatAdapter = ChatAdapter()
        binding.rvChatMessages.adapter = chatAdapter

        val messages = repository.getMessagesForProduct(productId)
        chatAdapter.submitList(messages)
        if (messages.isNotEmpty()) {
            binding.rvChatMessages.scrollToPosition(messages.size - 1)
        }
    }

    private fun sendUserMessage(productId: Long) {
        val text = binding.etMessageInput.text.toString().trim()
        if (text.isEmpty()) return

        val newMessage = repository.sendMessage(productId, text, senderName = "Me", isFromMe = true)
        chatAdapter.addMessage(newMessage)
        binding.rvChatMessages.smoothScrollToPosition(chatAdapter.itemCount - 1)
        binding.etMessageInput.setText("")

        // Simulate seller reply after 1.5 seconds if first reply
        Handler(Looper.getMainLooper()).postDelayed({
            val sellerReply = repository.sendMessage(
                productId,
                "Sounds great! Let me know if you want local pickup or fast shipping.",
                senderName = product?.sellerName ?: "SolderSlinger_99",
                isFromMe = false
            )
            chatAdapter.addMessage(sellerReply)
            binding.rvChatMessages.smoothScrollToPosition(chatAdapter.itemCount - 1)
        }, 1500)
    }
}
