package com.example.util

enum class AppLanguage(val code: String, val displayName: String) {
    BANGLA("bn", "বাংলা"),
    ENGLISH("en", "English")
}

object LanguageManager {
    var currentLanguage: AppLanguage = AppLanguage.BANGLA

    fun isBangla(): Boolean = currentLanguage == AppLanguage.BANGLA
    fun isEnglish(): Boolean = currentLanguage == AppLanguage.ENGLISH
}

object Strings {
    private val isBn: Boolean get() = LanguageManager.isBangla()

    // Navigation & Common
    val appName get() = if (isBn) "দোকান খাতা" else "Dokan Khata"
    val dashboard get() = if (isBn) "ড্যাশবোর্ড" else "Dashboard"
    val posSales get() = if (isBn) "নতুন বিক্রি (POS)" else "New Sale (POS)"
    val products get() = if (isBn) "পণ্য ও স্টক" else "Products & Stock"
    val customers get() = if (isBn) "কাস্টমার ও খাতা" else "Customers & Credit"
    val suppliers get() = if (isBn) "সাপ্লায়ার ও দেনা" else "Suppliers & Payables"
    val purchases get() = if (isBn) "মালামাল ক্রয়" else "Purchases"
    val expenses get() = if (isBn) "দোকানের খরচ" else "Expenses"
    val cashbook get() = if (isBn) "ক্যাশ বহি" else "Cashbook"
    val reports get() = if (isBn) "রিপোর্ট ও লাভ" else "Reports & Profit"
    val settings get() = if (isBn) "সেটিংস" else "Settings"

    // Dashboard
    val todaySales get() = if (isBn) "আজকের বিক্রি" else "Today's Sales"
    val todayCollection get() = if (isBn) "আজকের আদায়" else "Today's Collection"
    val todayExpense get() = if (isBn) "আজকের খরচ" else "Today's Expense"
    val todayNetProfit get() = if (isBn) "আজকের নিট লাভ" else "Today's Net Profit"
    val customerDue get() = if (isBn) "কাস্টমার বাকি (পাওনা)" else "Customer Due (Receivable)"
    val supplierDue get() = if (isBn) "সাপ্লায়ার দেনা" else "Supplier Due (Payable)"
    val lowStockAlert get() = if (isBn) "স্টক সতর্কবার্তা!" else "Low Stock Alert!"
    val recentSales get() = if (isBn) "সাম্প্রতিক বিক্রয়সমূহ" else "Recent Sales"
    val recentTransactions get() = if (isBn) "সাম্প্রতিক লেনদেন" else "Recent Transactions"
    val viewAll get() = if (isBn) "সবগুলো দেখুন" else "View All"
    val newSaleBtn get() = if (isBn) "নতুন বিক্রি শুরু করুন (POS)" else "Start New Sale (POS)"

    // POS & Sales
    val searchProduct get() = if (isBn) "পণ্যের নাম বা বারকোড খুঁজুন..." else "Search product name or barcode..."
    val scanBarcode get() = if (isBn) "বারকোড স্ক্যান" else "Scan Barcode"
    val allProducts get() = if (isBn) "সব পণ্য" else "All Products"
    val cartEmpty get() = if (isBn) "কার্টে কোন পণ্য নেই" else "Cart is empty"
    val cartItemsCount get() = if (isBn) "কার্টে মোট পণ্য:" else "Cart Items:"
    val subtotal get() = if (isBn) "সাবটোটাল:" else "Subtotal:"
    val discount get() = if (isBn) "ছাড় / ডিসকাউন্ট:" else "Discount:"
    val grandTotal get() = if (isBn) "সর্বমোট বিল:" else "Grand Total:"
    val paidAmount get() = if (isBn) "আদায় / পেইড টাকা:" else "Paid Amount:"
    val remainingDue get() = if (isBn) "বাকি থাকবে:" else "Remaining Due:"
    val checkout get() = if (isBn) "বিল সম্পন্ন করুন" else "Checkout & Bill"
    val confirmSale get() = if (isBn) "বিক্রয় নিশ্চিত করুন" else "Confirm Sale"
    val customerSelector get() = if (isBn) "খরিদ্দার নির্বাচন" else "Select Customer"
    val regularCashCustomer get() = if (isBn) "সাধারণ খরিদ্দার (ক্যাশ)" else "Walk-in Customer (Cash)"
    val paymentMethod get() = if (isBn) "পেমেন্ট মাধ্যম:" else "Payment Method:"
    val printInvoice get() = if (isBn) "মেমো প্রিন্ট / PDF শেয়ার" else "Print Memo / Share PDF"

