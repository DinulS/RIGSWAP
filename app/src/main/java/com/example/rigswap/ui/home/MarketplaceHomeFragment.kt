package com.example.rigswap.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.rigswap.data.repository.RigswapRepository
import com.example.rigswap.databinding.FragmentMarketplaceHomeBinding
import com.example.rigswap.ui.adapter.CategoryChipAdapter
import com.example.rigswap.ui.adapter.ProductGridAdapter
import com.example.rigswap.ui.detail.ProductDetailActivity

class MarketplaceHomeFragment : Fragment() {

    private var _binding: FragmentMarketplaceHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var repository: RigswapRepository
    private lateinit var productAdapter: ProductGridAdapter
    private lateinit var chipAdapter: CategoryChipAdapter

    private val categories = listOf("All", "GPUs", "CPUs", "Cooling", "Peripherals", "Storage", "PSUs")
    private var currentCategory = "All"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMarketplaceHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        repository = RigswapRepository.getInstance(requireContext())

        com.example.rigswap.util.InsetUtils.applyTopSystemBarInsets(binding.topBar)

        setupAdapters()
        loadProducts(currentCategory)

        binding.btnNotification.setOnClickListener {
            Toast.makeText(requireContext(), "You're all caught up with hardware alerts!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh products in case a new listing was added
        loadProducts(currentCategory)
    }

    private fun setupAdapters() {
        chipAdapter = CategoryChipAdapter(categories) { category ->
            currentCategory = category
            loadProducts(category)
        }
        binding.rvCategoryChips.adapter = chipAdapter

        productAdapter = ProductGridAdapter(onProductClick = { product ->
            val intent = Intent(requireContext(), ProductDetailActivity::class.java).apply {
                putExtra(ProductDetailActivity.EXTRA_PRODUCT_ID, product.id)
            }
            startActivity(intent)
        })
        binding.rvProductsGrid.adapter = productAdapter
    }

    private fun loadProducts(category: String) {
        val products = if (category == "All") {
            repository.getFeaturedProducts()
        } else {
            repository.getProductsByCategory(category)
        }
        productAdapter.submitList(products)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
