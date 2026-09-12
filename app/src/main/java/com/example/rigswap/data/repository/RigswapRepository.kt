package com.example.rigswap.data.repository

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import com.example.rigswap.data.db.RigswapDbHelper
import com.example.rigswap.data.model.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RigswapRepository private constructor(context: Context) {

    private val dbHelper = RigswapDbHelper(context.applicationContext)

    companion object {
        @Volatile
        private var INSTANCE: RigswapRepository? = null

        fun getInstance(context: Context): RigswapRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: RigswapRepository(context).also { INSTANCE = it }
            }
        }
    }

    // --- Products ---

    private fun mapCursorToProduct(cursor: Cursor): Product {
        return Product(
            id = cursor.getLong(cursor.getColumnIndexOrThrow(RigswapDbHelper.COL_ID)),
            title = cursor.getString(cursor.getColumnIndexOrThrow(RigswapDbHelper.COL_PROD_TITLE)),
            category = cursor.getString(cursor.getColumnIndexOrThrow(RigswapDbHelper.COL_PROD_CATEGORY)),
            priceLkr = cursor.getLong(cursor.getColumnIndexOrThrow(RigswapDbHelper.COL_PROD_PRICE)),
            condition = cursor.getString(cursor.getColumnIndexOrThrow(RigswapDbHelper.COL_PROD_CONDITION)),
            brand = cursor.getString(cursor.getColumnIndexOrThrow(RigswapDbHelper.COL_PROD_BRAND)) ?: "",
            model = cursor.getString(cursor.getColumnIndexOrThrow(RigswapDbHelper.COL_PROD_MODEL)) ?: "",
            memory = cursor.getString(cursor.getColumnIndexOrThrow(RigswapDbHelper.COL_PROD_MEMORY)) ?: "N/A",
            tdp = cursor.getString(cursor.getColumnIndexOrThrow(RigswapDbHelper.COL_PROD_TDP)) ?: "N/A",
            suggestedPsu = cursor.getString(cursor.getColumnIndexOrThrow(RigswapDbHelper.COL_PROD_SUGGESTED_PSU)) ?: "N/A",
            powerConnector = cursor.getString(cursor.getColumnIndexOrThrow(RigswapDbHelper.COL_PROD_POWER_CONN)) ?: "N/A",
            description = cursor.getString(cursor.getColumnIndexOrThrow(RigswapDbHelper.COL_PROD_DESCRIPTION)) ?: "",
            location = cursor.getString(cursor.getColumnIndexOrThrow(RigswapDbHelper.COL_PROD_LOCATION)) ?: "Colombo, Sri Lanka",
            rating = cursor.getFloat(cursor.getColumnIndexOrThrow(RigswapDbHelper.COL_PROD_RATING)),
            imageResName = cursor.getString(cursor.getColumnIndexOrThrow(RigswapDbHelper.COL_PROD_IMAGE)) ?: "img_rtx4090",
            imageResName2 = cursor.getString(cursor.getColumnIndexOrThrow(RigswapDbHelper.COL_PROD_IMAGE2)) ?: "",
            imageResName3 = cursor.getString(cursor.getColumnIndexOrThrow(RigswapDbHelper.COL_PROD_IMAGE3)) ?: "",
            sellerName = cursor.getString(cursor.getColumnIndexOrThrow(RigswapDbHelper.COL_PROD_SELLER_NAME)) ?: "SolderSlinger_99",
            sellerRating = cursor.getFloat(cursor.getColumnIndexOrThrow(RigswapDbHelper.COL_PROD_SELLER_RATING)),
            sellerSales = cursor.getInt(cursor.getColumnIndexOrThrow(RigswapDbHelper.COL_PROD_SELLER_SALES)),
            isCompatible = cursor.getInt(cursor.getColumnIndexOrThrow(RigswapDbHelper.COL_PROD_IS_COMPATIBLE)) == 1,
            isFeatured = cursor.getInt(cursor.getColumnIndexOrThrow(RigswapDbHelper.COL_PROD_IS_FEATURED)) == 1
        )
    }

    fun getAllProducts(): List<Product> {
        val list = mutableListOf<Product>()
        val db = dbHelper.readableDatabase
        val cursor = db.query(RigswapDbHelper.TABLE_PRODUCTS, null, null, null, null, null, "${RigswapDbHelper.COL_ID} DESC")
        cursor.use {
            while (it.moveToNext()) {
                list.add(mapCursorToProduct(it))
            }
        }
        return list
    }

    fun getFeaturedProducts(): List<Product> {
        val list = mutableListOf<Product>()
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            RigswapDbHelper.TABLE_PRODUCTS,
            null,
            "${RigswapDbHelper.COL_PROD_IS_FEATURED} = 1",
            null,
            null,
            null,
            "${RigswapDbHelper.COL_ID} DESC"
        )
        cursor.use {
            while (it.moveToNext()) {
                list.add(mapCursorToProduct(it))
            }
        }
        return list
    }

    fun getProductsByCategory(category: String): List<Product> {
        if (category == "All") return getAllProducts()
        val list = mutableListOf<Product>()
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            RigswapDbHelper.TABLE_PRODUCTS,
            null,
            "${RigswapDbHelper.COL_PROD_CATEGORY} = ?",
            arrayOf(category),
            null,
            null,
            "${RigswapDbHelper.COL_ID} DESC"
        )
        cursor.use {
            while (it.moveToNext()) {
                list.add(mapCursorToProduct(it))
            }
        }
        return list
    }

    fun getSellerProducts(sellerName: String): List<Product> {
        val list = mutableListOf<Product>()
        val db = dbHelper.readableDatabase
        // Match sellerName, or default seed seller ("RigBuilder_LK"), or case-insensitive match
        val cursor = db.query(
            RigswapDbHelper.TABLE_PRODUCTS,
            null,
            "${RigswapDbHelper.COL_PROD_SELLER_NAME} = ? COLLATE NOCASE",
            arrayOf(sellerName),
            null,
            null,
            "${RigswapDbHelper.COL_ID} DESC"
        )
        cursor.use {
            while (it.moveToNext()) {
                list.add(mapCursorToProduct(it))
            }
        }
        return list
    }

    fun deleteProduct(productId: Long): Int {
        val db = dbHelper.writableDatabase
        val count = db.delete(
            RigswapDbHelper.TABLE_PRODUCTS,
            "${RigswapDbHelper.COL_ID} = ?",
            arrayOf(productId.toString())
        )
        if (count > 0) {
            db.execSQL("UPDATE ${RigswapDbHelper.TABLE_USER} SET ${RigswapDbHelper.COL_USER_LISTINGS} = MAX(0, ${RigswapDbHelper.COL_USER_LISTINGS} - 1) WHERE ${RigswapDbHelper.COL_ID} = 1")
        }
        return count
    }

    fun getCompatibleProducts(category: String = "All"): List<Product> {
        val list = mutableListOf<Product>()
        val db = dbHelper.readableDatabase
        val selection = if (category == "All") {
            "${RigswapDbHelper.COL_PROD_IS_COMPATIBLE} = 1"
        } else {
            "${RigswapDbHelper.COL_PROD_IS_COMPATIBLE} = 1 AND ${RigswapDbHelper.COL_PROD_CATEGORY} = ?"
        }
        val selectionArgs = if (category == "All") null else arrayOf(category)
        val cursor = db.query(
            RigswapDbHelper.TABLE_PRODUCTS,
            null,
            selection,
            selectionArgs,
            null,
            null,
            "${RigswapDbHelper.COL_ID} DESC"
        )
        cursor.use {
            while (it.moveToNext()) {
                list.add(mapCursorToProduct(it))
            }
        }
        return list
    }

    /**
     * Evaluates compatibility of a product against the user's saved rig.
     * Returns a Pair: (isCompatible: Boolean, reason: String).
     */
    fun checkProductCompatibility(product: Product, rig: SavedRig): Pair<Boolean, String> {
        return when (product.category) {
            "CPUs" -> {
                val socketKeyword = rig.socket.replace("Socket ", "").trim()
                val textToSearch = "${product.title} ${product.model} ${product.powerConnector} ${product.description}"
                val isMatch = textToSearch.contains(socketKeyword, ignoreCase = true)
                if (isMatch) {
                    Pair(true, "Matches your ${rig.socket} motherboard")
                } else {
                    Pair(false, "Requires ${product.powerConnector}, but your rig is ${rig.socket}")
                }
            }
            "GPUs" -> {
                val rigWattage = Regex("(\\d+)").find(rig.psuCapacity)?.value?.toIntOrNull() ?: 850
                val suggestedWattage = Regex("(\\d+)").find(product.suggestedPsu)?.value?.toIntOrNull() ?: 0
                if (suggestedWattage > 0 && rigWattage < suggestedWattage) {
                    Pair(false, "Requires ${product.suggestedPsu} PSU, but your rig has ${rig.psuCapacity}")
                } else {
                    Pair(true, "Fits ${rig.gpuSlot} & supported by ${rig.psuCapacity}")
                }
            }
            "Cooling" -> {
                val socketKeyword = rig.socket.replace("Socket ", "").trim()
                val textToSearch = "${product.title} ${product.model} ${product.powerConnector} ${product.description}"
                val isUniversal = product.title.contains("Fan", ignoreCase = true) ||
                        product.title.contains("Tower", ignoreCase = true) ||
                        product.title.contains("Case", ignoreCase = true) ||
                        product.powerConnector.contains("Chassis", ignoreCase = true) ||
                        product.powerConnector.contains("PWM", ignoreCase = true)
                if (isUniversal) {
                    Pair(true, "Universal cooling/chassis compatible with your setup")
                } else {
                    val isMatch = textToSearch.contains(socketKeyword, ignoreCase = true)
                    if (isMatch) {
                        Pair(true, "Mounting bracket supports ${rig.socket}")
                    } else {
                        Pair(false, "Mounting bracket does not support ${rig.socket}")
                    }
                }
            }
            "Storage" -> {
                Pair(true, "High-speed NVMe storage compatible with your motherboard")
            }
            "PSUs" -> {
                val rigWattage = Regex("(\\d+)").find(rig.psuCapacity)?.value?.toIntOrNull() ?: 850
                val productWattage = Regex("(\\d+)").find(product.tdp)?.value?.toIntOrNull()
                    ?: Regex("(\\d+)\\s*[Ww]").find(product.title)?.groupValues?.get(1)?.toIntOrNull()
                    ?: 0

                if (productWattage > 0 && productWattage < rigWattage) {
                    Pair(false, "Insufficient wattage: Provides ${productWattage}W, but your rig requires at least ${rig.psuCapacity}")
                } else {
                    Pair(true, "Provides ${productWattage}W, meets or exceeds your rig's ${rig.psuCapacity} requirement")
                }
            }
            "Peripherals" -> {
                Pair(true, "Universal peripheral compatible with your PC")
            }
            "Motherboards" -> {
                val socketDesc = product.powerConnector // stored as socket string
                Pair(true, "Socket: $socketDesc — verify your CPU compatibility")
            }
            "RAM" -> {
                val standard = product.memory // stored as DDR4/DDR5 string
                Pair(true, "RAM standard: $standard — check your motherboard support")
            }
            else -> {
                Pair(true, "Compatible hardware")
            }
        }
    }

    fun getCompatibleProductsByRig(rig: SavedRig, category: String = "All"): List<Product> {
        val candidates = if (category == "All") getAllProducts() else getProductsByCategory(category)
        return candidates.filter { checkProductCompatibility(it, rig).first }
    }

    fun getCompatibleProductsBySpecs(
        socket: String,
        ram: String,
        gpuSlot: String,
        psu: String,
        category: String = "All"
    ): List<Product> {
        val tempRig = SavedRig(
            socket = socket,
            ramStandard = ram,
            gpuSlot = gpuSlot,
            psuCapacity = psu
        )
        return getCompatibleProductsByRig(tempRig, category)
    }

    fun getProductById(id: Long): Product? {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            RigswapDbHelper.TABLE_PRODUCTS,
            null,
            "${RigswapDbHelper.COL_ID} = ?",
            arrayOf(id.toString()),
            null,
            null,
            null
        )
        cursor.use {
            if (it.moveToFirst()) {
                return mapCursorToProduct(it)
            }
        }
        return null
    }

    fun insertProduct(product: Product): Long {
        val db = dbHelper.writableDatabase
        val cv = ContentValues().apply {
            put(RigswapDbHelper.COL_PROD_TITLE, product.title)
            put(RigswapDbHelper.COL_PROD_CATEGORY, product.category)
            put(RigswapDbHelper.COL_PROD_PRICE, product.priceLkr)
            put(RigswapDbHelper.COL_PROD_CONDITION, product.condition)
            put(RigswapDbHelper.COL_PROD_BRAND, product.brand)
            put(RigswapDbHelper.COL_PROD_MODEL, product.model)
            put(RigswapDbHelper.COL_PROD_MEMORY, product.memory)
            put(RigswapDbHelper.COL_PROD_TDP, product.tdp)
            put(RigswapDbHelper.COL_PROD_SUGGESTED_PSU, product.suggestedPsu)
            put(RigswapDbHelper.COL_PROD_POWER_CONN, product.powerConnector)
            put(RigswapDbHelper.COL_PROD_DESCRIPTION, product.description)
            put(RigswapDbHelper.COL_PROD_LOCATION, product.location)
            put(RigswapDbHelper.COL_PROD_RATING, product.rating)
            put(RigswapDbHelper.COL_PROD_IMAGE, product.imageResName)
            put(RigswapDbHelper.COL_PROD_IMAGE2, product.imageResName2)
            put(RigswapDbHelper.COL_PROD_IMAGE3, product.imageResName3)
            put(RigswapDbHelper.COL_PROD_SELLER_NAME, product.sellerName)
            put(RigswapDbHelper.COL_PROD_SELLER_RATING, product.sellerRating)
            put(RigswapDbHelper.COL_PROD_SELLER_SALES, product.sellerSales)
            put(RigswapDbHelper.COL_PROD_IS_COMPATIBLE, if (product.isCompatible) 1 else 0)
            put(RigswapDbHelper.COL_PROD_IS_FEATURED, if (product.isFeatured) 1 else 0)
        }
        val newId = db.insert(RigswapDbHelper.TABLE_PRODUCTS, null, cv)

        // Increment user active listings
        db.execSQL("UPDATE ${RigswapDbHelper.TABLE_USER} SET ${RigswapDbHelper.COL_USER_LISTINGS} = ${RigswapDbHelper.COL_USER_LISTINGS} + 1 WHERE ${RigswapDbHelper.COL_ID} = 1")

        return newId
    }

    fun searchProducts(
        query: String?,
        category: String? = null,
        minPrice: Long? = null,
        maxPrice: Long? = null,
        conditions: List<String>? = null,
        sortBy: String? = null
    ): List<Product> {
        val list = mutableListOf<Product>()
        val db = dbHelper.readableDatabase

        val whereClauses = mutableListOf<String>()
        val args = mutableListOf<String>()

        if (!query.isNullOrBlank()) {
            whereClauses.add("(${RigswapDbHelper.COL_PROD_TITLE} LIKE ? OR ${RigswapDbHelper.COL_PROD_BRAND} LIKE ? OR ${RigswapDbHelper.COL_PROD_MODEL} LIKE ?)")
            val q = "%${query.trim()}%"
            args.add(q)
            args.add(q)
            args.add(q)
        }

        if (!category.isNullOrBlank() && category != "All") {
            whereClauses.add("${RigswapDbHelper.COL_PROD_CATEGORY} = ?")
            args.add(category)
        }

        if (minPrice != null) {
            whereClauses.add("${RigswapDbHelper.COL_PROD_PRICE} >= ?")
            args.add(minPrice.toString())
        }

        if (maxPrice != null && maxPrice > 0) {
            whereClauses.add("${RigswapDbHelper.COL_PROD_PRICE} <= ?")
            args.add(maxPrice.toString())
        }

        if (!conditions.isNullOrEmpty()) {
            val condPlaceholders = conditions.joinToString(",") { "?" }
            whereClauses.add("${RigswapDbHelper.COL_PROD_CONDITION} IN ($condPlaceholders)")
            args.addAll(conditions)
        }

        val selection = if (whereClauses.isNotEmpty()) whereClauses.joinToString(" AND ") else null
        val selectionArgs = if (args.isNotEmpty()) args.toTypedArray() else null

        val orderBy = when (sortBy) {
            "Price: Low to High" -> "${RigswapDbHelper.COL_PROD_PRICE} ASC"
            "Price: High to Low" -> "${RigswapDbHelper.COL_PROD_PRICE} DESC"
            else -> "${RigswapDbHelper.COL_ID} DESC"
        }

        val cursor = db.query(RigswapDbHelper.TABLE_PRODUCTS, null, selection, selectionArgs, null, null, orderBy)
        cursor.use {
            while (it.moveToNext()) {
                list.add(mapCursorToProduct(it))
            }
        }
        return list
    }

    // --- Saved Rig ---

    fun getSavedRig(): SavedRig {
        val db = dbHelper.readableDatabase
        val cursor = db.query(RigswapDbHelper.TABLE_SAVED_RIG, null, null, null, null, null, null)
        cursor.use {
            if (it.moveToFirst()) {
                return SavedRig(
                    id = it.getLong(it.getColumnIndexOrThrow(RigswapDbHelper.COL_ID)),
                    name = it.getString(it.getColumnIndexOrThrow(RigswapDbHelper.COL_RIG_NAME)) ?: "My Main Battlestation",
                    subtitle = it.getString(it.getColumnIndexOrThrow(RigswapDbHelper.COL_RIG_SUBTITLE)) ?: "Mini-ITX / High FPS Build",
                    socket = it.getString(it.getColumnIndexOrThrow(RigswapDbHelper.COL_RIG_SOCKET)) ?: "Socket AM5",
                    ramStandard = it.getString(it.getColumnIndexOrThrow(RigswapDbHelper.COL_RIG_RAM)) ?: "DDR5 dual-channel",
                    gpuSlot = it.getString(it.getColumnIndexOrThrow(RigswapDbHelper.COL_RIG_GPU_SLOT)) ?: "PCIe Gen 5.0 x16",
                    psuCapacity = it.getString(it.getColumnIndexOrThrow(RigswapDbHelper.COL_RIG_PSU)) ?: "850 Watts Modular",
                    healthScore = it.getString(it.getColumnIndexOrThrow(RigswapDbHelper.COL_RIG_HEALTH)) ?: "98% OK"
                )
            }
        }
        return SavedRig()
    }

    fun updateSavedRig(rig: SavedRig) {
        val db = dbHelper.writableDatabase
        val cv = ContentValues().apply {
            put(RigswapDbHelper.COL_ID, 1)
            put(RigswapDbHelper.COL_RIG_NAME, rig.name)
            put(RigswapDbHelper.COL_RIG_SUBTITLE, rig.subtitle)
            put(RigswapDbHelper.COL_RIG_SOCKET, rig.socket)
            put(RigswapDbHelper.COL_RIG_RAM, rig.ramStandard)
            put(RigswapDbHelper.COL_RIG_GPU_SLOT, rig.gpuSlot)
            put(RigswapDbHelper.COL_RIG_PSU, rig.psuCapacity)
            put(RigswapDbHelper.COL_RIG_HEALTH, rig.healthScore)
        }
        val rows = db.update(RigswapDbHelper.TABLE_SAVED_RIG, cv, "${RigswapDbHelper.COL_ID} = 1", null)
        if (rows == 0) {
            db.insertWithOnConflict(RigswapDbHelper.TABLE_SAVED_RIG, null, cv, android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE)
        }
    }

    // --- Cart ---

    fun getCartItems(): List<CartItem> {
        val list = mutableListOf<CartItem>()
        val db = dbHelper.readableDatabase
        val cursor = db.query(RigswapDbHelper.TABLE_CART, null, null, null, null, null, "${RigswapDbHelper.COL_ID} ASC")
        cursor.use {
            while (it.moveToNext()) {
                val cartId = it.getLong(it.getColumnIndexOrThrow(RigswapDbHelper.COL_ID))
                val prodId = it.getLong(it.getColumnIndexOrThrow(RigswapDbHelper.COL_CART_PRODUCT_ID))
                val qty = it.getInt(it.getColumnIndexOrThrow(RigswapDbHelper.COL_CART_QUANTITY))
                val product = getProductById(prodId)
                if (product != null) {
                    list.add(CartItem(id = cartId, product = product, quantity = qty))
                }
            }
        }
        return list
    }

    fun addToCart(productId: Long, qty: Int = 1): Long {
        val db = dbHelper.writableDatabase
        // Check if already in cart
        val cursor = db.query(
            RigswapDbHelper.TABLE_CART,
            null,
            "${RigswapDbHelper.COL_CART_PRODUCT_ID} = ?",
            arrayOf(productId.toString()),
            null,
            null,
            null
        )
        cursor.use {
            if (it.moveToFirst()) {
                val cartId = it.getLong(it.getColumnIndexOrThrow(RigswapDbHelper.COL_ID))
                val existingQty = it.getInt(it.getColumnIndexOrThrow(RigswapDbHelper.COL_CART_QUANTITY))
                val cv = ContentValues().apply {
                    put(RigswapDbHelper.COL_CART_QUANTITY, existingQty + qty)
                }
                db.update(RigswapDbHelper.TABLE_CART, cv, "${RigswapDbHelper.COL_ID} = ?", arrayOf(cartId.toString()))
                return cartId
            }
        }

        val cv = ContentValues().apply {
            put(RigswapDbHelper.COL_CART_PRODUCT_ID, productId)
            put(RigswapDbHelper.COL_CART_QUANTITY, qty)
        }
        return db.insert(RigswapDbHelper.TABLE_CART, null, cv)
    }

    fun removeFromCart(cartItemId: Long): Int {
        val db = dbHelper.writableDatabase
        return db.delete(RigswapDbHelper.TABLE_CART, "${RigswapDbHelper.COL_ID} = ?", arrayOf(cartItemId.toString()))
    }

    fun clearCart(): Int {
        val db = dbHelper.writableDatabase
        return db.delete(RigswapDbHelper.TABLE_CART, null, null)
    }

    // --- Chat Messages ---

    fun getMessagesForProduct(productId: Long): List<ChatMessage> {
        val list = mutableListOf<ChatMessage>()
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            RigswapDbHelper.TABLE_CHAT,
            null,
            "${RigswapDbHelper.COL_CHAT_PROD_ID} = ?",
            arrayOf(productId.toString()),
            null,
            null,
            "${RigswapDbHelper.COL_ID} ASC"
        )
        cursor.use {
            while (it.moveToNext()) {
                list.add(
                    ChatMessage(
                        id = it.getLong(it.getColumnIndexOrThrow(RigswapDbHelper.COL_ID)),
                        productId = it.getLong(it.getColumnIndexOrThrow(RigswapDbHelper.COL_CHAT_PROD_ID)),
                        senderName = it.getString(it.getColumnIndexOrThrow(RigswapDbHelper.COL_CHAT_SENDER)) ?: "Unknown",
                        message = it.getString(it.getColumnIndexOrThrow(RigswapDbHelper.COL_CHAT_MESSAGE)) ?: "",
                        timestamp = it.getString(it.getColumnIndexOrThrow(RigswapDbHelper.COL_CHAT_TIMESTAMP)) ?: "",
                        isFromMe = it.getInt(it.getColumnIndexOrThrow(RigswapDbHelper.COL_CHAT_IS_ME)) == 1
                    )
                )
            }
        }
        return list
    }

    fun sendMessage(productId: Long, text: String, senderName: String = "Me", isFromMe: Boolean = true): ChatMessage {
        val db = dbHelper.writableDatabase
        val timeFormat = SimpleDateFormat("h:mm a", Locale.US)
        val timestamp = timeFormat.format(Date())
        val cv = ContentValues().apply {
            put(RigswapDbHelper.COL_CHAT_PROD_ID, productId)
            put(RigswapDbHelper.COL_CHAT_SENDER, senderName)
            put(RigswapDbHelper.COL_CHAT_MESSAGE, text)
            put(RigswapDbHelper.COL_CHAT_TIMESTAMP, timestamp)
            put(RigswapDbHelper.COL_CHAT_IS_ME, if (isFromMe) 1 else 0)
        }
        val id = db.insert(RigswapDbHelper.TABLE_CHAT, null, cv)
        return ChatMessage(
            id = id,
            productId = productId,
            senderName = senderName,
            message = text,
            timestamp = timestamp,
            isFromMe = isFromMe
        )
    }

    // --- Transactions & Merchant Hub ---

    fun getAllTransactions(): List<SellerTransaction> {
        val list = mutableListOf<SellerTransaction>()
        val db = dbHelper.readableDatabase
        val cursor = db.query(RigswapDbHelper.TABLE_TRANSACTIONS, null, null, null, null, null, "${RigswapDbHelper.COL_ID} DESC")
        cursor.use {
            while (it.moveToNext()) {
                list.add(
                    SellerTransaction(
                        id = it.getLong(it.getColumnIndexOrThrow(RigswapDbHelper.COL_ID)),
                        title = it.getString(it.getColumnIndexOrThrow(RigswapDbHelper.COL_TX_TITLE)) ?: "",
                        orderNumber = it.getString(it.getColumnIndexOrThrow(RigswapDbHelper.COL_TX_ORDER_NUM)) ?: "",
                        priceStr = it.getString(it.getColumnIndexOrThrow(RigswapDbHelper.COL_TX_PRICE)) ?: "",
                        status = it.getString(it.getColumnIndexOrThrow(RigswapDbHelper.COL_TX_STATUS)) ?: "PENDING"
                    )
                )
            }
        }
        return list
    }

    fun addTransaction(title: String, orderNumber: String, priceStr: String, status: String): Long {
        val db = dbHelper.writableDatabase
        val cv = ContentValues().apply {
            put(RigswapDbHelper.COL_TX_TITLE, title)
            put(RigswapDbHelper.COL_TX_ORDER_NUM, orderNumber)
            put(RigswapDbHelper.COL_TX_PRICE, priceStr)
            put(RigswapDbHelper.COL_TX_STATUS, status)
        }
        return db.insert(RigswapDbHelper.TABLE_TRANSACTIONS, null, cv)
    }

    // --- User ---

    fun getUser(): User {
        val db = dbHelper.readableDatabase
        val cursor = db.query(RigswapDbHelper.TABLE_USER, null, "${RigswapDbHelper.COL_ID} = 1", null, null, null, null)
        cursor.use {
            if (it.moveToFirst()) {
                return User(
                    id = it.getLong(it.getColumnIndexOrThrow(RigswapDbHelper.COL_ID)),
                    email = it.getString(it.getColumnIndexOrThrow(RigswapDbHelper.COL_USER_EMAIL)) ?: "gamer@rigswap.gg",
                    username = it.getString(it.getColumnIndexOrThrow(RigswapDbHelper.COL_USER_NAME)) ?: "RigBuilder_LK",
                    activeListingsCount = it.getInt(it.getColumnIndexOrThrow(RigswapDbHelper.COL_USER_LISTINGS)),
                    totalSalesLkr = it.getLong(it.getColumnIndexOrThrow(RigswapDbHelper.COL_USER_SALES))
                )
            }
        }
        return User()
    }
}