    // Loyalty Program
    val loyaltyPoints get() = if (isBn) "লয়্যালটি পয়েন্ট" else "Loyalty Points"
    val pointsAvailable get() = if (isBn) "উপলব্ধ পয়েন্ট:" else "Available Points:"
    val redeemPoints get() = if (isBn) "পয়েন্ট রিডিম (ছাড়)" else "Redeem Points (Discount)"
    val pointsToRedeem get() = if (isBn) "কত পয়েন্ট রিডিম করবেন?" else "Points to redeem?"
    val pointsDiscountValue get() = if (isBn) "পয়েন্ট থেকে সমমূল্যের ছাড়:" else "Points Discount Value:"
    val pointsEarnedMsg get() = if (isBn) "এই কেনাকাটায় পয়েন্ট অর্জন:" else "Points earned this sale:"
    val loyaltyProgramInfo get() = if (isBn) "প্রতি ৳১০০ কেনাকাটায় ১ পয়েন্ট অর্জন হয় (১ পয়েন্ট = ৳১ ছাড়)" else "Earn 1 point per ৳100 purchase (1 pt = ৳1 discount)"

    // Products
    val addProduct get() = if (isBn) "নতুন পণ্য যোগ করুন" else "Add New Product"
    val editProduct get() = if (isBn) "পণ্য তথ্য সংশোধন" else "Edit Product"
    val deleteProduct get() = if (isBn) "পণ্য মুছুন" else "Delete Product"
    val productName get() = if (isBn) "পণ্যের নাম *" else "Product Name *"
    val category get() = if (isBn) "ক্যাটাগরি" else "Category"
    val purchasePrice get() = if (isBn) "ক্রয় মূল্য" else "Purchase Price"
    val sellingPrice get() = if (isBn) "বিক্রয় মূল্য *" else "Selling Price *"
    val stockQty get() = if (isBn) "স্টক পরিমাণ *" else "Stock Quantity *"
    val minStockLimit get() = if (isBn) "লো-স্টক অ্যালার্ট সীমা" else "Low Stock Alert Limit"
    val barcode get() = if (isBn) "বারকোড" else "Barcode"
    val unit get() = if (isBn) "একক (কেজি/পিস/লিটার)" else "Unit (kg/pcs/liter)"

    // Customers
    val addCustomer get() = if (isBn) "নতুন কাস্টমার যোগ" else "Add New Customer"
    val customerName get() = if (isBn) "কাস্টমারের নাম *" else "Customer Name *"
    val phone get() = if (isBn) "মোবাইল নম্বর" else "Phone Number"
    val address get() = if (isBn) "ঠিকানা" else "Address"
    val totalPurchase get() = if (isBn) "মোট কেনাকাটা" else "Total Purchase"
    val totalDue get() = if (isBn) "বর্তমান বাকি" else "Current Due"
    val collectPayment get() = if (isBn) "টাকা নিন (জমা)" else "Collect Payment"
    val addDue get() = if (isBn) "বাকি দিন" else "Add Due"
    val ledgerHistory get() = if (isBn) "খাতা / হিস্ট্রি" else "Ledger History"
    val receiptPrint get() = if (isBn) "রিসিট প্রিন্ট" else "Print Receipt"

