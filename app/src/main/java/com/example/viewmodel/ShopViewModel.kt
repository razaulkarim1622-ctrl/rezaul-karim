package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.model.*
import com.example.data.repository.ShopRepository
import com.example.util.FormatUtils
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

// Pos Cart Item
data class CartItem(
    val product: Product,
    val quantity: Double,
    val unitPrice: Double
) {
    val subtotal: Double get() = quantity * unitPrice
    val totalCost: Double get() = quantity * product.purchasePrice
}

data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

data class DashboardSummary(
    val todaySales: Double = 0.0,
    val todayCollection: Double = 0.0,
    val todayDueSales: Double = 0.0,
    val totalCustomerDue: Double = 0.0,
    val totalSupplierDue: Double = 0.0,
    val todayExpense: Double = 0.0,
    val todayGrossProfit: Double = 0.0,
    val todayNetProfit: Double = 0.0,
    val totalProductsCount: Int = 0,
    val lowStockCount: Int = 0,
    val totalCustomersCount: Int = 0,
    val totalSuppliersCount: Int = 0
)

class ShopViewModel(application: Application) : AndroidViewModel(application) {
    val repository: ShopRepository

    init {
        val db = AppDatabase.getDatabase(application)
        repository = ShopRepository(db)
    }

    // State flows from Room
    val customers: StateFlow<List<Customer>> = repository.allCustomers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dueCustomers: StateFlow<List<Customer>> = repository.dueCustomers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val suppliers: StateFlow<List<Supplier>> = repository.allSuppliers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dueSuppliers: StateFlow<List<Supplier>> = repository.dueSuppliers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val products: StateFlow<List<Product>> = repository.allProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val lowStockProducts: StateFlow<List<Product>> = repository.lowStockProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val sales: StateFlow<List<Sale>> = repository.allSales
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val purchases: StateFlow<List<Purchase>> = repository.allPurchases
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val expenses: StateFlow<List<Expense>> = repository.allExpenses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val cashbookEntries: StateFlow<List<CashbookEntry>> = repository.allCashbookEntries
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentCustomerTransactions: StateFlow<List<CustomerTransaction>> = repository.getRecentCustomerTransactions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val settings: StateFlow<ShopSettings> = repository.settingsFlow
        .map { it ?: ShopSettings() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ShopSettings())

    // POS Cart State
    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    private val _selectedCustomerForSale = MutableStateFlow<Customer?>(null)
    val selectedCustomerForSale: StateFlow<Customer?> = _selectedCustomerForSale.asStateFlow()

    private val _discount = MutableStateFlow(0.0)
    val discount: StateFlow<Double> = _discount.asStateFlow()

    private val _paidAmount = MutableStateFlow(0.0)
    val paidAmount: StateFlow<Double> = _paidAmount.asStateFlow()

    private val _paymentMethod = MutableStateFlow("Cash")
    val paymentMethod: StateFlow<String> = _paymentMethod.asStateFlow()

    private val _redeemedPoints = MutableStateFlow(0)
    val redeemedPoints: StateFlow<Int> = _redeemedPoints.asStateFlow()

