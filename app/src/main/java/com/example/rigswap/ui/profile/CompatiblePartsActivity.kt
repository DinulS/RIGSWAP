package com.example.rigswap.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.rigswap.data.repository.RigswapRepository
import com.example.rigswap.databinding.ActivityCompatiblePartsBinding
import com.example.rigswap.ui.adapter.CategoryChipAdapter
import com.example.rigswap.ui.adapter.ProductGridAdapter
import com.example.rigswap.ui.detail.ProductDetailActivity

class CompatiblePartsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCompatiblePartsBinding
    private lateinit var repository: RigswapRepository
    private lateinit var productAdapter: ProductGridAdapter
    private lateinit var chipAdapter: CategoryChipAdapter

    private val categories = listOf("All", "GPUs", "CPUs", "Cooling", "Peripherals", "Storage", "PSUs")
    private var currentCategory = "All"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCompatiblePartsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        repository = RigswapRepository.getInstance(this)

        com.example.rigswap.util.InsetUtils.applyTopSystemBarInsets(binding.topBar)

        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnNotification.setOnClickListener {
            Toast.makeText(this, "Hardware compatibility engine active", Toast.LENGTH_SHORT).show()
        }

        setupAdapters()
        refreshRigUI()
    }

    override fun onResume() {
        super.onResume()
        refreshRigUI()
    }

    private fun getActiveRig(): com.example.rigswap.data.model.SavedRig {
        val dbRig = repository.getSavedRig()
        val extraSocket = intent.getStringExtra(SavedRigSpecsActivity.EXTRA_SOCKET)
        val extraRam = intent.getStringExtra(SavedRigSpecsActivity.EXTRA_RAM)
        val extraGpuSlot = intent.getStringExtra(SavedRigSpecsActivity.EXTRA_GPU_SLOT)
        val extraPsu = intent.getStringExtra(SavedRigSpecsActivity.EXTRA_PSU)

        return if (!extraPsu.isNullOrBlank() || !extraSocket.isNullOrBlank()) {
            dbRig.copy(
                socket = if (!extraSocket.isNullOrBlank()) extraSocket else dbRig.socket,
                ramStandard = if (!extraRam.isNullOrBlank()) extraRam else dbRig.ramStandard,
                gpuSlot = if (!extraGpuSlot.isNullOrBlank()) extraGpuSlot else dbRig.gpuSlot,
                psuCapacity = if (!extraPsu.isNullOrBlank()) extraPsu else dbRig.psuCapacity
            )
        } else {
            dbRig
        }
    }

    private fun refreshRigUI() {
        val savedRig = getActiveRig()

        binding.tvCompatRigTitle.text = savedRig.name
        binding.tvCompatRigSubtitle.text = savedRig.subtitle

        val ramShort = savedRig.ramStandard.split(" ").firstOrNull() ?: ""
        val psuShort = savedRig.psuCapacity.split(" ").firstOrNull() ?: ""
        binding.tvCompatRigSpecs.text = "${savedRig.socket} • $ramShort • ${psuShort}W PSU"

        binding.tvCompatScore.text = savedRig.healthScore

        loadCompatibleParts(currentCategory)
    }

    private fun setupAdapters() {
        chipAdapter = CategoryChipAdapter(categories) { category ->
            currentCategory = category
            loadCompatibleParts(category)
        }
        binding.rvCompatCategoryChips.adapter = chipAdapter

        // isCompatView = true to show green COMPAT badge on each card
        productAdapter = ProductGridAdapter(
            onProductClick = { product ->
                val intent = Intent(this, ProductDetailActivity::class.java).apply {
                    putExtra(ProductDetailActivity.EXTRA_PRODUCT_ID, product.id)
                }
                startActivity(intent)
            },
            isCompatView = true
        )
        binding.rvCompatPartsGrid.adapter = productAdapter
    }

    private fun loadCompatibleParts(category: String) {
        val savedRig = getActiveRig()
        val parts = repository.getCompatibleProductsByRig(savedRig, category)
        productAdapter.submitList(parts)

        if (parts.isEmpty()) {
            binding.tvEmptyCompatState.visibility = View.VISIBLE
            binding.rvCompatPartsGrid.visibility = View.GONE
        } else {
            binding.tvEmptyCompatState.visibility = View.GONE
            binding.rvCompatPartsGrid.visibility = View.VISIBLE
        }
    }
}
