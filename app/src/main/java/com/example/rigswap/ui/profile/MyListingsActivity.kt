package com.example.rigswap.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.rigswap.MainActivity
import com.example.rigswap.R
import com.example.rigswap.data.repository.RigswapRepository
import com.example.rigswap.databinding.ActivityMyListingsBinding
import com.example.rigswap.ui.adapter.ProductGridAdapter
import com.example.rigswap.ui.detail.ProductDetailActivity

class MyListingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMyListingsBinding
    private lateinit var repository: RigswapRepository
    private lateinit var productAdapter: ProductGridAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyListingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        repository = RigswapRepository.getInstance(this)

        com.example.rigswap.util.InsetUtils.applyTopSystemBarInsets(binding.topBar)

        setupViews()
        loadListings()
    }

    override fun onResume() {
        super.onResume()
        loadListings()
    }

    private fun setupViews() {
        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnNotification.setOnClickListener {
            Toast.makeText(this, "Storefront inventory is synchronized", Toast.LENGTH_SHORT).show()
        }

        productAdapter = ProductGridAdapter(
            onProductClick = { product ->
                val intent = Intent(this, ProductDetailActivity::class.java).apply {
                    putExtra(ProductDetailActivity.EXTRA_PRODUCT_ID, product.id)
                }
                startActivity(intent)
            }
        )
        binding.rvMyListings.adapter = productAdapter

        binding.btnAddNewListing.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java).apply {
                putExtra(MainActivity.EXTRA_DESTINATION_TAB, R.id.nav_sell)
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            startActivity(intent)
            finish()
        }
    }

    private fun loadListings() {
        val user = repository.getUser()
        val sellerListings = repository.getSellerProducts(user.username)

        binding.tvListingsSummary.text = "${sellerListings.size} Products Listed"

        if (sellerListings.isEmpty()) {
            binding.layoutEmptyState.visibility = View.VISIBLE
            binding.rvMyListings.visibility = View.GONE
        } else {
            binding.layoutEmptyState.visibility = View.GONE
            binding.rvMyListings.visibility = View.VISIBLE
            productAdapter.submitList(sellerListings)
        }
    }
}

