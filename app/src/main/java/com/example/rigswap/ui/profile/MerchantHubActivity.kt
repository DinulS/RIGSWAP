package com.example.rigswap.ui.profile

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.rigswap.MainActivity
import com.example.rigswap.R
import com.example.rigswap.data.repository.RigswapRepository
import com.example.rigswap.databinding.ActivityMerchantHubBinding
import com.example.rigswap.ui.adapter.TransactionAdapter

class MerchantHubActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMerchantHubBinding
    private lateinit var repository: RigswapRepository
    private lateinit var transactionAdapter: TransactionAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMerchantHubBinding.inflate(layoutInflater)
        setContentView(binding.root)

        repository = RigswapRepository.getInstance(this)

        com.example.rigswap.util.InsetUtils.applyTopSystemBarInsets(binding.topBar)

        setupViews()
        loadDashboardData()
    }

    override fun onResume() {
        super.onResume()
        loadDashboardData()
    }

    private fun loadDashboardData() {
        val user = repository.getUser()
        val actualListings = repository.getSellerProducts(user.username)
        val count = if (actualListings.isNotEmpty()) actualListings.size else user.activeListingsCount
        binding.tvActiveListingsCount.text = count.toString()
        binding.tvTotalSales.text = user.getFormattedSales()
        transactionAdapter.submitList(repository.getAllTransactions())
    }

    private fun setupViews() {
        transactionAdapter = TransactionAdapter()
        binding.rvTransactions.adapter = transactionAdapter

        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnNotification.setOnClickListener {
            Toast.makeText(this, "Order notifications up to date", Toast.LENGTH_SHORT).show()
        }

        binding.btnActiveListings.setOnClickListener {
            val intent = Intent(this, MyListingsActivity::class.java)
            startActivity(intent)
        }

        binding.btnCreateListing.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java).apply {
                putExtra(MainActivity.EXTRA_DESTINATION_TAB, R.id.nav_sell)
            }
            startActivity(intent)
            finish()
        }
    }
}
