package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "customers")
data class Customer(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val phone: String = "",
    val address: String = "",
    val note: String = "",
    val totalPurchase: Double = 0.0,
    val totalPaid: Double = 0.0,
    val currentDue: Double = 0.0, // totalPurchase - totalPaid
    val loyaltyPoints: Int = 0, // Loyalty points earned from purchases
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "suppliers")
data class Supplier(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val phone: String = "",
    val company: String = "",
    val address: String = "",
    val note: String = "",
    val totalPurchaseAmount: Double = 0.0,
    val totalPaidAmount: Double = 0.0,
    val currentDue: Double = 0.0, // দোকানদারের দেনা
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val barcode: String = "",
    val category: String = "সাধারণ",
    val brand: String = "",
    val purchasePrice: Double = 0.0,
    val sellingPrice: Double = 0.0,
    val stockQuantity: Double = 0.0,
    val minStockAlert: Double = 5.0,
    val unit: String = "টি", // যেমন: কেজি, লিটার, পিস, টি, প্যাকেট, ডজন
    val supplierName: String = "",
    val expiryDate: String = "",
    val description: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "sales")
data class Sale(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val invoiceNumber: String,
    val customerId: Long? = null,
    val customerName: String = "সাধারণ খরিদ্দার (Cash)",
    val customerPhone: String = "",
    val subtotal: Double,
    val discount: Double = 0.0,
    val grandTotal: Double,
    val paidAmount: Double,
    val dueAmount: Double,
    val paymentMethod: String = "Cash", // Cash, bKash, Nagad, Rocket, Bank, Due
    val note: String = "",
    val loyaltyPointsRedeemed: Int = 0,
    val loyaltyPointsEarned: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "sale_items")
data class SaleItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val saleId: Long,
    val productId: Long,
    val productName: String,
    val unit: String = "টি",
    val quantity: Double,
    val purchasePrice: Double, // For profit calculation
    val unitPrice: Double,
    val totalPrice: Double
)

@Entity(tableName = "purchases")
data class Purchase(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val supplierId: Long? = null,
    val supplierName: String,
    val supplierPhone: String = "",
    val totalAmount: Double,
    val paidAmount: Double,
    val dueAmount: Double,
    val paymentMethod: String = "Cash",
    val note: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "purchase_items")
data class PurchaseItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val purchaseId: Long,
    val productId: Long,
    val productName: String,
    val quantity: Double,
    val unitPrice: Double,
    val totalPrice: Double
)

@Entity(tableName = "customer_transactions")
data class CustomerTransaction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val customerId: Long,
    val customerName: String,
    val type: String, // "SALE_DUE" (বাকি নেওয়া), "PAYMENT" (টাকা জমা), "INITIAL_DUE"
    val amount: Double,
    val paymentMethod: String = "Cash", // Cash, bKash, Nagad, Rocket, Bank
    val saleId: Long? = null,
    val remainingDue: Double,
    val note: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "supplier_transactions")
data class SupplierTransaction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val supplierId: Long,
    val supplierName: String,
    val type: String, // "PURCHASE_DUE" (বাকি কেনা), "PAYMENT" (টাকা পরিশোধ)
    val amount: Double,
    val paymentMethod: String = "Cash",
    val purchaseId: Long? = null,
    val remainingDue: Double,
    val note: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val category: String, // দোকান ভাড়া, বিদ্যুৎ বিল, কর্মচারীর বেতন, পরিবহন, চা-নাস্তা, মোবাইল/নেট, অন্যান্য
    val amount: Double,
    val description: String = "",
    val paymentMethod: String = "Cash",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "cashbook_entries")
data class CashbookEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String, // "IN" (জমা) or "OUT" (খরচ/প্রদান)
    val category: String, // বিক্রয়, বাকি আদায়, মালামাল কেনা, দোকানের খরচ, প্রারম্ভিক জমা, ইত্যাদি
    val amount: Double,
    val balanceAfter: Double = 0.0,
    val paymentMethod: String = "Cash",
    val referenceId: String = "", // Sale ID, Expense ID etc
    val description: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "shop_settings")
data class ShopSettings(
    @PrimaryKey val id: Int = 1,
    val shopName: String = "আমার দোকান",
    val ownerName: String = "দোকানদার",
    val phone: String = "01700000000",
    val address: String = "বাজার রোড, ঢাকা",
    val currencySymbol: String = "৳",
    val thankYouMessage: String = "আমাদের সাথে কেনাকাটা করার জন্য ধন্যবাদ! আবার আসবেন।",
    val appPin: String = "111111", // Default login password
    val isPinEnabled: Boolean = true,
    val userRole: String = "Admin", // Admin, Manager, Staff
    val staffPin: String = "111111",
    val language: String = "bn", // "bn" or "en"
    val pointsPerHundred: Int = 1, // 1 point per 100 BDT purchase
    val pointValueInCurrency: Double = 1.0 // 1 point = 1 BDT discount
)
