package com.example.rigswap.ui.sell

import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.example.rigswap.R
import com.example.rigswap.data.model.MotherboardSocketCompatibility
import com.example.rigswap.data.model.Product
import com.example.rigswap.data.model.RamStandardCompatibility
import com.example.rigswap.data.repository.RigswapRepository
import com.example.rigswap.databinding.FragmentCreateListingBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.io.File
import java.io.FileOutputStream

class CreateListingFragment : Fragment() {

    private var _binding: FragmentCreateListingBinding? = null
    private val binding get() = _binding!!

    private lateinit var repository: RigswapRepository
    private var selectedCondition = "Used"

    companion object {
        private const val OTHER_BRAND = "Other"
        private const val OTHER_MODEL = "Other / Custom"
    }

    private var currentTargetSlot: Int = 1
    private val photoPaths = mutableMapOf<Int, String>()
    private var pendingCameraFile: File? = null
    private var pendingCameraUri: Uri? = null

    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            val savedPath = saveImageToInternalStorage(uri)
            if (savedPath != null) {
                photoPaths[currentTargetSlot] = savedPath
                updateSlotUI(currentTargetSlot, savedPath)
                Toast.makeText(requireContext(), "Photo added to slot $currentTargetSlot", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Failed to load selected photo", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success: Boolean ->
        if (success && pendingCameraFile != null && pendingCameraFile!!.exists()) {
            val savedPath = pendingCameraFile!!.absolutePath
            photoPaths[currentTargetSlot] = savedPath
            updateSlotUI(currentTargetSlot, savedPath)
            Toast.makeText(requireContext(), "High-resolution camera photo attached to slot $currentTargetSlot", Toast.LENGTH_SHORT).show()
        } else {
            // If cancelled or failed, clean up pending empty file if needed
            pendingCameraFile?.let {
                if (it.exists() && it.length() == 0L) {
                    it.delete()
                }
            }
        }
        pendingCameraFile = null
        pendingCameraUri = null
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateListingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        repository = RigswapRepository.getInstance(requireContext())

        com.example.rigswap.util.InsetUtils.applyTopSystemBarInsets(binding.topBar)

        setupCategorySpinner()
        setupConditionSelector()
        setupPhotoSlots()

        binding.btnPostListing.setOnClickListener {
            handlePostListing()
        }

        binding.btnNotification.setOnClickListener {
            Toast.makeText(requireContext(), "No seller notifications", Toast.LENGTH_SHORT).show()
        }
    }

    // ── Brand lists per category ──────────────────────────────────────────────
    private val brandsByCategory = mapOf(
        "GPUs"         to listOf("ASUS ROG", "Gigabyte AORUS", "MSI Gaming X", "Sapphire", "PowerColor", "XFX", "ZOTAC", "PNY", "Inno3D"),
        "CPUs"         to listOf("AMD", "Intel"),
        "Motherboards" to listOf("ASUS ROG", "ASUS TUF", "Gigabyte AORUS", "MSI MEG", "MSI MAG", "ASRock", "NZXT"),
        "RAM"          to listOf("Corsair", "G.Skill", "Kingston", "Samsung", "Crucial", "TeamGroup", "ADATA"),
        "Cooling"      to listOf("Corsair", "NZXT", "be quiet!", "Noctua", "DeepCool", "Arctic", "Lian Li"),
        "Peripherals"  to listOf("Logitech", "Razer", "SteelSeries", "HyperX", "Corsair", "ASUS ROG", "Wooting"),
        "Storage"      to listOf("Samsung", "Western Digital", "Seagate", "Kingston", "Crucial", "SK Hynix"),
        "PSUs"         to listOf("Corsair", "EVGA", "Seasonic", "be quiet!", "MSI", "Thermaltake", "NZXT")
    )

    // ── Model lists per "category|brand" key ─────────────────────────────────
    private val modelsByKey = mapOf(
        // GPUs
        "GPUs|ASUS ROG"        to listOf("ROG Strix RTX 4090 OC", "ROG Strix RTX 4080 SUPER", "ROG Strix RTX 4070 Ti SUPER", "ROG Strix RX 7900 XTX"),
        "GPUs|Gigabyte AORUS"  to listOf("AORUS Master RTX 4090", "AORUS Xtreme RTX 4080", "AORUS Elite RTX 4070 Ti SUPER", "AORUS RX 7900 XT"),
        "GPUs|MSI Gaming X"    to listOf("SUPRIM X RTX 4090", "Gaming X Trio RTX 4080", "Gaming X Trio RTX 4070 Ti SUPER", "Gaming X RX 7900 XTX"),
        "GPUs|Sapphire"        to listOf("NITRO+ RX 7900 XTX", "NITRO+ RX 7900 XT", "PULSE RX 7800 XT", "NITRO+ RX 6800 XT"),
        "GPUs|PowerColor"      to listOf("Red Devil RX 7900 XTX", "Hellhound RX 7800 XT", "Fighter RX 7700"),
        "GPUs|XFX"             to listOf("Speedster MERC RX 7900 XTX", "Speedster QICK RX 7800 XT", "Speedster SWFT RX 7700"),
        "GPUs|ZOTAC"           to listOf("GAMING Trinity RTX 4090", "GAMING Trinity RTX 4080 SUPER", "GAMING Twin Edge RTX 4070 SUPER"),
        "GPUs|PNY"             to listOf("XLR8 RTX 4090 OC", "XLR8 RTX 4080 SUPER OC", "XLR8 RTX 4070 Ti OC"),
        "GPUs|Inno3D"          to listOf("iChill RTX 4090 X4", "iChill RTX 4080 SUPER X3", "iChill RTX 4070 Ti SUPER X3"),
        // CPUs
        "CPUs|AMD"             to listOf("Ryzen 9 9950X", "Ryzen 9 7950X3D", "Ryzen 9 7900X3D", "Ryzen 7 9700X", "Ryzen 7 7800X3D", "Ryzen 5 9600X", "Ryzen 5 7600X"),
        "CPUs|Intel"           to listOf("Core Ultra 9 285K", "Core i9-14900K", "Core i9-13900K", "Core i7-14700K", "Core i7-13700K", "Core i5-14600K", "Core i5-13400F"),
        // Motherboards
        "Motherboards|ASUS ROG"        to listOf("ROG Maximus Z890 Apex", "ROG Maximus Z790 Hero", "ROG Strix B650E-F Gaming WiFi", "ROG Crosshair X670E Hero"),
        "Motherboards|ASUS TUF"        to listOf("TUF Gaming Z890-Pro WiFi", "TUF Gaming Z790-Plus WiFi D4", "TUF Gaming B650M-Plus WiFi"),
        "Motherboards|Gigabyte AORUS"  to listOf("Z890 AORUS Master", "Z790 AORUS Master", "B650 AORUS Elite AX", "X670E AORUS Xtreme"),
        "Motherboards|MSI MEG"         to listOf("MEG Z890 ACE", "MEG Z790 ACE", "MEG X670E ACE"),
        "Motherboards|MSI MAG"         to listOf("MAG Z890 Tomahawk WiFi", "MAG Z790 Tomahawk WiFi", "MAG B650 Tomahawk WiFi"),
        "Motherboards|ASRock"          to listOf("Z890 Taichi", "Z790 Taichi Carrara", "B650 Steel Legend WiFi"),
        "Motherboards|NZXT"            to listOf("N9 Z790", "N7 B650E", "N5 Z790"),
        // RAM
        "RAM|Corsair"    to listOf("Dominator Platinum RGB 64GB DDR5-6400", "Dominator Platinum RGB 32GB DDR5-6000", "Vengeance RGB 32GB DDR5-5600", "Vengeance LPX 32GB DDR4-3600"),
        "RAM|G.Skill"    to listOf("Trident Z5 RGB 64GB DDR5-6400", "Trident Z5 RGB 32GB DDR5-6000", "Ripjaws V 32GB DDR4-3600", "Flare X5 32GB DDR5-5600"),
        "RAM|Kingston"   to listOf("Fury Beast RGB 32GB DDR5-6000", "Fury Renegade 32GB DDR5-6400", "Fury Beast 32GB DDR4-3600", "ValueRAM 16GB DDR4-3200"),
        "RAM|Samsung"    to listOf("OEM 32GB DDR5-5600", "OEM 16GB DDR5-4800", "OEM 32GB DDR4-3200"),
        "RAM|Crucial"    to listOf("Pro Overclocking 32GB DDR5-6000", "Pro 32GB DDR5-5600", "Ballistix 32GB DDR4-3600"),
        "RAM|TeamGroup"  to listOf("T-Force Delta RGB 32GB DDR5-6000", "T-Force Vulcan 32GB DDR5-5600", "T-Force Delta RGB 32GB DDR4-3600"),
        "RAM|ADATA"      to listOf("XPG Lancer RGB 32GB DDR5-6000", "XPG Spectrix D60G 32GB DDR4-3600", "XPG Gammix D45 16GB DDR4-3200"),
        // Cooling
        "Cooling|Corsair"   to listOf("iCUE H170i Elite LCD XT", "iCUE H150i Elite LCD", "iCUE H100i Elite LCD", "iCUE AF120 RGB Slim"),
        "Cooling|NZXT"      to listOf("Kraken Elite 360 RGB", "Kraken 360 RGB", "Kraken 240 RGB", "T120 RGB"),
        "Cooling|be quiet!" to listOf("Silent Loop 3 360mm", "Dark Rock Pro 5", "Pure Rock 2 FX"),
        "Cooling|Noctua"    to listOf("NH-D15 G2", "NH-U12S Redux", "NF-A14 PWM Fan"),
        "Cooling|DeepCool"  to listOf("LT720 360mm", "AK620 Digital", "AG620 Digital"),
        "Cooling|Arctic"    to listOf("Liquid Freezer III 360", "Freezer 36 A-RGB", "P14 PWM PST"),
        "Cooling|Lian Li"   to listOf("Galahad II Trinity 360 Performance", "Galahad II 240", "Uni Fan SL-INF 120"),
        // Peripherals
        "Peripherals|Logitech"   to listOf("G Pro X Superlight 2 DEX", "G915 TKL Lightspeed", "G Pro X 60 Keyboard", "G435 Wireless Headset"),
        "Peripherals|Razer"      to listOf("DeathAdder V3 HyperSpeed", "BlackWidow V4 Pro", "Huntsman V3 Pro", "Kraken V4 Pro"),
        "Peripherals|SteelSeries" to listOf("Rival 650 Quantum Wireless", "Apex Pro TKL Wireless", "Arctis Nova Pro Wireless"),
        "Peripherals|HyperX"     to listOf("Pulsefire Haste 2 Wireless", "Alloy Origins 65", "Cloud Alpha Wireless"),
        "Peripherals|Corsair"    to listOf("M75 Air Wireless", "K70 RGB TKL", "HS80 RGB Wireless"),
        "Peripherals|ASUS ROG"   to listOf("Harpe Ace Aim Lab Edition", "Azoth Wireless Keyboard", "Delta S Wireless Headset"),
        "Peripherals|Wooting"    to listOf("Wooting 60HE+", "Wooting Two HE", "Wooting One"),
        // Storage
        "Storage|Samsung"        to listOf("990 Pro 4TB NVMe PCIe 4.0", "990 Pro 2TB NVMe PCIe 4.0", "870 EVO 4TB SATA", "870 QVO 8TB SATA"),
        "Storage|Western Digital" to listOf("Black SN850X 4TB NVMe", "Black SN850X 2TB NVMe", "Red Pro 8TB HDD", "Blue SN580 2TB NVMe"),
        "Storage|Seagate"        to listOf("FireCuda 530 4TB NVMe", "BarraCuda 4TB SATA", "Exos X20 20TB HDD"),
        "Storage|Kingston"       to listOf("KC3000 4TB NVMe PCIe 4.0", "A400 1TB SATA SSD", "NV2 4TB NVMe"),
        "Storage|Crucial"        to listOf("T705 4TB NVMe PCIe 5.0", "P5 Plus 2TB NVMe PCIe 4.0", "MX500 4TB SATA"),
        "Storage|SK Hynix"       to listOf("Platinum P51 4TB NVMe PCIe 4.0", "Gold P31 2TB NVMe", "Gold S31 4TB SATA"),
        // PSUs
        "PSUs|Corsair"       to listOf("HX1500i 80+ Platinum", "HX1200i 80+ Platinum", "HX1000i 80+ Platinum", "RM1000x 80+ Gold", "RM850x 80+ Gold", "RM750x 80+ Gold"),
        "PSUs|EVGA"          to listOf("SuperNOVA 1600 G+ 80+ Gold", "SuperNOVA 1000 G6 80+ Gold", "SuperNOVA 850 G6 80+ Gold"),
        "PSUs|Seasonic"      to listOf("PRIME TX-1600 80+ Titanium", "PRIME GX-1000 80+ Gold", "Focus GX-850 80+ Gold", "Focus GX-750 80+ Gold"),
        "PSUs|be quiet!"     to listOf("Dark Power 13 1000W", "Straight Power 12 850W 80+ Platinum", "System Power 10 750W 80+ Bronze"),
        "PSUs|MSI"           to listOf("MEG Ai1300P PCIE5 80+ Platinum", "MPG A1000G PCIE5 80+ Gold", "MAG A850GL PCIE5 80+ Gold"),
        "PSUs|Thermaltake"   to listOf("Toughpower GF3 1350W 80+ Gold", "Toughpower PF3 1050W 80+ Platinum", "Smart BM3 750W 80+ Bronze"),
        "PSUs|NZXT"          to listOf("C1500 Platinum 80+ Platinum", "C1200 Gold 80+ Gold", "C850 Gold 80+ Gold", "C750 Gold 80+ Gold")
    )

    private fun setupCategorySpinner() {
        val categories = listOf("GPUs", "CPUs", "Motherboards", "RAM", "Cooling", "Peripherals", "Storage", "PSUs")
        val spinnerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, categories)
        binding.spinnerCategory.adapter = spinnerAdapter

        binding.spinnerCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val category = categories[position]
                updateCategorySpecVisibility(category)
                updateBrandSpinner(category)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun updateBrandSpinner(category: String) {
        val brands = (brandsByCategory[category] ?: emptyList()) + OTHER_BRAND
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, brands)
        binding.spinnerBrand.adapter = adapter

        binding.spinnerBrand.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val brand = brands[position]
                binding.etCustomBrand.visibility = if (brand == OTHER_BRAND) View.VISIBLE else View.GONE
                updateModelSpinner(category, brand)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        // Trigger model update for the initial brand
        binding.etCustomBrand.visibility = View.GONE
        updateModelSpinner(category, brands[0])
    }