    // Suppliers & Purchases
    val addSupplier get() = if (isBn) "নতুন সাপ্লায়ার যোগ" else "Add New Supplier"
    val supplierName get() = if (isBn) "সাপ্লায়ারের নাম *" else "Supplier Name *"
    val companyName get() = if (isBn) "কোম্পানির নাম" else "Company Name"
    val paySupplier get() = if (isBn) "দেনা পরিশোধ" else "Pay Supplier"
    val addPurchase get() = if (isBn) "নতুন মালামাল স্টক ইন" else "New Purchase / Stock In"

    // Expenses & Cashbook
    val addExpense get() = if (isBn) "নতুন খরচ যুক্ত করুন" else "Add New Expense"
    val expenseCategory get() = if (isBn) "খরচের খাত" else "Expense Category"
    val amount get() = if (isBn) "টাকার পরিমাণ" else "Amount"
    val note get() = if (isBn) "বিবরণ / নোট" else "Note / Description"
    val cashIn get() = if (isBn) "ক্যাশ জমা (+)" else "Cash In (+)"
    val cashOut get() = if (isBn) "ক্যাশ প্রদান (-)" else "Cash Out (-)"
    val netCashBalance get() = if (isBn) "বর্তমান ক্যাশ ব্যালেন্স" else "Current Cash Balance"
    val addCashEntry get() = if (isBn) "নতুন ক্যাশ এন্ট্রি" else "New Cash Entry"

    // Reports
    val reportsTitle get() = if (isBn) "আর্থিক রিপোর্ট ও হিসাব" else "Financial Reports & Accounts"
    val today get() = if (isBn) "আজকে" else "Today"
    val last7Days get() = if (isBn) "গত ৭ দিন" else "Last 7 Days"
    val thisMonth get() = if (isBn) "এই মাস" else "This Month"
    val allTime get() = if (isBn) "সব সময়" else "All Time"
    val estNetProfit get() = if (isBn) "আনুমানিক নিট লাভ" else "Estimated Net Profit"
    val totalStockValue get() = if (isBn) "দোকানের মোট স্টক মালামালের মূল্য:" else "Total Shop Inventory Value:"

    // Settings
    val shopProfile get() = if (isBn) "দোকানের প্রোফাইল সেটিংস" else "Shop Profile Settings"
    val shopName get() = if (isBn) "দোকানের নাম *" else "Shop Name *"
    val ownerName get() = if (isBn) "স্বত্বাধিকারী / মালিকের নাম" else "Owner Name"
    val currencySymbol get() = if (isBn) "টাকার প্রতীক (৳ / BDT)" else "Currency Symbol"
    val thankYouMsg get() = if (isBn) "মেমো ধন্যবাদ বার্তা" else "Invoice Thank You Message"
    val pinSecurity get() = if (isBn) "নিরাপত্তা ও পিন লক" else "Security & PIN Lock"
    val enablePin get() = if (isBn) "অ্যাপ পিন লক চালু করুন" else "Enable App PIN Lock"
    val pinLabel get() = if (isBn) "সিকিউরিটি পিন (৪-৬ সংখ্যা)" else "Security PIN (4-6 digits)"
    val backupData get() = if (isBn) "ডাটা ব্যাকআপ" else "Backup Data"
    val saveSettings get() = if (isBn) "সেটিংস সংরক্ষণ করুন" else "Save Settings"
    val languageSelect get() = if (isBn) "ভাষা নির্বাচন (Language)" else "Select Language"
    val banglaLang get() = "বাংলা (Bengali)"
    val englishLang get() = "English"

    // Buttons & Dialogs
    val save get() = if (isBn) "সংরক্ষণ করুন" else "Save"
    val cancel get() = if (isBn) "বাতিল" else "Cancel"
    val close get() = if (isBn) "বন্ধ করুন" else "Close"
    val delete get() = if (isBn) "মুছে ফেলুন" else "Delete"
    val success get() = if (isBn) "সফল হয়েছে" else "Success"
}
