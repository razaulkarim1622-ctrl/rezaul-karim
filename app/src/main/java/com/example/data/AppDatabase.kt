package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.*
import com.example.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        Customer::class,
        Supplier::class,
        Product::class,
        Sale::class,
        SaleItem::class,
        Purchase::class,
        PurchaseItem::class,
        CustomerTransaction::class,
        SupplierTransaction::class,
        Expense::class,
        CashbookEntry::class,
        ShopSettings::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun customerDao(): CustomerDao
    abstract fun supplierDao(): SupplierDao
    abstract fun productDao(): ProductDao
    abstract fun saleDao(): SaleDao
    abstract fun purchaseDao(): PurchaseDao
    abstract fun customerTransactionDao(): CustomerTransactionDao
    abstract fun supplierTransactionDao(): SupplierTransactionDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun cashbookDao(): CashbookDao
    abstract fun shopSettingsDao(): ShopSettingsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "dokan_khata_db"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Populate default data on background thread
                            CoroutineScope(Dispatchers.IO).launch {
                                INSTANCE?.let { database ->
                                    populateInitialData(database)
                                }
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private suspend fun populateInitialData(db: AppDatabase) {
            val settingsDao = db.shopSettingsDao()
            settingsDao.insertOrUpdate(
                ShopSettings(
                    id = 1,
                    shopName = "মেসার্স ভাই ভাই জেনারেল স্টোর",
                    ownerName = "মোঃ রফিকুল ইসলাম",
                    phone = "01712-345678",
                    address = "নিউ মার্কেট, ধানমন্ডি, ঢাকা",
                    currencySymbol = "৳",
                    thankYouMessage = "আমাদের সাথে কেনাকাটা করার জন্য ধন্যবাদ! আবার আসবেন।",
                    userRole = "Admin",
                    appPin = "111111",
                    isPinEnabled = true,
                    staffPin = "111111"
                )
            )

            val customerDao = db.customerDao()
            val c1Id = customerDao.insertCustomer(
                Customer(
                    name = "আব্দুর রহিম",
                    phone = "01811-223344",
                    address = "রোড #৩, ধানমন্ডি",
                    note = "নিয়মিত খরিদ্দার",
                    totalPurchase = 4500.0,
                    totalPaid = 3500.0,
                    currentDue = 1000.0
                )
            )
            val c2Id = customerDao.insertCustomer(
                Customer(
                    name = "মোসাঃ সালমা বেগম",
                    phone = "01922-334455",
                    address = "হাউস #১২, গ্রিন রোড",
                    note = "মাসিক হিসাব",
                    totalPurchase = 2800.0,
                    totalPaid = 2000.0,
                    currentDue = 800.0
                )
            )
            val c3Id = customerDao.insertCustomer(
                Customer(
                    name = "কামাল হোসেন",
                    phone = "01633-445566",
                    address = "কাঁচাবাজার লেন",
                    note = "",
                    totalPurchase = 1200.0,
                    totalPaid = 1200.0,
                    currentDue = 0.0
                )
            )

            // Initial transactions
            val customerTxDao = db.customerTransactionDao()
            customerTxDao.insertTransaction(
                CustomerTransaction(
                    customerId = c1Id,
                    customerName = "আব্দুর রহিম",
                    type = "SALE_DUE",
                    amount = 1000.0,
                    paymentMethod = "Due",
                    remainingDue = 1000.0,
                    note = "বাকি কেনাকাটা",
                    timestamp = System.currentTimeMillis() - 86400000L
                )
            )
            customerTxDao.insertTransaction(
                CustomerTransaction(
                    customerId = c2Id,
                    customerName = "মোসাঃ সালমা বেগম",
                    type = "SALE_DUE",
                    amount = 800.0,
                    paymentMethod = "Due",
                    remainingDue = 800.0,
                    note = "মুদি মালামাল বাকি",
                    timestamp = System.currentTimeMillis() - 43200000L
                )
            )

            val supplierDao = db.supplierDao()
            val s1Id = supplierDao.insertSupplier(
                Supplier(
                    name = "ইউনিলিভার বাংলাদেশ ডিস্ট্রিবিউটর",
                    phone = "01755-667788",
                    company = "ইউনিলিভার বাংলাদেশ",
                    address = "তেজগাঁও শিল্প এলাকা, ঢাকা",
                    note = "সাবান, শ্যাম্পু ও ডিটারজেন্ট",
                    totalPurchaseAmount = 15000.0,
                    totalPaidAmount = 10000.0,
                    currentDue = 5000.0
                )
            )
            val s2Id = supplierDao.insertSupplier(
                Supplier(
                    name = "প্রাণ-আরএফএল সাপ্লায়ার্স",
                    phone = "01866-778899",
                    company = "প্রাণ ফুডস",
                    address = "বাড্ডা, ঢাকা",
                    note = "তেল, চিপস ও বেকারি পণ্য",
                    totalPurchaseAmount = 8000.0,
                    totalPaidAmount = 6000.0,
                    currentDue = 2000.0
                )
            )

            val supplierTxDao = db.supplierTransactionDao()
            supplierTxDao.insertTransaction(
                SupplierTransaction(
                    supplierId = s1Id,
                    supplierName = "ইউনিলিভার বাংলাদেশ ডিস্ট্রিবিউটর",
                    type = "PURCHASE_DUE",
                    amount = 5000.0,
                    remainingDue = 5000.0,
                    note = "ইনভয়েস #UN-4402",
                    timestamp = System.currentTimeMillis() - 172800000L
                )
            )

            val productDao = db.productDao()
            productDao.insertProduct(
                Product(
                    name = "মিনিকেট চাল (৫০ কেজি)",
                    barcode = "89411001",
                    category = "চাল ও ডাল",
                    purchasePrice = 3200.0,
                    sellingPrice = 3500.0,
                    stockQuantity = 12.0,
                    minStockAlert = 3.0,
                    unit = "বস্তা"
                )
            )
            productDao.insertProduct(
                Product(
                    name = "তীর সয়াবিন তেল (৫ লিটার)",
                    barcode = "89411002",
                    category = "তেল ও ঘি",
                    purchasePrice = 790.0,
                    sellingPrice = 850.0,
                    stockQuantity = 4.0, // Low stock trigger
                    minStockAlert = 5.0,
                    unit = "বোতল"
                )
            )
            productDao.insertProduct(
                Product(
                    name = "লাক্স সাবান ১০০ গ্রাম",
                    barcode = "89411003",
                    category = "টয়লেট্রিজ",
                    purchasePrice = 52.0,
                    sellingPrice = 60.0,
                    stockQuantity = 45.0,
                    minStockAlert = 10.0,
                    unit = "পিস"
                )
            )
            productDao.insertProduct(
                Product(
                    name = "চিনি রিফাইন্ড ১ কেজি",
                    barcode = "89411004",
                    category = "মুদি সামগ্রী",
                    purchasePrice = 125.0,
                    sellingPrice = 135.0,
                    stockQuantity = 25.0,
                    minStockAlert = 8.0,
                    unit = "কেজি"
                )
            )
            productDao.insertProduct(
                Product(
                    name = "ডানো গুঁড়া দুধ ৫০০ গ্রাম",
                    barcode = "89411005",
                    category = "দুধ ও বেবি ফুড",
                    purchasePrice = 430.0,
                    sellingPrice = 470.0,
                    stockQuantity = 2.0, // Low stock trigger
                    minStockAlert = 5.0,
                    unit = "প্যাকেট"
                )
            )

            // Initial Expense
            val expenseDao = db.expenseDao()
            expenseDao.insertExpense(
                Expense(
                    category = "দোকান ভাড়া",
                    amount = 5000.0,
                    description = "চলতি মাসের দোকান ভাড়া অগ্রিম",
                    timestamp = System.currentTimeMillis() - 86400000L
                )
            )
            expenseDao.insertExpense(
                Expense(
                    category = "চা-নাস্তা ও আপ্যায়ন",
                    amount = 180.0,
                    description = "দোকানের চা ও বিস্কুট",
                    timestamp = System.currentTimeMillis()
                )
            )

            // Initial Cashbook
            val cashbookDao = db.cashbookDao()
            cashbookDao.insertEntry(
                CashbookEntry(
                    type = "IN",
                    category = "প্রারম্ভিক নগদ জমা",
                    amount = 10000.0,
                    balanceAfter = 10000.0,
                    paymentMethod = "Cash",
                    description = "দোকানের ওপেনিং ক্যাশ ব্যালেন্স",
                    timestamp = System.currentTimeMillis() - 86400000L
                )
            )
            cashbookDao.insertEntry(
                CashbookEntry(
                    type = "OUT",
                    category = "দোকান ভাড়া",
                    amount = 5000.0,
                    balanceAfter = 5000.0,
                    paymentMethod = "Cash",
                    description = "দোকান ভাড়া প্রদান",
                    timestamp = System.currentTimeMillis() - 86400000L
                )
            )
            cashbookDao.insertEntry(
                CashbookEntry(
                    type = "OUT",
                    category = "চা-নাস্তা ও আপ্যায়ন",
                    amount = 180.0,
                    balanceAfter = 4820.0,
                    paymentMethod = "Cash",
                    description = "দোকানের চা ও বিস্কুট",
                    timestamp = System.currentTimeMillis()
                )
            )
        }
    }
}
