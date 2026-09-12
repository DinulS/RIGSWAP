package com.example.rigswap.data.model

import java.text.NumberFormat
import java.util.Locale

data class Product(
    val id: Long = 0,
    val title: String,
    val category: String, // GPUs, CPUs, Cooling, Peripherals, Storage, PSUs
    val priceLkr: Long,
    val condition: String, // NEW, USED, REFURB
    val brand: String = "",
    val model: String = "",
    val memory: String = "N/A",
    val tdp: String = "N/A",
    val suggestedPsu: String = "N/A",
    val powerConnector: String = "N/A",
    val description: String = "",
    val location: String = "Colombo, Sri Lanka",
    val rating: Float = 5.0f,
    val imageResName: String = "img_rtx4090",
    val imageResName2: String = "",
    val imageResName3: String = "",
    val sellerName: String = "SolderSlinger_99",
    val sellerRating: Float = 4.9f,
    val sellerSales: Int = 48,
    val isCompatible: Boolean = true,
    val isFeatured: Boolean = true
) {
    fun getFormattedPrice(): String {
        val formatter = NumberFormat.getNumberInstance(Locale.US)
        return "${formatter.format(priceLkr)} LKR"
    }
}

data class SavedRig(
    val id: Long = 1,
    val name: String = "My Main Battlestation",
    val subtitle: String = "Mini-ITX / High FPS Build",
    val socket: String = "Socket AM5",
    val ramStandard: String = "DDR5 dual-channel",
    val gpuSlot: String = "PCIe Gen 5.0 x16",
    val psuCapacity: String = "850 Watts Modular",
    val healthScore: String = "98% OK"
)

data class CartItem(
    val id: Long = 0,
    val product: Product,
    var quantity: Int = 1
)

data class ChatMessage(
    val id: Long = 0,
    val productId: Long = 1,
    val senderName: String,
    val message: String,
    val timestamp: String,
    val isFromMe: Boolean
)

data class SellerTransaction(
    val id: Long = 0,
    val title: String,
    val orderNumber: String,
    val priceStr: String,
    val status: String // SHIPPED, DELIVERED, PENDING
)

data class User(
    val id: Long = 1,
    val email: String = "gamer@rigswap.gg",
    val username: String = "RigBuilder_LK",
    val activeListingsCount: Int = 14,
    val totalSalesLkr: Long = 486500
) {
    fun getFormattedSales(): String {
        val formatter = NumberFormat.getNumberInstance(Locale.US)
        return "${formatter.format(totalSalesLkr)} LKR"
    }
}