    // Calculated Dashboard Summary
    val dashboardSummary: StateFlow<DashboardSummary> = combine(
        combine(sales, expenses, customers) { s, e, c -> Triple(s, e, c) },
        combine(suppliers, products, lowStockProducts, cashbookEntries) { sup, p, low, cash ->
            Quad(sup, p, low, cash)
        }
    ) { (salesList, expensesList, custList), (suppList, prodList, lowStockList, cashEntries) ->
        val startOfToday = getStartOfDayTimestamp()
        val endOfToday = getEndOfDayTimestamp()

        val todaySalesList = salesList.filter { it.timestamp in startOfToday..endOfToday }
        val todayExpensesList = expensesList.filter { it.timestamp in startOfToday..endOfToday }

        val todaySalesTotal = todaySalesList.sumOf { it.grandTotal }
        val todayDueSalesTotal = todaySalesList.sumOf { it.dueAmount }

        val todayCollectionTotal = cashEntries
            .filter { it.type == "IN" && it.timestamp in startOfToday..endOfToday }
            .sumOf { it.amount }

        val totalCustDue = custList.sumOf { it.currentDue }
        val totalSuppDue = suppList.sumOf { it.currentDue }
        val todayExpenseTotal = todayExpensesList.sumOf { it.amount }

        // Approximate Gross profit today from sales (selling price - estimated cost)
        var grossProfit = 0.0
        todaySalesList.forEach { sale ->
            grossProfit += (sale.grandTotal * 0.15) // Fallback default markup or accurate from items
        }
        val netProfit = (grossProfit - todayExpenseTotal).coerceAtLeast(-999999.0)

        DashboardSummary(
            todaySales = todaySalesTotal,
            todayCollection = todayCollectionTotal,
            todayDueSales = todayDueSalesTotal,
            totalCustomerDue = totalCustDue,
            totalSupplierDue = totalSuppDue,
            todayExpense = todayExpenseTotal,
            todayGrossProfit = grossProfit,
            todayNetProfit = netProfit,
            totalProductsCount = prodList.size,
            lowStockCount = lowStockList.size,
            totalCustomersCount = custList.size,
            totalSuppliersCount = suppList.size
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardSummary())

    // Cart Actions
    fun addToCart(product: Product, quantity: Double = 1.0) {
        val current = _cartItems.value.toMutableList()
        val index = current.indexOfFirst { it.product.id == product.id }
        if (index >= 0) {
            val item = current[index]
            current[index] = item.copy(quantity = item.quantity + quantity)
        } else {
            current.add(CartItem(product = product, quantity = quantity, unitPrice = product.sellingPrice))
        }
        _cartItems.value = current
    }

    fun updateCartQuantity(productId: Long, newQuantity: Double) {
        if (newQuantity <= 0) {
            removeFromCart(productId)
        } else {
            _cartItems.value = _cartItems.value.map {
                if (it.product.id == productId) it.copy(quantity = newQuantity) else it
            }
        }
    }

    fun removeFromCart(productId: Long) {
        _cartItems.value = _cartItems.value.filter { it.product.id != productId }
    }

    fun clearCart() {
        _cartItems.value = emptyList()
        _discount.value = 0.0
        _paidAmount.value = 0.0
        _redeemedPoints.value = 0
        _selectedCustomerForSale.value = null
        _paymentMethod.value = "Cash"
    }

    fun setDiscount(amount: Double) {
        _discount.value = amount.coerceAtLeast(0.0)
    }

    fun setRedeemedPoints(points: Int) {
        _redeemedPoints.value = points.coerceAtLeast(0)
    }

    fun setPaidAmount(amount: Double) {
        _paidAmount.value = amount.coerceAtLeast(0.0)
    }

    fun setPaymentMethod(method: String) {
        _paymentMethod.value = method
    }

    fun selectCustomerForSale(customer: Customer?) {
        _selectedCustomerForSale.value = customer
        _redeemedPoints.value = 0
    }

    fun checkoutSale(onSuccess: (Sale, List<SaleItem>) -> Unit) {
        val items = _cartItems.value
        if (items.isEmpty()) return

        val subtotal = items.sumOf { it.subtotal }
        // Loyalty discount: 1 point = 1 currency unit
        val loyaltyDiscount = _redeemedPoints.value.toDouble()
        val totalDiscount = _discount.value + loyaltyDiscount
        val grandTotal = (subtotal - totalDiscount).coerceAtLeast(0.0)
        val paid = _paidAmount.value.coerceAtMost(grandTotal)
        val due = (grandTotal - paid).coerceAtLeast(0.0)

        val customer = _selectedCustomerForSale.value
        val invoiceNumber = FormatUtils.generateInvoiceNumber()

        // Calculate points earned: 1 point per 100 spent (rounded down)
        val pointsEarned = if (customer != null) (grandTotal / 100.0).toInt() else 0

        val sale = Sale(
            invoiceNumber = invoiceNumber,
            customerId = customer?.id,
            customerName = customer?.name ?: "সাধারণ খরিদ্দার (Cash)",
            customerPhone = customer?.phone ?: "",
            subtotal = subtotal,
            discount = totalDiscount,
            grandTotal = grandTotal,
            paidAmount = paid,
            dueAmount = due,
            paymentMethod = _paymentMethod.value,
            loyaltyPointsRedeemed = _redeemedPoints.value,
            loyaltyPointsEarned = pointsEarned,
            timestamp = System.currentTimeMillis()
        )

        val saleItems = items.map {
            SaleItem(
                saleId = 0,
                productId = it.product.id,
                productName = it.product.name,
                unit = it.product.unit,
                quantity = it.quantity,
                purchasePrice = it.product.purchasePrice,
                unitPrice = it.unitPrice,
                totalPrice = it.subtotal
            )
        }

        viewModelScope.launch {
            val saleId = repository.recordSale(sale, saleItems)
            val completedSale = sale.copy(id = saleId)
            clearCart()
            onSuccess(completedSale, saleItems)
        }
    }

    // Customer operations
    fun addCustomer(customer: Customer) = viewModelScope.launch {
        val id = repository.insertCustomer(customer)
        if (customer.currentDue > 0) {
            repository.db.customerTransactionDao().insertTransaction(
                CustomerTransaction(
                    customerId = id,
                    customerName = customer.name,
                    type = "INITIAL_DUE",
                    amount = customer.currentDue,
                    paymentMethod = "Due",
                    remainingDue = customer.currentDue,
                    note = "পূর্বের প্রারম্ভিক বাকি"
                )
            )
        }
    }

    fun updateCustomer(customer: Customer) = viewModelScope.launch {
        repository.updateCustomer(customer)
    }

    fun deleteCustomer(customer: Customer) = viewModelScope.launch {
        repository.deleteCustomer(customer)
    }

    fun collectCustomerPayment(customerId: Long, amount: Double, method: String, note: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.recordCustomerPayment(customerId, amount, method, note)
            onComplete()
        }
    }

