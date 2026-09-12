package com.example.rigswap.data.db

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class RigswapDbHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "rigswap.db"
        const val DATABASE_VERSION = 5

        // Tables
        const val TABLE_PRODUCTS = "products"
        const val TABLE_SAVED_RIG = "saved_rig"
        const val TABLE_CART = "cart_items"
        const val TABLE_CHAT = "chat_messages"
        const val TABLE_TRANSACTIONS = "seller_transactions"
        const val TABLE_USER = "users"

        // Common Column
        const val COL_ID = "id"

        // Products Columns
        const val COL_PROD_TITLE = "title"
        const val COL_PROD_CATEGORY = "category"
        const val COL_PROD_PRICE = "price_lkr"
        const val COL_PROD_CONDITION = "condition"
        const val COL_PROD_BRAND = "brand"
        const val COL_PROD_MODEL = "model"
        const val COL_PROD_MEMORY = "memory"
        const val COL_PROD_TDP = "tdp"
        const val COL_PROD_SUGGESTED_PSU = "suggested_psu"
        const val COL_PROD_POWER_CONN = "power_connector"
        const val COL_PROD_DESCRIPTION = "description"
        const val COL_PROD_LOCATION = "location"
        const val COL_PROD_RATING = "rating"
        const val COL_PROD_IMAGE = "image_res"
        const val COL_PROD_IMAGE2 = "image_res2"
        const val COL_PROD_IMAGE3 = "image_res3"
        const val COL_PROD_SELLER_NAME = "seller_name"
        const val COL_PROD_SELLER_RATING = "seller_rating"
        const val COL_PROD_SELLER_SALES = "seller_sales"
        const val COL_PROD_IS_COMPATIBLE = "is_compatible"
        const val COL_PROD_IS_FEATURED = "is_featured"

        // Saved Rig Columns
        const val COL_RIG_NAME = "name"
        const val COL_RIG_SUBTITLE = "subtitle"
        const val COL_RIG_SOCKET = "socket"
        const val COL_RIG_RAM = "ram_standard"
        const val COL_RIG_GPU_SLOT = "gpu_slot"
        const val COL_RIG_PSU = "psu_capacity"
        const val COL_RIG_HEALTH = "health_score"

        // Cart Columns
        const val COL_CART_PRODUCT_ID = "product_id"
        const val COL_CART_QUANTITY = "quantity"

        // Chat Columns
        const val COL_CHAT_PROD_ID = "product_id"
        const val COL_CHAT_SENDER = "sender_name"
        const val COL_CHAT_MESSAGE = "message"
        const val COL_CHAT_TIMESTAMP = "timestamp"
        const val COL_CHAT_IS_ME = "is_me"

        // Transaction Columns
        const val COL_TX_TITLE = "title"
        const val COL_TX_ORDER_NUM = "order_number"
        const val COL_TX_PRICE = "price_str"
        const val COL_TX_STATUS = "status"

        // User Columns
        const val COL_USER_EMAIL = "email"
        const val COL_USER_NAME = "username"
        const val COL_USER_LISTINGS = "active_listings"
        const val COL_USER_SALES = "total_sales"
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE $TABLE_PRODUCTS (
                $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_PROD_TITLE TEXT NOT NULL,
                $COL_PROD_CATEGORY TEXT NOT NULL,
                $COL_PROD_PRICE INTEGER NOT NULL,
                $COL_PROD_CONDITION TEXT NOT NULL,
                $COL_PROD_BRAND TEXT,
                $COL_PROD_MODEL TEXT,
                $COL_PROD_MEMORY TEXT,
                $COL_PROD_TDP TEXT,
                $COL_PROD_SUGGESTED_PSU TEXT,
                $COL_PROD_POWER_CONN TEXT,
                $COL_PROD_DESCRIPTION TEXT,
                $COL_PROD_LOCATION TEXT,
                $COL_PROD_RATING REAL,
                $COL_PROD_IMAGE TEXT,
                $COL_PROD_IMAGE2 TEXT,
                $COL_PROD_IMAGE3 TEXT,
                $COL_PROD_SELLER_NAME TEXT,
                $COL_PROD_SELLER_RATING REAL,
                $COL_PROD_SELLER_SALES INTEGER,
                $COL_PROD_IS_COMPATIBLE INTEGER DEFAULT 1,
                $COL_PROD_IS_FEATURED INTEGER DEFAULT 1
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE $TABLE_SAVED_RIG (
                $COL_ID INTEGER PRIMARY KEY,
                $COL_RIG_NAME TEXT,
                $COL_RIG_SUBTITLE TEXT,
                $COL_RIG_SOCKET TEXT,
                $COL_RIG_RAM TEXT,
                $COL_RIG_GPU_SLOT TEXT,
                $COL_RIG_PSU TEXT,
                $COL_RIG_HEALTH TEXT
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE $TABLE_CART (
                $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_CART_PRODUCT_ID INTEGER NOT NULL,
                $COL_CART_QUANTITY INTEGER DEFAULT 1
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE $TABLE_CHAT (
                $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_CHAT_PROD_ID INTEGER,
                $COL_CHAT_SENDER TEXT,
                $COL_CHAT_MESSAGE TEXT,
                $COL_CHAT_TIMESTAMP TEXT,
                $COL_CHAT_IS_ME INTEGER
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE $TABLE_TRANSACTIONS (
                $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_TX_TITLE TEXT,
                $COL_TX_ORDER_NUM TEXT,
                $COL_TX_PRICE TEXT,
                $COL_TX_STATUS TEXT
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE $TABLE_USER (
                $COL_ID INTEGER PRIMARY KEY,
                $COL_USER_EMAIL TEXT,
                $COL_USER_NAME TEXT,
                $COL_USER_LISTINGS INTEGER,
                $COL_USER_SALES INTEGER
            )
        """.trimIndent())

        seedInitialData(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_PRODUCTS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_SAVED_RIG")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_CART")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_CHAT")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_TRANSACTIONS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USER")
        onCreate(db)
    }

    private data class SeedItem(
        val title: String,
        val category: String,
        val priceLkr: Long,
        val condition: String,
        val brand: String,
        val model: String,
        val memory: String,
        val tdp: String,
        val suggestedPsu: String,
        val powerConn: String,
        val description: String,
        val location: String,
        val rating: Float,
        val imageRes: String,
        val sellerName: String,
        val sellerRating: Float,
        val sellerSales: Int,
        val isCompatible: Int,
        val isFeatured: Int
    )

    private fun seedInitialData(db: SQLiteDatabase) {
        // 1. Seed User
        val userValues = ContentValues().apply {
            put(COL_ID, 1)
            put(COL_USER_EMAIL, "gamer@rigswap.gg")
            put(COL_USER_NAME, "RigBuilder_LK")
            put(COL_USER_LISTINGS, 14)
            put(COL_USER_SALES, 486500)
        }
        db.insert(TABLE_USER, null, userValues)

        // 2. Seed Saved Rig
        val rigValues = ContentValues().apply {
            put(COL_ID, 1)
            put(COL_RIG_NAME, "My Main Battlestation")
            put(COL_RIG_SUBTITLE, "Mini-ITX / High FPS Build")
            put(COL_RIG_SOCKET, "Socket AM5")
            put(COL_RIG_RAM, "DDR5 dual-channel")
            put(COL_RIG_GPU_SLOT, "PCIe Gen 5.0 x16")
            put(COL_RIG_PSU, "850 Watts Modular")
            put(COL_RIG_HEALTH, "98% OK")
        }
        db.insert(TABLE_SAVED_RIG, null, rigValues)

        // 3. Seed Products
        val products = listOf(
            // GPUs
            SeedItem("ASUS ROG Strix RTX 4090 24GB", "GPUs", 950000L, "NEW", "ASUS ROG", "Strix RTX 4090", "24GB GDDR6X", "450W", "850W+", "16-pin (12VHPWR)", "Flagship enthusiast GPU. Requires 850W+ PSU and PCIe 4.0/5.0 slot.", "Malabe, Colombo", 5.0f, "img_rtx4090", "SolderSlinger_99", 4.9f, 48, 1, 1),
            SeedItem("RTX 4090 Founders Edition", "GPUs", 900000L, "NEW", "NVIDIA", "RTX 4090 FE", "24GB GDDR6X", "450W", "850W+", "16-pin (12VHPWR)", "Original NVIDIA Founders Edition. Mint sealed box. Requires 850W+ PSU.", "Malabe, Colombo", 5.0f, "img_rtx4090", "SolderSlinger_99", 4.9f, 48, 1, 1),
            SeedItem("MSI Gaming X Slim RTX 4070 Super", "GPUs", 285000L, "NEW", "MSI", "RTX 4070 Super 12G", "12GB GDDR6X", "220W", "650W+", "16-pin (12VHPWR)", "High efficiency gaming GPU. Compatible with 650W+ power supplies.", "Colombo 4, Colombo", 4.9f, "img_rtx4090", "TechStoreLK", 4.8f, 65, 1, 1),
            SeedItem("ASUS Dual RTX 4060 Ti 8GB", "GPUs", 165000L, "USED", "ASUS", "RTX 4060 Ti OC", "8GB GDDR6", "160W", "550W+", "8-pin PCIe", "Compact dual-fan graphics card. Highly efficient, runs on 550W+ PSU.", "Nugegoda, Colombo", 4.7f, "img_rtx4090", "SolderSlinger_99", 4.9f, 48, 1, 0),

            // CPUs
            SeedItem("Intel Core Ultra 7 265K (LGA 1851)", "CPUs", 320000L, "NEW", "Intel", "Core Ultra 7 265K", "30MB Cache", "125W", "750W+", "LGA 1851", "Arrow Lake-S flagship 20-core processor. Requires Intel LGA 1851 motherboard and DDR5 memory.", "Colombo 3, Colombo", 5.0f, "img_ryzen7", "TechStoreLK", 4.9f, 54, 1, 1),
            SeedItem("Ryzen 7 7800X3D (AM5)", "CPUs", 300000L, "USED", "AMD", "Ryzen 7 7800X3D", "96MB 3D V-Cache", "120W", "650W+", "Socket AM5", "The world's best gaming processor. Fits Socket AM5 motherboards.", "Colombo 7, Colombo", 4.8f, "img_ryzen7", "TechStoreLK", 4.9f, 112, 1, 1),
            SeedItem("Intel Core i7-14700K (LGA 1700)", "CPUs", 295000L, "NEW", "Intel", "Core i7-14700K", "33MB Smart Cache", "125W", "750W+", "LGA 1700", "20 cores (8P + 12E). Compatible with Intel LGA 1700 motherboards.", "Colombo 3, Colombo", 4.9f, "img_ryzen7", "TechStoreLK", 4.9f, 112, 1, 1),
            SeedItem("Ryzen 7 5800X3D (AM4)", "CPUs", 185000L, "USED", "AMD", "Ryzen 7 5800X3D", "96MB 3D V-Cache", "105W", "650W+", "Socket AM4", "Ultimate drop-in gaming upgrade for Socket AM4 motherboard platforms.", "Kandy, Central", 4.9f, "img_ryzen7", "OverclockLab", 4.7f, 29, 1, 0),

            // Cooling
            SeedItem("Corsair H150i Elite 360mm", "Cooling", 30000L, "USED", "Corsair", "H150i Elite LCD", "360mm Radiator", "250W Dissipation", "N/A", "AM5 / LGA 1700 / AM4", "Triple magnetic levitation fans with LCD screen. Universal AM5 & LGA 1700 brackets included.", "Ambalantota, Hambantota", 5.0f, "img_h150i", "OverclockLab", 4.7f, 29, 1, 1),
            SeedItem("Noctua NH-D15 chromax", "Cooling", 18500L, "USED", "Noctua", "NH-D15 chromax.black", "Dual Tower 140mm", "250W+", "N/A", "AM5 / LGA 1700", "Legendary dual-tower quiet cooler with AM5 and LGA 1700 mounting kits.", "Colombo 3, Colombo", 4.9f, "img_noctua", "SolderSlinger_99", 4.9f, 48, 1, 0),
            SeedItem("NZXT H7 Flow Mid-Tower", "Cooling", 28000L, "USED", "NZXT", "H7 Flow 2024", "Perforated Mesh", "N/A", "N/A", "ATX / ITX Chassis", "Excellent thermal airflow with tempered glass. Fits all standard ATX/ITX builds.", "Rajagiriya, Colombo", 4.7f, "img_nzxt_case", "CaseModsLK", 4.6f, 19, 1, 0),
            SeedItem("Arctic P12 PWM PST 5-Pack", "Cooling", 8500L, "NEW", "Arctic", "P12 PWM PST Value Pack", "5x 120mm Fans", "N/A", "N/A", "4-Pin PWM PST", "Universal high pressure case fans compatible with any chassis.", "Moratuwa, Colombo", 4.9f, "img_arctic_fans", "SilentCoolingLK", 4.9f, 84, 1, 0),

            // Storage
            SeedItem("Samsung 990 Pro 2TB NVMe", "Storage", 65000L, "NEW", "Samsung", "990 PRO PCIe 4.0", "2TB V-NAND 3-bit", "N/A", "N/A", "M.2 2280 PCIe 4.0", "Blazing fast 7450 MB/s read speed. Fits PCIe M.2 slots on AM4, AM5, and LGA1700.", "Kandy, Central", 5.0f, "img_samsung990", "StorageWorld", 5.0f, 62, 1, 0),
            SeedItem("Crucial T700 1TB PCIe 5.0 NVMe", "Storage", 85000L, "NEW", "Crucial", "T700 Gen 5", "1TB Micron 232-Layer", "N/A", "N/A", "M.2 2280 PCIe 5.0", "Extreme PCIe Gen 5 speeds up to 11,700 MB/s. Compatible with PCIe 5.0 motherboards.", "Colombo 3, Colombo", 5.0f, "img_samsung990", "StorageWorld", 5.0f, 62, 1, 0),

            // PSUs
            SeedItem("Corsair RM850x 850W Gold", "PSUs", 42000L, "NEW", "Corsair", "RM850x Gold", "N/A", "850W Modular", "80 Plus Gold", "ATX 3.0 PCIe 5", "Zero RPM fan mode, high quality Japanese capacitors. Fits standard ATX chassis.", "Nugegoda, Colombo", 4.9f, "img_rm850x", "PowerSupplySL", 4.8f, 35, 1, 0),
            SeedItem("Seasonic Focus GX-650 650W", "PSUs", 29000L, "NEW", "Seasonic", "Focus GX-650", "N/A", "650W Modular", "80 Plus Gold", "ATX / PCIe", "Reliable 650W Gold-rated power supply ideal for mid-tier gaming setups.", "Colombo 5, Colombo", 4.8f, "img_rm850x", "PowerSupplySL", 4.8f, 35, 1, 0),
            SeedItem("Corsair RM1000x 1000W Gold", "PSUs", 58000L, "NEW", "Corsair", "RM1000x Shift", "N/A", "1000W Modular", "80 Plus Gold", "ATX 3.0 PCIe 5", "High wattage flagship PSU for power-hungry multi-core CPUs and RTX 4090.", "Colombo 7, Colombo", 5.0f, "img_rm850x", "PowerSupplySL", 4.8f, 35, 1, 0),
            SeedItem("be quiet! Dark Power 1200W", "PSUs", 72000L, "NEW", "be quiet!", "Dark Power Pro 12", "N/A", "1200W Modular", "80 Plus Titanium", "ATX 3.0 PCIe 5", "Flagship 1200W Titanium power supply for extreme overclocking and multi-GPU setups.", "Colombo 7, Colombo", 5.0f, "img_rm850x", "PowerSupplySL", 4.9f, 42, 1, 0),

            // Peripherals (Note: No TDP, No Memory, No Suggested PSU!)
            SeedItem("Wooting 60HE Custom", "Peripherals", 15000L, "REFURB", "Wooting", "60HE Analog", "N/A", "N/A", "N/A", "USB Type-C", "Ultra fast analog gaming keyboard with rapid trigger and Lekker hall-effect switches.", "Kaduwela, Malabe", 4.6f, "img_wooting", "PeripheralsHub", 4.8f, 73, 1, 1),
            SeedItem("Logitech G Pro X Superlight 2", "Peripherals", 38000L, "NEW", "Logitech G", "Superlight 2 Wireless", "N/A", "N/A", "N/A", "LIGHTSPEED Wireless / USB-C", "60g ultralight esports wireless gaming mouse with HERO 2 sensor and 4K polling.", "Colombo 3, Colombo", 4.9f, "img_wooting", "PeripheralsHub", 4.8f, 73, 1, 0)
        )

        for (p in products) {
            val cv = ContentValues().apply {
                put(COL_PROD_TITLE, p.title)
                put(COL_PROD_CATEGORY, p.category)
                put(COL_PROD_PRICE, p.priceLkr)
                put(COL_PROD_CONDITION, p.condition)
                put(COL_PROD_BRAND, p.brand)
                put(COL_PROD_MODEL, p.model)
                put(COL_PROD_MEMORY, p.memory)
                put(COL_PROD_TDP, p.tdp)
                put(COL_PROD_SUGGESTED_PSU, p.suggestedPsu)
                put(COL_PROD_POWER_CONN, p.powerConn)
                put(COL_PROD_DESCRIPTION, p.description)
                put(COL_PROD_LOCATION, p.location)
                put(COL_PROD_RATING, p.rating)
                put(COL_PROD_IMAGE, p.imageRes)
                put(COL_PROD_SELLER_NAME, p.sellerName)
                put(COL_PROD_SELLER_RATING, p.sellerRating)
                put(COL_PROD_SELLER_SALES, p.sellerSales)
                put(COL_PROD_IS_COMPATIBLE, p.isCompatible)
                put(COL_PROD_IS_FEATURED, p.isFeatured)
            }
            db.insert(TABLE_PRODUCTS, null, cv)
        }

        // 4. Seed Initial Cart (matching Figure 6: Corsair H150i + Ryzen 7 7800X3D)
        val cart1 = ContentValues().apply {
            put(COL_CART_PRODUCT_ID, 4) // H150i
            put(COL_CART_QUANTITY, 1)
        }
        val cart2 = ContentValues().apply {
            put(COL_CART_PRODUCT_ID, 3) // Ryzen 7
            put(COL_CART_QUANTITY, 1)
        }
        db.insert(TABLE_CART, null, cart1)
        db.insert(TABLE_CART, null, cart2)

        // 5. Seed Initial Chat Messages (matching Figure 7 / Image 2)
        val msg1 = ContentValues().apply {
            put(COL_CHAT_PROD_ID, 1)
            put(COL_CHAT_SENDER, "SolderSlinger_99")
            put(COL_CHAT_MESSAGE, "Hey! Yes, the 4090 is still available. Never mined on, only used for casual Cyberpunk.")
            put(COL_CHAT_TIMESTAMP, "10:14 AM")
            put(COL_CHAT_IS_ME, 0)
        }
        val msg2 = ContentValues().apply {
            put(COL_CHAT_PROD_ID, 1)
            put(COL_CHAT_SENDER, "Me")
            put(COL_CHAT_MESSAGE, "Perfect. Does it come with the native 16-pin adapter?")
            put(COL_CHAT_TIMESTAMP, "10:15 AM")
            put(COL_CHAT_IS_ME, 1)
        }
        val msg3 = ContentValues().apply {
            put(COL_CHAT_PROD_ID, 1)
            put(COL_CHAT_SENDER, "SolderSlinger_99")
            put(COL_CHAT_MESSAGE, "Yes, standard adapter + original box is included in the sale.")
            put(COL_CHAT_TIMESTAMP, "10:16 AM")
            put(COL_CHAT_IS_ME, 0)
        }
        db.insert(TABLE_CHAT, null, msg1)
        db.insert(TABLE_CHAT, null, msg2)
        db.insert(TABLE_CHAT, null, msg3)

        // 6. Seed Transactions (matching Figure 9)
        val tx1 = ContentValues().apply {
            put(COL_TX_TITLE, "Custom RGB Fan Controller")
            put(COL_TX_ORDER_NUM, "Order #TX-4919 • $45")
            put(COL_TX_PRICE, "$45")
            put(COL_TX_STATUS, "SHIPPED")
        }
        val tx2 = ContentValues().apply {
            put(COL_TX_TITLE, "EK-Quantum AM4 CPU Block")
            put(COL_TX_ORDER_NUM, "Order #TX-4012 • $80")
            put(COL_TX_PRICE, "$80")
            put(COL_TX_STATUS, "DELIVERED")
        }
        val tx3 = ContentValues().apply {
            put(COL_TX_TITLE, "Custom Cable Extension Mod")
            put(COL_TX_ORDER_NUM, "Order #TX-4890 • $35")
            put(COL_TX_PRICE, "$35")
            put(COL_TX_STATUS, "PENDING")
        }
        db.insert(TABLE_TRANSACTIONS, null, tx1)
        db.insert(TABLE_TRANSACTIONS, null, tx2)
        db.insert(TABLE_TRANSACTIONS, null, tx3)
    }
}
