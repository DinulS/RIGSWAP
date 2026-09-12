package com.example.rigswap.ui.detail

import android.content.Intent
import android.os.Bundle
import android.graphics.BitmapFactory
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.rigswap.R
import com.example.rigswap.data.model.Product
import com.example.rigswap.data.repository.RigswapRepository
import com.example.rigswap.databinding.ActivityProductDetailBinding
import com.example.rigswap.ui.adapter.ImageSliderAdapter
import com.example.rigswap.ui.adapter.ProductGridAdapter
import com.example.rigswap.ui.chat.ChatActivity
import com.example.rigswap.ui.profile.CartActivity
import com.example.rigswap.ui.profile.CompatiblePartsActivity
import androidx.viewpager2.widget.ViewPager2
import java.io.File

class ProductDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProductDetailBinding
    private lateinit var repository: RigswapRepository
    private var product: Product? = null

    companion object {
        const val EXTRA_PRODUCT_ID = "extra_product_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        repository = RigswapRepository.getInstance(this)

        com.example.rigswap.util.InsetUtils.applyTopSystemBarInsets(binding.topBar)

        val productId = intent.getLongExtra(EXTRA_PRODUCT_ID, 1L)
        product = repository.getProductById(productId) ?: repository.getAllProducts().firstOrNull()

        bindProductData()

        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnNotification.setOnClickListener {
            Toast.makeText(this, "No new notifications", Toast.LENGTH_SHORT).show()
        }

        // Compatibility banner click
        binding.compatibilityBanner.setOnClickListener {
            val intent = Intent(this, CompatiblePartsActivity::class.java)
            startActivity(intent)
        }

        // Message seller button
        binding.btnMessage.setOnClickListener {
            product?.let { p ->
                val intent = Intent(this, ChatActivity::class.java).apply {
                    putExtra(ChatActivity.EXTRA_PRODUCT_ID, p.id)
                }
                startActivity(intent)
            }
        }

        // Add to cart button
        binding.btnAddToCart.setOnClickListener {
            product?.let { p ->
                repository.addToCart(p.id, 1)
                Toast.makeText(this, "${p.title} added to Cart!", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, CartActivity::class.java)
                startActivity(intent)
            }
        }
    }

    private fun setupImageSlider(product: Product) {
        val imageList = mutableListOf<String>()
        if (product.imageResName.isNotEmpty()) imageList.add(product.imageResName)
        if (product.imageResName2.isNotEmpty()) imageList.add(product.imageResName2)
        if (product.imageResName3.isNotEmpty()) imageList.add(product.imageResName3)

        if (imageList.isEmpty()) {
            imageList.add("img_rtx4090") // Fallback
        }

        val adapter = ImageSliderAdapter(imageList) { path ->
            showFullScreenImage(path, product.title)
        }
        binding.vpProductImages.adapter = adapter

        binding.tvImageIndicator.text = "1/${imageList.size}"
        binding.vpProductImages.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                binding.tvImageIndicator.text = "${position + 1}/${imageList.size}"
            }
        })
    }

    private fun showFullScreenImage(imageResName: String, title: String) {
        val dialog = android.app.Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
        dialog.setContentView(R.layout.dialog_image_viewer)

        val ivFull = dialog.findViewById<ImageView>(R.id.ivFullImage)
        val btnClose = dialog.findViewById<ImageView>(R.id.btnCloseViewer)
        val tvTitle = dialog.findViewById<android.widget.TextView>(R.id.tvViewerTitle)
        val topBar = dialog.findViewById<View>(R.id.topBarViewer)

        tvTitle.text = title
        com.example.rigswap.util.InsetUtils.applyTopSystemBarInsets(topBar)

        val file = File(imageResName)
        if (file.exists() && file.isFile) {
            val bitmap = BitmapFactory.decodeFile(file.absolutePath)
            if (bitmap != null) {
                ivFull.setImageBitmap(bitmap)
            } else {
                ProductGridAdapter.bindProductImage(ivFull, imageResName)
            }
        } else {
            ProductGridAdapter.bindProductImage(ivFull, imageResName)
        }

        btnClose.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    override fun onResume() {
        super.onResume()
        bindProductData()
    }

    private fun bindProductData() {
        val p = product ?: return

        binding.tvDetailTitle.text = p.title
        binding.tvDetailPrice.text = p.getFormattedPrice()
        binding.tvSellerName.text = p.sellerName
        binding.tvSellerStats.text = "⭐ ${p.sellerRating} (${p.sellerSales} sales)"
        binding.tvDetailDescription.text = if (p.description.isNotEmpty()) p.description else "Flagship PC hardware in excellent condition."

        // Image Slider
        setupImageSlider(p)

        // Category-aware Key Specifications
        binding.tvSpecBrand.text = if (p.brand.isNotEmpty()) p.brand else "Standard"
        binding.tvSpecModel.text = if (p.model.isNotEmpty()) p.model else p.title

        when (p.category) {
            "Peripherals" -> {
                // Peripherals: No TDP, No Memory, No Suggested PSU
                binding.rowSpecMemory.visibility = android.view.View.GONE
                binding.divSpecMemory.visibility = android.view.View.GONE

                binding.rowSpecTdp.visibility = android.view.View.GONE
                binding.divSpecTdp.visibility = android.view.View.GONE

                binding.rowSpecPsu.visibility = android.view.View.GONE
                binding.divSpecPsu.visibility = android.view.View.GONE

                binding.rowSpecConnector.visibility = android.view.View.VISIBLE
                binding.tvSpecConnectorLabel.text = "Connectivity"
                binding.tvSpecConnector.text = p.powerConnector
            }
            "Storage" -> {
                // Storage: Capacity & Form Factor; No TDP, No Suggested PSU
                binding.rowSpecMemory.visibility = android.view.View.VISIBLE
                binding.divSpecMemory.visibility = android.view.View.VISIBLE
                binding.tvSpecMemoryLabel.text = "Capacity & Type"
                binding.tvSpecMemory.text = p.memory

                binding.rowSpecTdp.visibility = android.view.View.GONE
                binding.divSpecTdp.visibility = android.view.View.GONE

                binding.rowSpecPsu.visibility = android.view.View.GONE
                binding.divSpecPsu.visibility = android.view.View.GONE

                binding.rowSpecConnector.visibility = android.view.View.VISIBLE
                binding.tvSpecConnectorLabel.text = "Form Factor"
                binding.tvSpecConnector.text = p.powerConnector
            }
            "PSUs" -> {
                // PSUs: Total Output & Efficiency; No Memory
                binding.rowSpecMemory.visibility = android.view.View.GONE
                binding.divSpecMemory.visibility = android.view.View.GONE

                binding.rowSpecTdp.visibility = android.view.View.VISIBLE
                binding.divSpecTdp.visibility = android.view.View.VISIBLE
                binding.tvSpecTdpLabel.text = "Wattage Output"
                binding.tvSpecTdp.text = p.tdp

                binding.rowSpecPsu.visibility = android.view.View.VISIBLE
                binding.divSpecPsu.visibility = android.view.View.VISIBLE
                binding.tvSpecPsuLabel.text = "Efficiency"
                binding.tvSpecPsu.text = p.suggestedPsu

                binding.rowSpecConnector.visibility = android.view.View.VISIBLE
                binding.tvSpecConnectorLabel.text = "Standards"
                binding.tvSpecConnector.text = p.powerConnector
            }
            "Cooling" -> {
                // Cooling: Radiator / Size & Heat Dissipation; No Suggested PSU
                binding.rowSpecMemory.visibility = android.view.View.VISIBLE
                binding.divSpecMemory.visibility = android.view.View.VISIBLE
                binding.tvSpecMemoryLabel.text = "Radiator / Size"
                binding.tvSpecMemory.text = p.memory

                if (p.tdp != "N/A" && p.tdp.isNotEmpty()) {
                    binding.rowSpecTdp.visibility = android.view.View.VISIBLE
                    binding.divSpecTdp.visibility = android.view.View.VISIBLE
                    binding.tvSpecTdpLabel.text = "Heat Dissipation"
                    binding.tvSpecTdp.text = p.tdp
                } else {
                    binding.rowSpecTdp.visibility = android.view.View.GONE
                    binding.divSpecTdp.visibility = android.view.View.GONE
                }

                binding.rowSpecPsu.visibility = android.view.View.GONE
                binding.divSpecPsu.visibility = android.view.View.GONE

                binding.rowSpecConnector.visibility = android.view.View.VISIBLE
                binding.tvSpecConnectorLabel.text = "Socket / Mount"
                binding.tvSpecConnector.text = p.powerConnector
            }
            "CPUs" -> {
                // CPUs: Cache, TDP, Suggested PSU, Socket
                binding.rowSpecMemory.visibility = android.view.View.VISIBLE
                binding.divSpecMemory.visibility = android.view.View.VISIBLE
                binding.tvSpecMemoryLabel.text = "L3 Cache"
                binding.tvSpecMemory.text = p.memory

                binding.rowSpecTdp.visibility = android.view.View.VISIBLE
                binding.divSpecTdp.visibility = android.view.View.VISIBLE
                binding.tvSpecTdpLabel.text = "TDP"
                binding.tvSpecTdp.text = p.tdp

                binding.rowSpecPsu.visibility = android.view.View.VISIBLE
                binding.divSpecPsu.visibility = android.view.View.VISIBLE
                binding.tvSpecPsuLabel.text = "Suggested PSU"
                binding.tvSpecPsu.text = p.suggestedPsu

                binding.rowSpecConnector.visibility = android.view.View.VISIBLE
                binding.tvSpecConnectorLabel.text = "Socket"
                binding.tvSpecConnector.text = p.powerConnector
            }
            "Motherboards" -> {
                // Motherboard: Form Factor & Socket
                binding.rowSpecMemory.visibility = android.view.View.VISIBLE
                binding.divSpecMemory.visibility = android.view.View.VISIBLE
                binding.tvSpecMemoryLabel.text = "Form Factor"
                binding.tvSpecMemory.text = p.memory

                binding.rowSpecTdp.visibility = android.view.View.GONE
                binding.divSpecTdp.visibility = android.view.View.GONE

                binding.rowSpecPsu.visibility = android.view.View.GONE
                binding.divSpecPsu.visibility = android.view.View.GONE

                binding.rowSpecConnector.visibility = android.view.View.VISIBLE
                binding.tvSpecConnectorLabel.text = "CPU Socket"
                binding.tvSpecConnector.text = p.powerConnector
            }
            "RAM" -> {
                // RAM: Standard & Speed
                binding.rowSpecMemory.visibility = android.view.View.VISIBLE
                binding.divSpecMemory.visibility = android.view.View.VISIBLE
                binding.tvSpecMemoryLabel.text = "RAM Standard"
                binding.tvSpecMemory.text = p.memory

                binding.rowSpecTdp.visibility = android.view.View.GONE
                binding.divSpecTdp.visibility = android.view.View.GONE

                binding.rowSpecPsu.visibility = android.view.View.GONE
                binding.divSpecPsu.visibility = android.view.View.GONE

                binding.rowSpecConnector.visibility = android.view.View.VISIBLE
                binding.tvSpecConnectorLabel.text = "Module Type"
                binding.tvSpecConnector.text = p.powerConnector
            }
            else -> {
                // GPUs & Other
                binding.rowSpecMemory.visibility = android.view.View.VISIBLE
                binding.divSpecMemory.visibility = android.view.View.VISIBLE
                binding.tvSpecMemoryLabel.text = "VRAM Memory"
                binding.tvSpecMemory.text = p.memory

                binding.rowSpecTdp.visibility = android.view.View.VISIBLE
                binding.divSpecTdp.visibility = android.view.View.VISIBLE
                binding.tvSpecTdpLabel.text = "TDP"
                binding.tvSpecTdp.text = p.tdp

                binding.rowSpecPsu.visibility = android.view.View.VISIBLE
                binding.divSpecPsu.visibility = android.view.View.VISIBLE
                binding.tvSpecPsuLabel.text = "Suggested PSU"
                binding.tvSpecPsu.text = p.suggestedPsu

                binding.rowSpecConnector.visibility = android.view.View.VISIBLE
                binding.tvSpecConnectorLabel.text = "Power Connector"
                binding.tvSpecConnector.text = p.powerConnector
            }
        }

        // Condition badge
        binding.tvDetailConditionBadge.text = p.condition
        when (p.condition.uppercase()) {
            "NEW" -> {
                binding.tvDetailConditionBadge.setTextColor(ContextCompat.getColor(this, R.color.badge_new_green))
                binding.tvDetailConditionBadge.setBackgroundResource(R.drawable.bg_badge_new)
            }
            "USED" -> {
                binding.tvDetailConditionBadge.setTextColor(ContextCompat.getColor(this, R.color.badge_used_orange))
                binding.tvDetailConditionBadge.setBackgroundResource(R.drawable.bg_badge_used)
            }
            else -> {
                binding.tvDetailConditionBadge.setTextColor(ContextCompat.getColor(this, R.color.badge_refurb_red))
                binding.tvDetailConditionBadge.setBackgroundResource(R.drawable.bg_badge_refurb)
            }
        }

        // Dynamic Compatibility Banner evaluated against current Saved Rig
        val savedRig = repository.getSavedRig()
        val (isCompat, reason) = repository.checkProductCompatibility(p, savedRig)

        if (isCompat) {
            binding.tvCompatibilityText.text = "Fits your saved build: $reason"
            binding.compatibilityBanner.setBackgroundResource(R.drawable.bg_badge_compat)
            binding.tvCompatibilityText.setTextColor(ContextCompat.getColor(this, R.color.badge_new_green))
        } else {
            binding.tvCompatibilityText.text = "Compatibility check: $reason"
            binding.compatibilityBanner.setBackgroundResource(R.drawable.bg_badge_pending)
            binding.tvCompatibilityText.setTextColor(ContextCompat.getColor(this, R.color.badge_used_orange))
        }
    }
}