    fun addCustomerDueManually(customerId: Long, amount: Double, note: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            val cust = repository.getCustomerById(customerId) ?: return@launch
            val newTotalPurchase = cust.totalPurchase + amount
            val newDue = cust.currentDue + amount
            repository.updateCustomer(cust.copy(totalPurchase = newTotalPurchase, currentDue = newDue))
            repository.db.customerTransactionDao().insertTransaction(
                CustomerTransaction(
                    customerId = cust.id,
                    customerName = cust.name,
                    type = "SALE_DUE",
                    amount = amount,
                    paymentMethod = "Due",
                    remainingDue = newDue,
                    note = if (note.isNotBlank()) note else "হাতে লেখা বাকি"
                )
            )
            onComplete()
        }
    }

    // Supplier operations
    fun addSupplier(supplier: Supplier) = viewModelScope.launch {
        val id = repository.insertSupplier(supplier)
        if (supplier.currentDue > 0) {
            repository.db.supplierTransactionDao().insertTransaction(
                SupplierTransaction(
                    supplierId = id,
                    supplierName = supplier.name,
                    type = "PURCHASE_DUE",
                    amount = supplier.currentDue,
                    remainingDue = supplier.currentDue,
                    note = "পূর্বের প্রারম্ভিক দেনা"
                )
            )
        }
    }

    fun updateSupplier(supplier: Supplier) = viewModelScope.launch {
        repository.updateSupplier(supplier)
    }

    fun deleteSupplier(supplier: Supplier) = viewModelScope.launch {
        repository.deleteSupplier(supplier)
    }

    fun paySupplier(supplierId: Long, amount: Double, method: String, note: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.recordSupplierPayment(supplierId, amount, method, note)
            onComplete()
        }
    }

    // Product operations
    fun addProduct(product: Product) = viewModelScope.launch {
        repository.insertProduct(product)
    }

    fun updateProduct(product: Product) = viewModelScope.launch {
        repository.updateProduct(product)
    }

    fun deleteProduct(product: Product) = viewModelScope.launch {
        repository.deleteProduct(product)
    }

    // Purchase operations
    fun addPurchase(
        supplier: Supplier?,
        product: Product,
        quantity: Double,
        unitPrice: Double,
        paid: Double,
        note: String
    ) = viewModelScope.launch {
        val total = quantity * unitPrice
        val due = (total - paid).coerceAtLeast(0.0)

        val purchase = Purchase(
            supplierId = supplier?.id,
            supplierName = supplier?.name ?: "সাধারণ সাপ্লায়ার",
            supplierPhone = supplier?.phone ?: "",
            totalAmount = total,
            paidAmount = paid,
            dueAmount = due,
            paymentMethod = "Cash",
            note = note
        )

        val item = PurchaseItem(
            purchaseId = 0,
            productId = product.id,
            productName = product.name,
            quantity = quantity,
            unitPrice = unitPrice,
            totalPrice = total
        )

        repository.recordPurchase(purchase, listOf(item))
    }

    // Expense operations
    fun addExpense(category: String, amount: Double, desc: String, method: String) = viewModelScope.launch {
        repository.recordExpense(
            Expense(
                category = category,
                amount = amount,
                description = desc,
                paymentMethod = method
            )
        )
    }

    fun deleteExpense(expense: Expense) = viewModelScope.launch {
        repository.deleteExpense(expense)
    }

    // Cashbook operations
    fun addCashbookEntry(type: String, category: String, amount: Double, method: String, desc: String) = viewModelScope.launch {
        repository.addCashbookEntry(
            CashbookEntry(
                type = type,
                category = category,
                amount = amount,
                paymentMethod = method,
                description = desc
            )
        )
    }

    // Settings
    fun updateSettings(newSettings: ShopSettings) = viewModelScope.launch {
        repository.updateSettings(newSettings)
    }

    // Transactions Flow
    fun getTransactionsForCustomer(customerId: Long): Flow<List<CustomerTransaction>> =
        repository.getTransactionsForCustomer(customerId)

    fun getTransactionsForSupplier(supplierId: Long): Flow<List<SupplierTransaction>> =
        repository.getTransactionsForSupplier(supplierId)

    suspend fun getSaleItems(saleId: Long): List<SaleItem> = repository.getSaleItems(saleId)

    private fun getStartOfDayTimestamp(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    private fun getEndOfDayTimestamp(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        return calendar.timeInMillis
    }
}