    private fun updateModelSpinner(category: String, brand: String) {
        val key = "$category|$brand"
        val models = (modelsByKey[key] ?: emptyList()) + OTHER_MODEL
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, models)
        binding.spinnerModel.adapter = adapter

        binding.spinnerModel.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val model = models[position]
                binding.etCustomModel.visibility = if (model == OTHER_MODEL) View.VISIBLE else View.GONE
                when (category) {
                    "Motherboards" -> updateSocketSpinnerForMotherboard(model)
                    "RAM" -> updateRamStandardSpinnerForModel(model)
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        // If the only option is "Other / Custom" (e.g. custom brand), show the field immediately
        binding.etCustomModel.visibility = if (models[0] == OTHER_MODEL) View.VISIBLE else View.GONE
        when (category) {
            "Motherboards" -> updateSocketSpinnerForMotherboard(models[0])
            "RAM" -> updateRamStandardSpinnerForModel(models[0])
        }
    }

    /**
     * Known motherboard models have one physical CPU socket. Keeping only that
     * option selected prevents a listing from being posted with an incompatible
     * socket; custom models still allow the seller to choose manually.
     */
    private fun updateSocketSpinnerForMotherboard(model: String) {
        val compatibleSockets = MotherboardSocketCompatibility.socketsForModel(model)
        binding.spinnerSocket.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            compatibleSockets
        )
    }

    /**
     * A known RAM model's DDR generation and speed are fixed. Restrict the
     * standard picker to that value; a custom model remains manually selectable.
     */
    private fun updateRamStandardSpinnerForModel(model: String) {
        val compatibleStandards = RamStandardCompatibility.standardsForModel(model)
        binding.spinnerRamStandard.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            compatibleStandards
        )
    }

    private fun updateCategorySpecVisibility(category: String) {
        // Show the outer container only if there's a relevant spec row
        val hasSpec = category in listOf("GPUs", "PSUs", "Motherboards", "RAM")
        binding.containerCategorySpecs.visibility = if (hasSpec) View.VISIBLE else View.GONE

        // Individual rows
        binding.rowSpecGpuSlot.visibility    = if (category == "GPUs")         View.VISIBLE else View.GONE
        binding.rowSpecPsuCapacity.visibility = if (category == "PSUs")         View.VISIBLE else View.GONE
        binding.rowSpecSocket.visibility      = if (category == "Motherboards") View.VISIBLE else View.GONE
        binding.rowSpecRamStandard.visibility = if (category == "RAM")          View.VISIBLE else View.GONE

        // Populate the visible spec spinner
        when (category) {
            "GPUs" -> {
                val slots = listOf("PCIe 5.0 x16", "PCIe 4.0 x16", "PCIe 3.0 x16", "PCIe 4.0 x8")
                binding.spinnerGpuSlot.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, slots)
            }
            "PSUs" -> {
                val caps = listOf("550W", "650W", "750W", "850W", "1000W", "1200W", "1600W")
                binding.spinnerPsuCapacity.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, caps)
            }
            "Motherboards" -> {
                binding.spinnerSocket.adapter = ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_spinner_dropdown_item,
                    MotherboardSocketCompatibility.allSupportedSockets
                )
            }
            "RAM" -> {
                binding.spinnerRamStandard.adapter = ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_spinner_dropdown_item,
                    RamStandardCompatibility.allSupportedStandards
                )
            }
        }
    }


    private fun setupConditionSelector() {
        updateConditionUI("Used")

        binding.btnCondNew.setOnClickListener { updateConditionUI("New") }
        binding.btnCondUsed.setOnClickListener { updateConditionUI("Used") }
        binding.btnCondRefurb.setOnClickListener { updateConditionUI("Refurbished") }
    }

    private fun updateConditionUI(condition: String) {
        selectedCondition = condition
        val context = requireContext()

        // Reset all to unselected
        binding.btnCondNew.setBackgroundResource(R.drawable.bg_dark_input)
        binding.btnCondNew.setTextColor(ContextCompat.getColor(context, R.color.text_secondary))

        binding.btnCondUsed.setBackgroundResource(R.drawable.bg_dark_input)
        binding.btnCondUsed.setTextColor(ContextCompat.getColor(context, R.color.text_secondary))

        binding.btnCondRefurb.setBackgroundResource(R.drawable.bg_dark_input)
        binding.btnCondRefurb.setTextColor(ContextCompat.getColor(context, R.color.text_secondary))

        // Set active
        when (condition) {
            "New" -> {
                binding.btnCondNew.setBackgroundResource(R.drawable.bg_card_active)
                binding.btnCondNew.setTextColor(ContextCompat.getColor(context, R.color.accent_cyan))
            }
            "Used" -> {
                binding.btnCondUsed.setBackgroundResource(R.drawable.bg_card_active)
                binding.btnCondUsed.setTextColor(ContextCompat.getColor(context, R.color.accent_cyan))
            }
            "Refurbished" -> {
                binding.btnCondRefurb.setBackgroundResource(R.drawable.bg_card_active)
                binding.btnCondRefurb.setTextColor(ContextCompat.getColor(context, R.color.accent_cyan))
            }
        }
    }

    private fun setupPhotoSlots() {
        binding.slotAddPhoto1.setOnClickListener {
            handleSlotClick(1)
        }
        binding.slotAddPhoto2.setOnClickListener {
            handleSlotClick(2)
        }
        binding.slotAddPhoto3.setOnClickListener {
            handleSlotClick(3)
        }

        binding.btnViewPhoto1.setOnClickListener {
            photoPaths[1]?.let { showEnlargedPhotoDialog(it, "Main Product Photo") }
        }
        binding.btnViewPhoto2.setOnClickListener {
            photoPaths[2]?.let { showEnlargedPhotoDialog(it, "Secondary Angle Photo") }
        }
        binding.btnViewPhoto3.setOnClickListener {
            photoPaths[3]?.let { showEnlargedPhotoDialog(it, "Benchmark / Spec Photo") }
        }

        binding.btnRemovePhoto1.setOnClickListener {
            photoPaths.remove(1)
            updateSlotUI(1, null)
        }
        binding.btnRemovePhoto2.setOnClickListener {
            photoPaths.remove(2)
            updateSlotUI(2, null)
        }
        binding.btnRemovePhoto3.setOnClickListener {
            photoPaths.remove(3)
            updateSlotUI(3, null)
        }
    }

    private fun launchCameraCapture() {
        try {
            val context = requireContext()
            val dir = File(context.filesDir, "listing_images")
            if (!dir.exists()) dir.mkdirs()
            val file = File(dir, "cam_${System.currentTimeMillis()}_${(1000..9999).random()}.jpg")
            val authority = "${context.packageName}.fileprovider"
            val uri = FileProvider.getUriForFile(context, authority, file)
            pendingCameraFile = file
            pendingCameraUri = uri
            cameraLauncher.launch(uri)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "Camera not available: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleSlotClick(slot: Int) {
        val existingPath = photoPaths[slot]
        if (existingPath != null) {
            val options = arrayOf("View Full Size", "Take New Photo (Camera)", "Choose from Gallery", "Remove Photo")
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Photo Options (Slot $slot)")
                .setItems(options) { _, which ->
                    when (which) {
                        0 -> showEnlargedPhotoDialog(existingPath, if (slot == 1) "Main Product Photo" else "Photo (Slot $slot)")
                        1 -> {
                            currentTargetSlot = slot
                            launchCameraCapture()
                        }
                        2 -> {
                            currentTargetSlot = slot
                            try {
                                galleryLauncher.launch("image/*")
                            } catch (e: Exception) {
                                Toast.makeText(requireContext(), "Gallery not available: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                        3 -> {
                            photoPaths.remove(slot)
                            updateSlotUI(slot, null)
                        }
                    }
                }
                .show()
        } else {
            showPhotoPickerDialog(slot)
        }
    }

    private fun showPhotoPickerDialog(slot: Int) {
        currentTargetSlot = slot
        val options = arrayOf("Take Photo (Camera)", "Choose from Gallery")
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(if (slot == 1) "Add Main Product Photo" else "Add Photo (Slot $slot)")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> {
                        launchCameraCapture()
                    }
                    1 -> {
                        try {
                            galleryLauncher.launch("image/*")
                        } catch (e: Exception) {
                            Toast.makeText(requireContext(), "Gallery not available: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            .show()
    }

    private fun showEnlargedPhotoDialog(imagePath: String, title: String) {
        val dialog = android.app.Dialog(requireContext(), android.R.style.Theme_Black_NoTitleBar_Fullscreen)
        dialog.setContentView(R.layout.dialog_image_viewer)

        val ivFull = dialog.findViewById<android.widget.ImageView>(R.id.ivFullImage)
        val btnClose = dialog.findViewById<android.widget.ImageView>(R.id.btnCloseViewer)
        val tvTitle = dialog.findViewById<android.widget.TextView>(R.id.tvViewerTitle)
        val topBar = dialog.findViewById<View>(R.id.topBarViewer)

        tvTitle.text = title
        com.example.rigswap.util.InsetUtils.applyTopSystemBarInsets(topBar)

        val file = File(imagePath)
        if (file.exists() && file.isFile) {
            val bitmap = BitmapFactory.decodeFile(file.absolutePath)
            if (bitmap != null) {
                ivFull.setImageBitmap(bitmap)
            } else {
                com.example.rigswap.ui.adapter.ProductGridAdapter.bindProductImage(ivFull, imagePath)
            }
        } else {
            com.example.rigswap.ui.adapter.ProductGridAdapter.bindProductImage(ivFull, imagePath)
        }

        btnClose.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun updateSlotUI(slot: Int, path: String?) {
        val context = context ?: return
        val strokeActive = ContextCompat.getColor(context, R.color.accent_cyan)
        val strokeDefault = ContextCompat.getColor(context, R.color.bg_input_stroke)

        when (slot) {
            1 -> {
                if (path != null) {
                    binding.layoutPlaceholder1.visibility = View.GONE
                    binding.ivPhoto1.visibility = View.VISIBLE
                    binding.btnRemovePhoto1.visibility = View.VISIBLE
                    binding.btnViewPhoto1.visibility = View.VISIBLE
                    binding.badgeMainPhoto.visibility = View.VISIBLE
                    binding.tvTapToEnlargeHint1.visibility = View.VISIBLE
                    binding.slotAddPhoto1.strokeColor = strokeActive
                    binding.ivPhoto1.setImageURI(Uri.fromFile(File(path)))
                } else {
                    binding.layoutPlaceholder1.visibility = View.VISIBLE
                    binding.ivPhoto1.visibility = View.GONE
                    binding.btnRemovePhoto1.visibility = View.GONE
                    binding.btnViewPhoto1.visibility = View.GONE
                    binding.badgeMainPhoto.visibility = View.GONE
                    binding.tvTapToEnlargeHint1.visibility = View.GONE
                    binding.slotAddPhoto1.strokeColor = strokeDefault
                    binding.ivPhoto1.setImageDrawable(null)
                }
            }
            2 -> {
                if (path != null) {
                    binding.layoutPlaceholder2.visibility = View.GONE
                    binding.ivPhoto2.visibility = View.VISIBLE
                    binding.btnRemovePhoto2.visibility = View.VISIBLE
                    binding.btnViewPhoto2.visibility = View.VISIBLE
                    binding.slotAddPhoto2.strokeColor = strokeActive
                    binding.ivPhoto2.setImageURI(Uri.fromFile(File(path)))
                } else {
                    binding.layoutPlaceholder2.visibility = View.VISIBLE
                    binding.ivPhoto2.visibility = View.GONE
                    binding.btnRemovePhoto2.visibility = View.GONE
                    binding.btnViewPhoto2.visibility = View.GONE
                    binding.slotAddPhoto2.strokeColor = strokeDefault
                    binding.ivPhoto2.setImageDrawable(null)
                }
            }
            3 -> {
                if (path != null) {
                    binding.layoutPlaceholder3.visibility = View.GONE
                    binding.ivPhoto3.visibility = View.VISIBLE
                    binding.btnRemovePhoto3.visibility = View.VISIBLE
                    binding.btnViewPhoto3.visibility = View.VISIBLE
                    binding.slotAddPhoto3.strokeColor = strokeActive
                    binding.ivPhoto3.setImageURI(Uri.fromFile(File(path)))
                } else {
                    binding.layoutPlaceholder3.visibility = View.VISIBLE
                    binding.ivPhoto3.visibility = View.GONE
                    binding.btnRemovePhoto3.visibility = View.GONE
                    binding.btnViewPhoto3.visibility = View.GONE
                    binding.slotAddPhoto3.strokeColor = strokeDefault
                    binding.ivPhoto3.setImageDrawable(null)
                }
            }
        }
    }

    private fun saveImageToInternalStorage(uri: Uri): String? {
        return try {
            val context = context ?: return null
            val dir = File(context.filesDir, "listing_images")
            if (!dir.exists()) dir.mkdirs()
            val file = File(dir, "gallery_${System.currentTimeMillis()}_${(1000..9999).random()}.jpg")

            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                FileOutputStream(file).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            } ?: return null

            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun handlePostListing() {
        val title = binding.etProductTitle.text.toString().trim()
        val category = binding.spinnerCategory.selectedItem?.toString() ?: "GPUs"
        val priceStr = binding.etPrice.text.toString().trim()
        var brand = binding.spinnerBrand.selectedItem?.toString() ?: ""
        var model = binding.spinnerModel.selectedItem?.toString() ?: ""
        val desc = binding.etDescription.text.toString().trim()

        if (title.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter a product title", Toast.LENGTH_SHORT).show()
            return
        }

        // Resolve custom "Other" values
        if (brand == OTHER_BRAND) {
            brand = binding.etCustomBrand.text.toString().trim()
            if (brand.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter a brand name", Toast.LENGTH_SHORT).show()
                return
            }
        }
        if (model == OTHER_MODEL) {
            model = binding.etCustomModel.text.toString().trim()
            if (model.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter a model name", Toast.LENGTH_SHORT).show()
                return
            }
        }

        val price = priceStr.toLongOrNull() ?: 250000L

        val condCode = when (selectedCondition) {
            "New" -> "NEW"
            "Refurbished" -> "REFURB"
            else -> "USED"
        }

        // Use user's selected photo if available; otherwise fallback to default category drawable
        val primaryPhotoPath = photoPaths[1] ?: photoPaths[2] ?: photoPaths[3]
        val imageRes = primaryPhotoPath ?: when (category) {
            "GPUs"         -> "img_rtx4090"
            "CPUs"         -> "img_ryzen7"
            "Cooling"      -> "img_h150i"
            "Peripherals"  -> "img_wooting"
            "Storage"      -> "img_samsung990"
            "PSUs"         -> "img_rm850x"
            "Motherboards" -> "img_rtx4090"  // fallback — no mobo-specific drawable
            "RAM"          -> "img_rtx4090"  // fallback — no ram-specific drawable
            else           -> "img_rtx4090"
        }

        // Read category-specific spec spinner value
        val gpuSlot    = binding.spinnerGpuSlot.selectedItem?.toString() ?: "PCIe 4.0 x16"
        val psuCap     = binding.spinnerPsuCapacity.selectedItem?.toString() ?: "850W"
        val socket     = binding.spinnerSocket.selectedItem?.toString() ?: "AM5 (AMD Ryzen 7000/9000)"
        val ramStd     = binding.spinnerRamStandard.selectedItem?.toString() ?: "DDR5-5600"

        // Map category → (memory, tdp, suggestedPsu, powerConnector)
        val (mem, tdpVal, psuVal, pwrVal) = when (category) {
            "GPUs"  -> listOf("16GB GDDR6X", "285W", "750W+", gpuSlot)
            "CPUs"  -> listOf("L3 3D V-Cache", "120W", "650W+", "AM5 LGA 1718")
            "Cooling"     -> listOf("360mm Radiator", "250W Dissipation", "N/A", "4-Pin PWM")
            "Peripherals" -> listOf("Analog Hall Effect", "5W", "N/A", "USB Type-C")
            "Storage"     -> listOf("2TB NVMe PCIe 4.0", "8.5W", "N/A", "M.2 2280")
            "PSUs"         -> listOf(psuCap, psuCap, "80+ Gold Efficiency", "ATX 3.0 PCIe 5.0")
            "Motherboards" -> listOf("ATX Form Factor", "N/A", "N/A", socket)
            "RAM"          -> listOf(ramStd, "N/A", "N/A", "DIMM")
            else           -> listOf("Standard", "N/A", "N/A", "Standard")
        }

        val newProduct = Product(
            id = 0,
            title = title,
            category = category,
            priceLkr = price,
            condition = condCode,
            brand = brand,
            model = model,
            memory = mem,
            tdp = tdpVal,
            suggestedPsu = psuVal,
            powerConnector = pwrVal,
            description = desc,
            location = "Colombo, Sri Lanka",
            rating = 5.0f,
            imageResName = imageRes,
            imageResName2 = photoPaths[2] ?: "",
            imageResName3 = photoPaths[3] ?: "",
            sellerName = "RigBuilder_LK",
            sellerRating = 5.0f,
            sellerSales = 15,
            isCompatible = true,
            isFeatured = true
        )

        val newId = repository.insertProduct(newProduct)
        if (newId > 0) {
            Toast.makeText(requireContext(), "Listing \"$title\" published successfully!", Toast.LENGTH_LONG).show()
            // Reset form
            binding.etProductTitle.setText("")
            binding.etPrice.setText("")
            binding.etDescription.setText("")
            binding.etCustomBrand.setText("")
            binding.etCustomModel.setText("")
            photoPaths.clear()
            updateSlotUI(1, null)
            updateSlotUI(2, null)
            updateSlotUI(3, null)
        } else {
            Toast.makeText(requireContext(), "Failed to save listing to local database", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
