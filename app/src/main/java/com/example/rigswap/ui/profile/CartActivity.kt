package com.example.rigswap.ui.profile

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.rigswap.data.repository.RigswapRepository
import com.example.rigswap.databinding.ActivityCartBinding
import com.example.rigswap.ui.adapter.CartAdapter
import java.text.NumberFormat
import java.util.Locale

class CartActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCartBinding
    private lateinit var repository: RigswapRepository
    private lateinit var cartAdapter: CartAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        repository = RigswapRepository.getInstance(this)

        com.example.rigswap.util.InsetUtils.applyTopSystemBarInsets(binding.topBar)

        setupCartRecycler()
        loadCartData()

        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnNotification.setOnClickListener {
            Toast.makeText(this, "No cart alerts", Toast.LENGTH_SHORT).show()
        }

        binding.btnCheckout.setOnClickListener {
            handleCheckout()
        }
    }

    private fun setupCartRecycler() {
        cartAdapter = CartAdapter(onRemoveClick = { item ->
            repository.removeFromCart(item.id)
            Toast.makeText(this, "${item.product.title} removed from cart", Toast.LENGTH_SHORT).show()
            loadCartData()
        })
        binding.rvCartItems.adapter = cartAdapter
    }

    private fun loadCartData() {
        val items = repository.getCartItems()
        cartAdapter.submitList(items)

        if (items.isEmpty()) {
            binding.tvEmptyCart.visibility = View.VISIBLE
            binding.cardSummary.visibility = View.GONE
            binding.btnCheckout.isEnabled = false
            binding.btnCheckout.alpha = 0.5f
        } else {
            binding.tvEmptyCart.visibility = View.GONE
            binding.cardSummary.visibility = View.VISIBLE
            binding.btnCheckout.isEnabled = true
            binding.btnCheckout.alpha = 1.0f

            var subtotal = 0L
            for (i in items) {
                subtotal += (i.product.priceLkr * i.quantity)
            }

            val formatter = NumberFormat.getNumberInstance(Locale.US)
            val subtotalStr = "${formatter.format(subtotal)} LKR"

            binding.tvSubtotal.text = subtotalStr
            binding.tvTotalDue.text = subtotalStr
        }
    }

    private fun handleCheckout() {
        AlertDialog.Builder(this)
            .setTitle("Confirm Order")
            .setMessage("Place order for hardware components with Buyer Protection?")
            .setPositiveButton("Confirm Order") { _, _ ->
                repository.addTransaction("Flagship Hardware Order", "Order #TX-5021 • Paid", binding.tvTotalDue.text.toString(), "PENDING")
                repository.clearCart()
                loadCartData()
                Toast.makeText(this, "Order placed successfully! Tracking active in Merchant Hub.", Toast.LENGTH_LONG).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
