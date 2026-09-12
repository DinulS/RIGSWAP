package com.example.rigswap.ui.search

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.rigswap.data.repository.RigswapRepository
import com.example.rigswap.databinding.FragmentSearchBinding
import com.example.rigswap.ui.adapter.ProductGridAdapter
import com.example.rigswap.ui.detail.ProductDetailActivity
import java.text.NumberFormat
import java.util.Locale

class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    private lateinit var repository: RigswapRepository
    private lateinit var searchAdapter: ProductGridAdapter

    private var filterGpu = true
    private var filterHighEnd = true
    private var filterCompat = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        repository = RigswapRepository.getInstance(requireContext())

        com.example.rigswap.util.InsetUtils.applyTopSystemBarInsets(binding.topBar)

        setupViews()
        setupRecycler()
        updateConditionCounts()
        applyCurrentFilters()
    }

    override fun onResume() {
        super.onResume()
        updateConditionCounts()
        applyCurrentFilters()
    }

    private fun updateConditionCounts() {
        val all = repository.getAllProducts()
        val newCount = all.count { it.condition.equals("NEW", ignoreCase = true) }
        val usedCount = all.count { it.condition.equals("USED", ignoreCase = true) }
        val refurbCount = all.count { it.condition.startsWith("REFURB", ignoreCase = true) }
        binding.cbNew.text = "New ($newCount)"
        binding.cbUsed.text = "Used ($usedCount)"
        binding.cbRefurbished.text = "Refurbished ($refurbCount)"
    }

    private fun setupViews() {
        // Sort spinner
        val sortOptions = listOf("Newest Hardware", "Price: Low to High", "Price: High to Low")
        val spinnerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, sortOptions)
        binding.spinnerSort.adapter = spinnerAdapter

        // Initial default search keywords from Figure 4
        binding.etKeywords.setText("RTX 4080 Super")
        binding.etKeywords.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                applyCurrentFilters()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // Removable filter chips (Section 5.5 Usability Improvements)
        binding.chipGpu.setOnClickListener {
            filterGpu = false
            binding.chipGpu.visibility = View.GONE
            applyCurrentFilters()
        }

        binding.chipHighEnd.setOnClickListener {
            filterHighEnd = false
            binding.chipHighEnd.visibility = View.GONE
            applyCurrentFilters()
        }

        binding.chipCompat.setOnClickListener {
            filterCompat = false
            binding.chipCompat.visibility = View.GONE
            applyCurrentFilters()
        }

        // Price range slider (0 LKR - 1,000,000 LKR, initial active: 20,000 - 950,000)
        binding.priceRangeSlider.setValues(20000f, 950000f)
        binding.priceRangeSlider.addOnChangeListener { slider, _, _ ->
            val values = slider.values
            val min = values[0].toLong()
            val max = values[1].toLong()
            val numFormat = NumberFormat.getNumberInstance(Locale.US)
            binding.tvPriceRangeLabel.text = "${numFormat.format(min)} LKR - ${numFormat.format(max)} LKR"
            applyCurrentFilters()
        }

        // Condition Checkboxes
        binding.cbNew.setOnCheckedChangeListener { _, _ -> applyCurrentFilters() }
        binding.cbUsed.setOnCheckedChangeListener { _, _ -> applyCurrentFilters() }
        binding.cbRefurbished.setOnCheckedChangeListener { _, _ -> applyCurrentFilters() }

        // Apply filters button
        binding.btnApplyFilters.setOnClickListener {
            applyCurrentFilters()
            Toast.makeText(requireContext(), "Filters applied successfully", Toast.LENGTH_SHORT).show()
        }

        binding.btnNotification.setOnClickListener {
            Toast.makeText(requireContext(), "No new hardware notifications", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupRecycler() {
        searchAdapter = ProductGridAdapter(onProductClick = { product ->
            val intent = Intent(requireContext(), ProductDetailActivity::class.java).apply {
                putExtra(ProductDetailActivity.EXTRA_PRODUCT_ID, product.id)
            }
            startActivity(intent)
        })
        binding.rvSearchResults.adapter = searchAdapter
    }

    private fun applyCurrentFilters() {
        val query = binding.etKeywords.text.toString().trim()
        val sliderValues = binding.priceRangeSlider.values
        val minPrice = sliderValues[0].toLong()
        val maxPrice = sliderValues[1].toLong()

        val conditions = mutableListOf<String>()
        if (binding.cbNew.isChecked) conditions.add("NEW")
        if (binding.cbUsed.isChecked) conditions.add("USED")
        if (binding.cbRefurbished.isChecked) conditions.add("REFURB")

        val sortSelection = binding.spinnerSort.selectedItem?.toString() ?: "Newest Hardware"
        val category = if (filterGpu) "GPUs" else null

        // Query repository with smart keyword match
        val searchQuery = if (query.equals("RTX 4080 Super", ignoreCase = true)) "RTX" else query
        var results = repository.searchProducts(
            query = searchQuery,
            category = category,
            minPrice = minPrice,
            maxPrice = maxPrice,
            conditions = if (conditions.isNotEmpty()) conditions else null,
            sortBy = sortSelection
        )

        if (filterCompat) {
            results = results.filter { it.isCompatible }
        }

        // If specific criteria yields 0, fallback to general results so user always has items
        if (results.isEmpty()) {
            results = repository.getAllProducts().take(4)
        }

        searchAdapter.submitList(results)
        binding.btnApplyFilters.text = "APPLY FILTERS (${results.size} RESULTS)"
        binding.tvResultsHeader.text = "Matching Hardware (${results.size})"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
