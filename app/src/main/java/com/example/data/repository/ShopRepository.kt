package com.example.data.repository

import com.example.data.AppDatabase
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.*

class ShopRepository(private val db: AppDatabase) {
    // Customers
    val allCustomers: Flow<List<Customer>> = db.customerDao().getAllCustomers()
    val dueCustomers: Flow<List<Customer>> = db.customerDao().getDueCustomers()
    val totalCustomerDue: Flow<Double?> = db.customerDao().getTotalCustomerDue()

    suspend fun getCustomerById(id: Long) = db.customerDao().getCustomerById(id)
    suspend fun insertCustomer(customer: Customer) = db.customerDao().insertCustomer(customer)
    suspend fun updateCustomer(customer: Customer) = db.customerDao().updateCustomer(customer)
    suspend fun deleteCustomer(customer: Customer) = db.customerDao().deleteCustomer(customer)

    // Suppliers
    val allSuppliers: Flow<List<Supplier>> = db.supplierDao().getAllSuppliers()
    val dueSuppliers: Flow<List<Supplier>> = db.supplierDao().getDueSuppliers()
    val totalSupplierDue: Flow<Double?> = db.supplierDao().getTotalSupplierDue()

    suspend fun getSupplierById(id: Long) = db.supplierDao().getSupplierById(id)
    suspend fun insertSupplier(supplier: Supplier) = db.supplierDao().insertSupplier(supplier)
    suspend fun updateSupplier(supplier: Supplier) = db.supplierDao().updateSupplier(supplier)
    suspend fun deleteSupplier(supplier: Supplier) = db.supplierDao().deleteSupplier(supplier)

    // Products
    val allProducts: Flow<List<Product>> = db.productDao().getAllProducts()
    val lowStockProducts: Flow<List<Product>> = db.productDao().getLowStockProducts()

    suspend fun getProductById(id: Long) = db.productDao().getProductById(id)
    suspend fun insertProduct(product: Product) = db.productDao().insertProduct(product)
    suspend fun updateProduct(product: Product) = db.productDao().updateProduct(product)
    suspend fun deleteProduct(product: Product) = db.productDao().deleteProduct(product)

    // Sales & POS Execution
    val allSales: Flow<List<Sale>> = db.saleDao().getAllSales()
    val allSaleItems: Flow<List<SaleItem>> = db.saleDao().getAllSaleItems()

    suspend fun getSaleById(id: Long) = db.saleDao().getSaleById(id)
    suspend fun getSaleItems(saleId: Long) = db.saleDao().getItemsForSale(saleId)

