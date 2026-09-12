package com.example.rigswap.ui.profile

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.rigswap.R
import com.example.rigswap.data.model.SavedRig
import com.example.rigswap.data.repository.RigswapRepository
import com.example.rigswap.databinding.ActivitySavedRigSpecsBinding

class SavedRigSpecsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySavedRigSpecsBinding
    private lateinit var repository: RigswapRepository

    companion object {
        const val EXTRA_SOCKET = "extra_socket"
        const val EXTRA_RAM = "extra_ram"
        const val EXTRA_GPU_SLOT = "extra_gpu_slot"
        const val EXTRA_PSU = "extra_psu"

        // Common motherboard socket options
        val SOCKET_OPTIONS = listOf(
            "LGA 1851",
            "LGA 1700",
            "Socket AM5",
            "Socket AM4",
            "LGA 1200",
            "LGA 1151",
            "Socket sTR5",
            "Socket sTRX4",
            "LGA 2066"
        )

        // Common RAM standard options
        val RAM_OPTIONS = listOf(
            "DDR5 dual-channel",
            "DDR5 quad-channel",
            "DDR4 dual-channel",
            "DDR4 quad-channel",
            "DDR3 dual-channel",
            "DDR3 quad-channel"
        )

        // Common GPU slot options
        val GPU_SLOT_OPTIONS = listOf(
            "PCIe Gen 5.0 x16",
            "PCIe Gen 4.0 x16",
            "PCIe Gen 3.0 x16",
            "PCIe Gen 5.0 x8",
            "PCIe Gen 4.0 x8",
            "PCIe Gen 3.0 x8"
        )

        // Common PSU capacity options
        val PSU_OPTIONS = listOf(
            "1600 Watts Modular",
            "1200 Watts Modular",
            "1000 Watts Modular",
            "850 Watts Modular",
            "750 Watts Modular",
            "650 Watts Modular",
            "550 Watts Modular",
            "450 Watts"
        )

        /**
         * Returns motherboard sockets compatible with a selected PCIe GPU slot standard.
         * For example, PCIe Gen 5.0 is only supported on LGA 1851, LGA 1700, Socket AM5, and Socket sTR5.
         */
        fun getCompatibleSocketsForGpuSlot(gpuSlot: String): List<String> {
            val g = gpuSlot.trim()
            return when {
                g.contains("Gen 5", ignoreCase = true) || g.contains("5.0", ignoreCase = true) -> {
                    listOf("LGA 1851", "LGA 1700", "Socket AM5", "Socket sTR5")
                }
                g.contains("Gen 4", ignoreCase = true) || g.contains("4.0", ignoreCase = true) -> {
                    listOf("LGA 1851", "LGA 1700", "Socket AM5", "Socket AM4", "LGA 1200", "Socket sTR5", "Socket sTRX4")
                }
                else -> SOCKET_OPTIONS
            }
        }

        /**
         * Returns motherboard sockets compatible with a selected RAM standard.
         */
        fun getCompatibleSocketsForRam(ram: String): List<String> {
            val r = ram.trim()
            return when {
                r.contains("DDR5", ignoreCase = true) -> {
                    listOf("LGA 1851", "LGA 1700", "Socket AM5", "Socket sTR5")
                }
                r.contains("DDR4", ignoreCase = true) -> {
                    listOf("Socket AM4", "LGA 1700", "LGA 1200", "LGA 1151", "Socket sTRX4", "LGA 2066")
                }
                r.contains("DDR3", ignoreCase = true) -> {
                    listOf("LGA 1151")
                }
                else -> SOCKET_OPTIONS
            }
        }

        /**
         * Returns real-world hardware-compatible RAM options based on motherboard socket.
         */
        fun getCompatibleRamOptions(socket: String): List<String> {
            val s = socket.trim()
            return when {
                // Exclusively DDR5 platforms (LGA 1851, AM5, sTR5)
                s.equals("LGA 1851", ignoreCase = true) || s.contains("1851", ignoreCase = true) ||
                s.equals("Socket AM5", ignoreCase = true) || s.contains("AM5", ignoreCase = true) ||
                s.equals("Socket sTR5", ignoreCase = true) || s.contains("sTR5", ignoreCase = true) -> {
                    listOf("DDR5 dual-channel", "DDR5 quad-channel")
                }

                // Hybrid DDR5 / DDR4 platform (Intel LGA 1700)
                s.equals("LGA 1700", ignoreCase = true) || s.contains("1700", ignoreCase = true) -> {
                    listOf(
                        "DDR5 dual-channel",
                        "DDR5 quad-channel",
                        "DDR4 dual-channel",
                        "DDR4 quad-channel"
                    )
                }

                // Exclusively DDR4 platforms (Socket AM4, LGA 1200, LGA 2066, sTRX4)
                s.equals("Socket AM4", ignoreCase = true) || s.contains("AM4", ignoreCase = true) ||
                s.equals("LGA 1200", ignoreCase = true) || s.contains("1200", ignoreCase = true) ||
                s.equals("LGA 2066", ignoreCase = true) || s.contains("2066", ignoreCase = true) ||
                s.equals("Socket sTRX4", ignoreCase = true) || s.contains("sTRX4", ignoreCase = true) -> {
                    listOf("DDR4 dual-channel", "DDR4 quad-channel")
                }

                // DDR4 / DDR3 platform (Intel LGA 1151)
                s.equals("LGA 1151", ignoreCase = true) || s.contains("1151", ignoreCase = true) -> {
                    listOf(
                        "DDR4 dual-channel",
                        "DDR4 quad-channel",
                        "DDR3 dual-channel",
                        "DDR3 quad-channel"
                    )
                }

                else -> RAM_OPTIONS
            }
        }

        /**
         * Returns real-world hardware-compatible GPU PCIe slot options based on motherboard socket.
         */
        fun getCompatibleGpuSlotsForSocket(socket: String): List<String> {
            val s = socket.trim()
            return when {
                // Sockets supporting PCIe Gen 5.0, Gen 4.0, Gen 3.0
                s.equals("LGA 1851", ignoreCase = true) || s.contains("1851", ignoreCase = true) ||
                s.equals("LGA 1700", ignoreCase = true) || s.contains("1700", ignoreCase = true) ||
                s.equals("Socket AM5", ignoreCase = true) || s.contains("AM5", ignoreCase = true) ||
                s.equals("Socket sTR5", ignoreCase = true) || s.contains("sTR5", ignoreCase = true) -> {
                    listOf(
                        "PCIe Gen 5.0 x16",
                        "PCIe Gen 4.0 x16",
                        "PCIe Gen 3.0 x16",
                        "PCIe Gen 5.0 x8",
                        "PCIe Gen 4.0 x8",
                        "PCIe Gen 3.0 x8"
                    )
                }
                // Sockets supporting PCIe Gen 4.0 and Gen 3.0 (No PCIe Gen 5.0 in real life)
                s.equals("Socket AM4", ignoreCase = true) || s.contains("AM4", ignoreCase = true) ||
                s.equals("LGA 1200", ignoreCase = true) || s.contains("1200", ignoreCase = true) ||
                s.equals("Socket sTRX4", ignoreCase = true) || s.contains("sTRX4", ignoreCase = true) -> {
                    listOf(
                        "PCIe Gen 4.0 x16",
                        "PCIe Gen 3.0 x16",
                        "PCIe Gen 4.0 x8",
                        "PCIe Gen 3.0 x8"
                    )
                }
                // Legacy sockets supporting PCIe Gen 3.0 only (LGA 1151, LGA 2066)
                else -> {
                    listOf(
                        "PCIe Gen 3.0 x16",
                        "PCIe Gen 3.0 x8"
                    )
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySavedRigSpecsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        repository = RigswapRepository.getInstance(this)

        com.example.rigswap.util.InsetUtils.applyTopSystemBarInsets(binding.topBar)

        setupDropdowns()
        loadSavedRig()

        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnNotification.setOnClickListener {
            Toast.makeText(this, "Hardware compatibility engine is active.", Toast.LENGTH_SHORT).show()
        }

        binding.btnSaveSpecs.setOnClickListener {
            saveRigSpecs()
            Toast.makeText(this, "Rig specs saved successfully!", Toast.LENGTH_SHORT).show()
        }

        binding.btnFindCompatibleParts.setOnClickListener {
            saveRigSpecs()

            val intent = Intent(this, CompatiblePartsActivity::class.java).apply {
                putExtra(EXTRA_SOCKET, binding.tvSocket.text.toString().trim())
                putExtra(EXTRA_RAM, binding.tvRam.text.toString().trim())
                putExtra(EXTRA_GPU_SLOT, binding.tvGpuSlot.text.toString().trim())
                putExtra(EXTRA_PSU, binding.tvPsu.text.toString().trim())
            }
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        loadSavedRig()
    }

    private fun setupDropdowns() {
        // Socket Dropdown: dynamically filtered by current GPU slot and RAM requirements
        setupInteractiveDropdown(
            binding.tvSocket,
            optionsProvider = {
                val currentGpu = binding.tvGpuSlot.text.toString().trim()
                val socketsForGpu = getCompatibleSocketsForGpuSlot(currentGpu)
                socketsForGpu
            },
            onItemSelected = { selectedSocket ->
                onSocketSelected(selectedSocket)
            }
        )

        // RAM Dropdown: dynamically filtered by current Socket
        setupInteractiveDropdown(
            binding.tvRam,
            optionsProvider = {
                val currentSocket = binding.tvSocket.text.toString().trim()
                getCompatibleRamOptions(currentSocket)
            },
            onItemSelected = { selectedRam ->
                onRamSelected(selectedRam)
            }
        )

        // GPU Slot Dropdown: dynamically filtered by current Socket
        setupInteractiveDropdown(
            binding.tvGpuSlot,
            optionsProvider = {
                val currentSocket = binding.tvSocket.text.toString().trim()
                getCompatibleGpuSlotsForSocket(currentSocket)
            },
            onItemSelected = { selectedGpuSlot ->
                onGpuSlotSelected(selectedGpuSlot)
            }
        )

        // PSU Dropdown
        setupInteractiveDropdown(
            binding.tvPsu,
            optionsProvider = { PSU_OPTIONS },
            onItemSelected = { /* no constraint updates required */ }
        )
    }

    private fun onSocketSelected(socket: String) {
        // 1. Update RAM options and auto-correct if needed
        val compatibleRam = getCompatibleRamOptions(socket)
        val currentRam = binding.tvRam.text.toString().trim()
        if (currentRam.isEmpty() || !compatibleRam.contains(currentRam)) {
            val fallbackRam = compatibleRam.first()
            binding.tvRam.setText(fallbackRam, false)
            if (currentRam.isNotEmpty()) {
                val ddrType = if (fallbackRam.startsWith("DDR5")) "DDR5" else "DDR4"
                Toast.makeText(this, "$socket only supports $ddrType memory. RAM set to $fallbackRam.", Toast.LENGTH_SHORT).show()
            }
        }

        // 2. Update GPU Slot options and auto-correct if needed
        val compatibleGpuSlots = getCompatibleGpuSlotsForSocket(socket)
        val currentGpuSlot = binding.tvGpuSlot.text.toString().trim()
        if (currentGpuSlot.isEmpty() || !compatibleGpuSlots.contains(currentGpuSlot)) {
            val fallbackGpu = compatibleGpuSlots.first()
            binding.tvGpuSlot.setText(fallbackGpu, false)
            if (currentGpuSlot.isNotEmpty()) {
                Toast.makeText(this, "$socket does not support $currentGpuSlot. GPU slot set to $fallbackGpu.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun onGpuSlotSelected(gpuSlot: String) {
        val compatibleSockets = getCompatibleSocketsForGpuSlot(gpuSlot)
        val currentSocket = binding.tvSocket.text.toString().trim()

        if (currentSocket.isEmpty() || !compatibleSockets.contains(currentSocket)) {
            val fallbackSocket = compatibleSockets.first()
            binding.tvSocket.setText(fallbackSocket, false)
            Toast.makeText(this, "$gpuSlot requires a compatible motherboard. Socket set to $fallbackSocket.", Toast.LENGTH_SHORT).show()
            onSocketSelected(fallbackSocket)
        }
    }

    private fun onRamSelected(ram: String) {
        val compatibleSockets = getCompatibleSocketsForRam(ram)
        val currentSocket = binding.tvSocket.text.toString().trim()

        if (currentSocket.isEmpty() || !compatibleSockets.contains(currentSocket)) {
            val fallbackSocket = compatibleSockets.first()
            binding.tvSocket.setText(fallbackSocket, false)
            Toast.makeText(this, "$ram is not supported on $currentSocket. Socket set to $fallbackSocket.", Toast.LENGTH_SHORT).show()
            onSocketSelected(fallbackSocket)
        }
    }

    private fun setupInteractiveDropdown(
        autoCompleteView: AutoCompleteTextView,
        optionsProvider: () -> List<String>,
        onItemSelected: (String) -> Unit
    ) {
        val initialOptions = optionsProvider()
        val initialAdapter = ArrayAdapter(this, R.layout.item_dropdown_spec, initialOptions)
        autoCompleteView.setAdapter(initialAdapter)

        autoCompleteView.setOnClickListener {
            val freshOptions = optionsProvider()
            val freshAdapter = ArrayAdapter(this, R.layout.item_dropdown_spec, freshOptions)
            autoCompleteView.setAdapter(freshAdapter)
            freshAdapter.filter.filter(null)
            autoCompleteView.showDropDown()
        }

        autoCompleteView.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                val freshOptions = optionsProvider()
                val freshAdapter = ArrayAdapter(this, R.layout.item_dropdown_spec, freshOptions)
                autoCompleteView.setAdapter(freshAdapter)
                freshAdapter.filter.filter(null)
                autoCompleteView.showDropDown()
            }
        }

        autoCompleteView.setOnItemClickListener { parent, _, position, _ ->
            val selected = parent.getItemAtPosition(position).toString()
            onItemSelected(selected)
        }
    }

    private fun loadSavedRig() {
        val savedRig = repository.getSavedRig()

        binding.tvRigName.setText(savedRig.name)
        binding.tvRigSubtitle.setText(savedRig.subtitle)
        binding.tvHealthScore.text = savedRig.healthScore
        binding.tvSocket.setText(savedRig.socket, false)

        val compatibleRam = getCompatibleRamOptions(savedRig.socket)
        val validRam = if (compatibleRam.contains(savedRig.ramStandard)) savedRig.ramStandard else compatibleRam.first()
        binding.tvRam.setText(validRam, false)

        val compatibleGpu = getCompatibleGpuSlotsForSocket(savedRig.socket)
        val validGpu = if (compatibleGpu.contains(savedRig.gpuSlot)) savedRig.gpuSlot else compatibleGpu.first()
        binding.tvGpuSlot.setText(validGpu, false)

        binding.tvPsu.setText(savedRig.psuCapacity, false)
    }

    private fun saveRigSpecs() {
        val updatedRig = SavedRig(
            id = 1,
            name = binding.tvRigName.text.toString().trim(),
            subtitle = binding.tvRigSubtitle.text.toString().trim(),
            socket = binding.tvSocket.text.toString().trim(),
            ramStandard = binding.tvRam.text.toString().trim(),
            gpuSlot = binding.tvGpuSlot.text.toString().trim(),
            psuCapacity = binding.tvPsu.text.toString().trim(),
            healthScore = binding.tvHealthScore.text.toString().trim()
        )
        repository.updateSavedRig(updatedRig)
    }
}
