package com.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.util.LanguageManager

enum class Screen(val bnTitle: String, val enTitle: String, val icon: ImageVector) {
    DASHBOARD("ড্যাশবোর্ড", "Dashboard", Icons.Default.Dashboard),
    POS_SALES("নতুন বিক্রি", "POS Sale", Icons.Default.PointOfSale),
    PRODUCTS("পণ্য ও স্টক", "Products", Icons.Default.Inventory2),
    CUSTOMERS("কাস্টমার ও খাতা", "Customers", Icons.Default.People),
    SUPPLIERS("সাপ্লায়ার ও দেনা", "Suppliers", Icons.Default.LocalShipping),
    PURCHASES("মালামাল ক্রয়", "Purchases", Icons.Default.ShoppingBag),
    EXPENSES("দোকানের খরচ", "Expenses", Icons.Default.ReceiptLong),
    CASHBOOK("ক্যাশ বহি", "Cashbook", Icons.Default.AccountBalanceWallet),
    REPORTS("রিপোর্ট ও হিসাব", "Reports", Icons.Default.Assessment),
    SETTINGS("সেটিংস", "Settings", Icons.Default.Settings);

    val title: String get() = if (LanguageManager.isBangla()) bnTitle else enTitle
}