    /**
     * Executes a complete Sale transaction:
     * 1. Inserts Sale & SaleItems
     * 2. Reduces stock for each product
     * 3. Updates Customer total purchase, total paid, and current due (if customer selected)
     * 4. Logs Customer Transaction (if due or payment)
     * 5. Adds entry into Cashbook if paid amount > 0
     */
    suspend fun recordSale(
        sale: Sale,
        items: List<SaleItem>
    ): Long {
        val saleId = db.saleDao().insertSale(sale)
        val itemsWithId = items.map { it.copy(saleId = saleId) }
        db.saleDao().insertSaleItems(itemsWithId)

        // 1. Stock reduction
        for (item in items) {
            db.productDao().reduceStock(item.productId, item.quantity)
        }

        // 2. Customer ledger & due update & Loyalty Points
        if (sale.customerId != null && sale.customerId > 0) {
            val customer = db.customerDao().getCustomerById(sale.customerId)
            if (customer != null) {
                val newTotalPurchase = customer.totalPurchase + sale.grandTotal
                val newTotalPaid = customer.totalPaid + sale.paidAmount
                val newDue = newTotalPurchase - newTotalPaid
                // Calculate loyalty points
                val remainingLoyaltyPoints = (customer.loyaltyPoints - sale.loyaltyPointsRedeemed + sale.loyaltyPointsEarned).coerceAtLeast(0)

                db.customerDao().updateCustomer(
                    customer.copy(
                        totalPurchase = newTotalPurchase,
                        totalPaid = newTotalPaid,
                        currentDue = newDue,
                        loyaltyPoints = remainingLoyaltyPoints
                    )
                )

                // Log customer transaction
                if (sale.dueAmount > 0) {
                    db.customerTransactionDao().insertTransaction(
                        CustomerTransaction(
                            customerId = customer.id,
                            customerName = customer.name,
                            type = "SALE_DUE",
                            amount = sale.dueAmount,
                            paymentMethod = sale.paymentMethod,
                            saleId = saleId,
                            remainingDue = newDue,
                            note = "বিক্রয় মেমো #${sale.invoiceNumber} (নগদ: ৳${sale.paidAmount}, বাকি: ৳${sale.dueAmount})"
                        )
                    )
                } else {
                    db.customerTransactionDao().insertTransaction(
                        CustomerTransaction(
                            customerId = customer.id,
                            customerName = customer.name,
                            type = "PAYMENT",
                            amount = sale.paidAmount,
                            paymentMethod = sale.paymentMethod,
                            saleId = saleId,
                            remainingDue = newDue,
                            note = "নগদ ক্রয় মেমো #${sale.invoiceNumber}"
                        )
                    )
                }
            }
        }

        // 3. Cashbook update if cash received
        if (sale.paidAmount > 0) {
            db.cashbookDao().insertEntry(
                CashbookEntry(
                    type = "IN",
                    category = "পণ্য বিক্রয়",
                    amount = sale.paidAmount,
                    paymentMethod = sale.paymentMethod,
                    referenceId = sale.invoiceNumber,
                    description = "বিক্রয় মেমো #${sale.invoiceNumber} (${sale.customerName})"
                )
            )
        }

        return saleId
    }

    /**
     * Customer settles due payment
     */
    suspend fun recordCustomerPayment(
        customerId: Long,
        amount: Double,
        paymentMethod: String,
        note: String
    ): Boolean {
        val customer = db.customerDao().getCustomerById(customerId) ?: return false
        val newTotalPaid = customer.totalPaid + amount
        val newDue = (customer.totalPurchase - newTotalPaid).coerceAtLeast(0.0)
        
        db.customerDao().updateCustomer(
            customer.copy(
                totalPaid = newTotalPaid,
                currentDue = newDue
            )
        )

        db.customerTransactionDao().insertTransaction(
            CustomerTransaction(
                customerId = customer.id,
                customerName = customer.name,
                type = "PAYMENT",
                amount = amount,
                paymentMethod = paymentMethod,
                remainingDue = newDue,
                note = if (note.isNotBlank()) note else "বাকি টাকা পরিশোধ ($paymentMethod)"
            )
        )

        // Add to cashbook
        db.cashbookDao().insertEntry(
            CashbookEntry(
                type = "IN",
                category = "কাস্টমার বাকি আদায়",
                amount = amount,
                paymentMethod = paymentMethod,
                description = "বাকি আদায়: ${customer.name}"
            )
        )
        return true
    }

    // Purchases & Stock In
    val allPurchases: Flow<List<Purchase>> = db.purchaseDao().getAllPurchases()

    suspend fun recordPurchase(
        purchase: Purchase,
        items: List<PurchaseItem>
    ): Long {
        val purchaseId = db.purchaseDao().insertPurchase(purchase)
        val itemsWithId = items.map { it.copy(purchaseId = purchaseId) }
        db.purchaseDao().insertPurchaseItems(itemsWithId)

        // Add to stock
        for (item in items) {
            db.productDao().addStock(item.productId, item.quantity)
        }

        // Update supplier balance
        if (purchase.supplierId != null && purchase.supplierId > 0) {
            val supplier = db.supplierDao().getSupplierById(purchase.supplierId)
            if (supplier != null) {
                val newPurchaseTotal = supplier.totalPurchaseAmount + purchase.totalAmount
                val newPaidTotal = supplier.totalPaidAmount + purchase.paidAmount
                val newDue = newPurchaseTotal - newPaidTotal
                db.supplierDao().updateSupplier(
                    supplier.copy(
                        totalPurchaseAmount = newPurchaseTotal,
                        totalPaidAmount = newPaidTotal,
                        currentDue = newDue
                    )
                )

                db.supplierTransactionDao().insertTransaction(
                    SupplierTransaction(
                        supplierId = supplier.id,
                        supplierName = supplier.name,
                        type = if (purchase.dueAmount > 0) "PURCHASE_DUE" else "PAYMENT",
                        amount = if (purchase.dueAmount > 0) purchase.dueAmount else purchase.paidAmount,
                        paymentMethod = purchase.paymentMethod,
                        purchaseId = purchaseId,
                        remainingDue = newDue,
                        note = "ক্রয় বিল #${purchaseId} (পেইড: ৳${purchase.paidAmount}, বাকি: ৳${purchase.dueAmount})"
                    )
                )
            }
        }

        // Cashbook deduction if paid
        if (purchase.paidAmount > 0) {
            db.cashbookDao().insertEntry(
                CashbookEntry(
                    type = "OUT",
                    category = "মালামাল ক্রয়",
                    amount = purchase.paidAmount,
                    paymentMethod = purchase.paymentMethod,
                    referenceId = purchaseId.toString(),
                    description = "সাপ্লায়ার: ${purchase.supplierName}"
                )
            )
        }

        return purchaseId
    }

