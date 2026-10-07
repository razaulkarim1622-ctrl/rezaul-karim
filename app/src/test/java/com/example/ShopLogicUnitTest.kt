package com.example

import com.example.data.model.Customer
import com.example.data.model.Product
import com.example.data.model.Sale
import com.example.util.FormatUtils
import com.example.util.LanguageManager
import com.example.util.Strings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ShopLogicUnitTest {

    @Test
    fun testCurrencyFormatting() {
        val formatted = FormatUtils.formatCurrency(1500.5, "৳")
        assertTrue(formatted.contains("1,500.50"))
        assertTrue(formatted.contains("৳"))
    }

    @Test
    fun testLoyaltyPointsCalculation() {
        val totalSpent = 550.0
        val earnedPoints = (totalSpent / 100.0).toInt()
        assertEquals(5, earnedPoints)

        val customer = Customer(
            id = 1,
            name = "Test Customer",
            loyaltyPoints = 20
        )
        val redeemed = 10
        val updatedPoints = customer.loyaltyPoints - redeemed + earnedPoints
        assertEquals(15, updatedPoints)
    }

    @Test
    fun testLanguageManagerSwitching() {
        LanguageManager.currentLanguage = com.example.util.AppLanguage.BANGLA
        assertEquals("দোকান খাতা", Strings.appName)
        assertEquals("নতুন বিক্রি (POS)", Strings.posSales)

        LanguageManager.currentLanguage = com.example.util.AppLanguage.ENGLISH
        assertEquals("Dokan Khata", Strings.appName)
        assertEquals("New Sale (POS)", Strings.posSales)
    }

    @Test
    fun testInvoiceNumberFormat() {
        val invoiceNo = FormatUtils.generateInvoiceNumber()
        assertTrue(invoiceNo.startsWith("INV-"))
    }
}
