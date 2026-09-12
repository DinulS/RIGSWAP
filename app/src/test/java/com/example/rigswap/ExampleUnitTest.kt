package com.example.rigswap

import com.example.rigswap.data.model.CartItem
import com.example.rigswap.data.model.ChatMessage
import com.example.rigswap.data.model.MotherboardSocketCompatibility
import com.example.rigswap.data.model.Product
import com.example.rigswap.data.model.RamStandardCompatibility
import com.example.rigswap.data.model.SavedRig
import com.example.rigswap.data.model.SellerTransaction
import com.example.rigswap.data.model.User
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun product_priceFormatting_isCorrect() {
        val product = Product(
            title = "ASUS ROG Strix RTX 4090 24GB",
            category = "GPUs",
            priceLkr = 950000L,
            condition = "NEW"
        )
        assertEquals("950,000 LKR", product.getFormattedPrice())
    }

    @Test
    fun product_lowPriceFormatting_isCorrect() {
        val product = Product(
            title = "Thermal Paste",
            category = "Cooling",
            priceLkr = 1500L,
            condition = "NEW"
        )
        assertEquals("1,500 LKR", product.getFormattedPrice())
    }

    @Test
    fun user_totalSalesFormatting_isCorrect() {
        val user = User(
            id = 1,
            email = "gamer@rigswap.gg",
            username = "RigBuilder_LK",
            activeListingsCount = 14,
            totalSalesLkr = 486500L
        )
        assertEquals("486,500 LKR", user.getFormattedSales())
    }

    @Test
    fun cartItem_totalCalculation_isCorrect() {
        val product = Product(
            title = "Corsair H150i Elite 360mm",
            category = "Cooling",
            priceLkr = 30000L,
            condition = "USED"
        )
        val cartItem = CartItem(id = 1, product = product, quantity = 2)
        val lineTotal = cartItem.product.priceLkr * cartItem.quantity
        assertEquals(60000L, lineTotal)
    }

    @Test
    fun savedRig_defaultAttributes_areValid() {
        val rig = SavedRig()
        assertEquals("My Main Battlestation", rig.name)
        assertEquals("Socket AM5", rig.socket)
        assertEquals("98% OK", rig.healthScore)
    }

    @Test
    fun motherboard_realWorldModels_useTheirActualCpuSocket() {
        assertEquals(
            MotherboardSocketCompatibility.LGA_1851,
            MotherboardSocketCompatibility.socketForModel("MEG Z890 ACE")
        )
        assertEquals(
            MotherboardSocketCompatibility.LGA_1700,
            MotherboardSocketCompatibility.socketForModel("MEG Z790 ACE")
        )
        assertEquals(
            MotherboardSocketCompatibility.SOCKET_AM5,
            MotherboardSocketCompatibility.socketForModel("MEG X670E ACE")
        )
    }

    @Test
    fun ram_realWorldModels_useTheirActualDdrStandard() {
        assertEquals(
            "DDR5-6400",
            RamStandardCompatibility.standardForModel("Dominator Platinum RGB 64GB DDR5-6400")
        )
        assertEquals(
            "DDR4-3600",
            RamStandardCompatibility.standardForModel("Ripjaws V 32GB DDR4-3600")
        )
        assertEquals(
            RamStandardCompatibility.allSupportedStandards,
            RamStandardCompatibility.standardsForModel("Other / Custom")
        )
    }

    @Test
    fun chatMessage_ownership_isCorrect() {
        val buyerMsg = ChatMessage(
            id = 1,
            productId = 1,
            senderName = "Me",
            message = "Is native 16-pin adapter included?",
            timestamp = "10:15 AM",
            isFromMe = true
        )
        assertTrue(buyerMsg.isFromMe)

        val sellerMsg = ChatMessage(
            id = 2,
            productId = 1,
            senderName = "SolderSlinger_99",
            message = "Yes, original box and adapter included.",
            timestamp = "10:16 AM",
            isFromMe = false
        )
        assertFalse(sellerMsg.isFromMe)
    }

    @Test
    fun sellerTransaction_status_isCorrect() {
        val tx = SellerTransaction(
            id = 1,
            title = "EK-Quantum AM4 CPU Block",
            orderNumber = "Order #TX-4012",
            priceStr = "$80",
            status = "DELIVERED"
        )
        assertEquals("DELIVERED", tx.status)
        assertEquals("Order #TX-4012", tx.orderNumber)
    }
}