    suspend fun recordSupplierPayment(
        supplierId: Long,
        amount: Double,
        paymentMethod: String,
        note: String
    ): Boolean {
        val supplier = db.supplierDao().getSupplierById(supplierId) ?: return false
        val newPaid = supplier.totalPaidAmount + amount
        val newDue = (supplier.totalPurchaseAmount - newPaid).coerceAtLeast(0.0)

        db.supplierDao().updateSupplier(
            supplier.copy(
                totalPaidAmount = newPaid,
                currentDue = newDue
            )
        )

        db.supplierTransactionDao().insertTransaction(
            SupplierTransaction(
                supplierId = supplier.id,
                supplierName = supplier.name,
                type = "PAYMENT",
                amount = amount,
                paymentMethod = paymentMethod,
                remainingDue = newDue,
                note = if (note.isNotBlank()) note else "সাপ্লায়ার দেনা পরিশোধ ($paymentMethod)"
            )
        )

        db.cashbookDao().insertEntry(
            CashbookEntry(
                type = "OUT",
                category = "সাপ্লায়ার দেনা পরিশোধ",
                amount = amount,
                paymentMethod = paymentMethod,
                description = "দেনা পরিশোধ: ${supplier.name}"
            )
        )
        return true
    }

    // Customer & Supplier Transactions Ledger
    fun getTransactionsForCustomer(customerId: Long): Flow<List<CustomerTransaction>> =
        db.customerTransactionDao().getTransactionsForCustomer(customerId)

    fun getRecentCustomerTransactions(): Flow<List<CustomerTransaction>> =
        db.customerTransactionDao().getRecentCustomerTransactions()

    fun getTransactionsForSupplier(supplierId: Long): Flow<List<SupplierTransaction>> =
        db.supplierTransactionDao().getTransactionsForSupplier(supplierId)

    // Expenses
    val allExpenses: Flow<List<Expense>> = db.expenseDao().getAllExpenses()

    suspend fun recordExpense(expense: Expense): Long {
        val id = db.expenseDao().insertExpense(expense)
        db.cashbookDao().insertEntry(
            CashbookEntry(
                type = "OUT",
                category = expense.category,
                amount = expense.amount,
                paymentMethod = expense.paymentMethod,
                referenceId = id.toString(),
                description = expense.description.ifBlank { expense.category }
            )
        )
        return id
    }

    suspend fun deleteExpense(expense: Expense) = db.expenseDao().deleteExpense(expense)

    // Cashbook
    val allCashbookEntries: Flow<List<CashbookEntry>> = db.cashbookDao().getAllEntries()

    suspend fun addCashbookEntry(entry: CashbookEntry) = db.cashbookDao().insertEntry(entry)

    // Settings
    val settingsFlow: Flow<ShopSettings?> = db.shopSettingsDao().getSettingsFlow()
    suspend fun getSettings() = db.shopSettingsDao().getSettings()
    suspend fun updateSettings(settings: ShopSettings) = db.shopSettingsDao().insertOrUpdate(settings)
}
