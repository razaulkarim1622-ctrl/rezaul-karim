package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Customer
import com.example.data.model.Product
import com.example.data.model.Sale
import com.example.data.model.SaleItem
import com.example.ui.components.BarcodeScannerDialog
import com.example.ui.theme.*
import com.example.util.FormatUtils
import com.example.util.InvoicePdfGenerator
import com.example.util.Strings
import com.example.viewmodel.ShopViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PosSaleScreen(
    viewModel: ShopViewModel,
    onSaleCompleted: (Sale, List<SaleItem>) -> Unit
) {
    val context = LocalContext.current
    val products by viewModel.products.collectAsStateWithLifecycle()
    val customers by viewModel.customers.collectAsStateWithLifecycle()
    val cartItems by viewModel.cartItems.collectAsStateWithLifecycle()
    val selectedCustomer by viewModel.selectedCustomerForSale.collectAsStateWithLifecycle()
    val discount by viewModel.discount.collectAsStateWithLifecycle()
    val paymentMethod by viewModel.paymentMethod.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("সব পণ্য") }
    var showCheckoutDialog by remember { mutableStateOf(false) }
    var showSelectCustomerDialog by remember { mutableStateOf(false) }
    var showBarcodeScanner by remember { mutableStateOf(false) }

    val categories = remember(products) {
        listOf("সব পণ্য") + products.map { it.category }.distinct()
    }

    val filteredProducts = remember(products, searchQuery, selectedCategory) {
        products.filter { prod ->
            val matchCategory = selectedCategory == "সব পণ্য" || prod.category == selectedCategory
            val matchSearch = prod.name.contains(searchQuery, ignoreCase = true) ||
                    prod.barcode.contains(searchQuery, ignoreCase = true)
            matchCategory && matchSearch
        }
    }

    val subtotal = remember(cartItems) { cartItems.sumOf { it.subtotal } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("pos_sales_screen")
    ) {
        // Top POS Action Bar & Customer Selector
        Surface(
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                // Customer selection card
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Card(
                        onClick = { showSelectCustomerDialog = true },
                        colors = CardDefaults.cardColors(
                            containerColor = if (selectedCustomer != null) IndigoLight else MaterialTheme.colorScheme.surfaceVariant
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                tint = if (selectedCustomer != null) NavyPrimary else Color.Gray
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = selectedCustomer?.name ?: Strings.regularCashCustomer,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                if (selectedCustomer != null) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (selectedCustomer!!.currentDue > 0) {
                                            Text(
                                                text = "${Strings.totalDue}: ৳${selectedCustomer!!.currentDue}  •  ",
                                                fontSize = 11.sp,
                                                color = DueRed,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                        Text(
                                            text = "⭐ ${Strings.loyaltyPoints}: ${selectedCustomer!!.loyaltyPoints} pt",
                                            fontSize = 11.sp,
                                            color = WarningAmber,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (selectedCustomer != null) {
                        IconButton(onClick = { viewModel.selectCustomerForSale(null) }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear Customer")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Search Bar with Barcode Scanner Button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text(Strings.searchProduct) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = null)
                                }
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("pos_search_product_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    FilledTonalIconButton(
                        onClick = { showBarcodeScanner = true },
                        modifier = Modifier
                            .size(54.dp)
                            .testTag("pos_scan_barcode_btn")
                    ) {
                        Icon(
                            Icons.Default.QrCodeScanner,
                            contentDescription = Strings.scanBarcode,
                            tint = NavyPrimary
                        )
                    }
                }

                // Category Chips
                ScrollableTabRow(
                    selectedTabIndex = categories.indexOf(selectedCategory).coerceAtLeast(0),
                    edgePadding = 0.dp,
                    divider = {},
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    categories.forEach { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat) },
                            modifier = Modifier.padding(end = 6.dp)
                        )
                    }
                }
            }
        }

        // Product List Area
        Box(modifier = Modifier.weight(1f)) {
            if (filteredProducts.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Inventory2, contentDescription = null, modifier = Modifier.size(48.dp), tint = Color.Gray)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(Strings.cartEmpty, color = Color.Gray)
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredProducts) { product ->
                        val inCart = cartItems.find { it.product.id == product.id }

                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = product.name,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = FormatUtils.formatCurrency(product.sellingPrice, settings.currencySymbol),
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                        Text(
                                            text = " / ${product.unit}",
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontSize = 12.sp
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        if (product.barcode.isNotBlank()) {
                                            Text(
                                                text = "🔍 ${product.barcode}",
                                                color = Color.Gray,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                }

                                if (inCart != null) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        IconButton(
                                            onClick = { viewModel.updateCartQuantity(product.id, inCart.quantity - 1) },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(Icons.Default.Remove, contentDescription = "Decrease", tint = NavyPrimary)
                                        }
                                        Text(
                                            text = "${inCart.quantity.toInt()}",
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp)
                                        )
                                        IconButton(
                                            onClick = { viewModel.updateCartQuantity(product.id, inCart.quantity + 1) },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(Icons.Default.Add, contentDescription = "Increase", tint = NavyPrimary)
                                        }
                                    }
                                } else {
                                    Button(
                                        onClick = { viewModel.addToCart(product, 1.0) },
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Icon(Icons.Default.AddShoppingCart, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("যোগ")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Bottom POS Cart Summary Bar
        Surface(
            tonalElevation = 8.dp,
            shadowElevation = 8.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "${Strings.cartItemsCount} ${cartItems.size} টি",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = FormatUtils.formatCurrency(subtotal, settings.currencySymbol),
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Row {
                        if (cartItems.isNotEmpty()) {
                            OutlinedButton(
                                onClick = { viewModel.clearCart() },
                                modifier = Modifier.padding(end = 8.dp)
                            ) {
                                Text(Strings.cancel)
                            }
                        }

                        Button(
                            onClick = {
                                if (cartItems.isEmpty()) {
                                    Toast.makeText(context, "অনুগ্রহ করে কার্টে পণ্য যোগ করুন", Toast.LENGTH_SHORT).show()
                                } else {
                                    showCheckoutDialog = true
                                }
                            },
                            enabled = cartItems.isNotEmpty(),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("pos_checkout_button")
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(Strings.checkout, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // Camera Barcode Scanner Dialog
    if (showBarcodeScanner) {
        BarcodeScannerDialog(
            onBarcodeScanned = { barcode ->
                // Look up product by barcode
                val matched = products.find { it.barcode.equals(barcode.trim(), ignoreCase = true) }
                if (matched != null) {
                    viewModel.addToCart(matched, 1.0)
                    Toast.makeText(context, "পণ্য যুক্ত হয়েছে: ${matched.name}", Toast.LENGTH_SHORT).show()
                } else {
                    searchQuery = barcode
                    Toast.makeText(context, "বারকোড: $barcode (কোন পণ্য মেলেনি, সার্চে বসানো হয়েছে)", Toast.LENGTH_LONG).show()
                }
            },
            onDismiss = { showBarcodeScanner = false }
        )
    }

    // Customer Selection Dialog
    if (showSelectCustomerDialog) {
        AlertDialog(
            onDismissRequest = { showSelectCustomerDialog = false },
            title = { Text(Strings.customerSelector) },
            text = {
                LazyColumn(modifier = Modifier.heightIn(max = 350.dp)) {
                    item {
                        ListItem(
                            headlineContent = { Text(Strings.regularCashCustomer) },
                            leadingContent = { Icon(Icons.Default.PersonOutline, contentDescription = null) },
                            modifier = Modifier.clickable {
                                viewModel.selectCustomerForSale(null)
                                showSelectCustomerDialog = false
                            }
                        )
                    }
                    items(customers) { customer ->
                        ListItem(
                            headlineContent = { Text(customer.name, fontWeight = FontWeight.Bold) },
                            supportingContent = {
                                Column {
                                    Text("${customer.phone}  •  ${Strings.totalDue}: ৳${customer.currentDue}")
                                    Text("⭐ ${Strings.loyaltyPoints}: ${customer.loyaltyPoints} pt", color = WarningAmber, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            },
                            leadingContent = { Icon(Icons.Default.Person, contentDescription = null, tint = NavyPrimary) },
                            modifier = Modifier.clickable {
                                viewModel.selectCustomerForSale(customer)
                                showSelectCustomerDialog = false
                            }
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSelectCustomerDialog = false }) {
                    Text(Strings.close)
                }
            }
        )
    }

    // Checkout Details & Payment Dialog with LOYALTY POINTS REDEMPTION
    if (showCheckoutDialog) {
        var discountInput by remember { mutableStateOf(discount.toString()) }
        var currentMethod by remember { mutableStateOf(paymentMethod) }
        var redeemPointsInput by remember { mutableStateOf("0") }

        val customerLoyaltyPoints = selectedCustomer?.loyaltyPoints ?: 0
        val pointsToRedeem = (redeemPointsInput.toIntOrNull() ?: 0).coerceIn(0, customerLoyaltyPoints)
        val loyaltyDiscountValue = pointsToRedeem.toDouble() // 1 point = ৳1

        val manualDiscount = discountInput.toDoubleOrNull() ?: 0.0
        val totalCombinedDiscount = manualDiscount + loyaltyDiscountValue
        val effectiveGrandTotal = (subtotal - totalCombinedDiscount).coerceAtLeast(0.0)

        var paidInput by remember { mutableStateOf(effectiveGrandTotal.toString()) }

        // Recalculate paid if total changes
        LaunchedEffect(effectiveGrandTotal) {
            paidInput = effectiveGrandTotal.toString()
        }

        val currentPaidVal = paidInput.toDoubleOrNull() ?: 0.0
        val remainingDueVal = (effectiveGrandTotal - currentPaidVal).coerceAtLeast(0.0)
        val pointsEarnedThisSale = if (selectedCustomer != null) (effectiveGrandTotal / 100.0).toInt() else 0

        val paymentMethods = listOf("Cash", "বিকাশ", "নগদ", "রকেট", "ব্যাংক", "বাকি (Due)")

        AlertDialog(
            onDismissRequest = { showCheckoutDialog = false },
            title = {
                Text(
                    text = Strings.confirmSale,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        Text(
                            text = "ক্রেতা: ${selectedCustomer?.name ?: Strings.regularCashCustomer}",
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // Subtotal
                    item {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(Strings.subtotal)
                            Text(FormatUtils.formatCurrency(subtotal, settings.currencySymbol), fontWeight = FontWeight.Bold)
                        }
                    }

                    // LOYALTY PROGRAM CARD (if customer selected)
                    if (selectedCustomer != null && customerLoyaltyPoints > 0) {
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = WarningAmberLight),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "⭐ ${Strings.loyaltyPoints}",
                                            fontWeight = FontWeight.Bold,
                                            color = WarningAmber
                                        )
                                        Text(
                                            text = "${Strings.pointsAvailable} $customerLoyaltyPoints pt",
                                            fontWeight = FontWeight.Bold,
                                            color = NavyDark
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        OutlinedTextField(
                                            value = redeemPointsInput,
                                            onValueChange = {
                                                val parsed = it.filter { char -> char.isDigit() }
                                                redeemPointsInput = parsed
                                            },
                                            label = { Text("রিডিম পয়েন্ট সংখ্যা (১ pt = ৳১)") },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            singleLine = true,
                                            modifier = Modifier.weight(1f)
                                        )

                                        Spacer(modifier = Modifier.width(6.dp))

                                        TextButton(onClick = {
                                            // Max possible points to redeem (cannot exceed subtotal)
                                            val maxPossible = customerLoyaltyPoints.coerceAtMost(subtotal.toInt())
                                            redeemPointsInput = maxPossible.toString()
                                        }) {
                                            Text("সব পয়েন্ট")
                                        }
                                    }

                                    if (loyaltyDiscountValue > 0) {
                                        Text(
                                            text = "পয়েন্ট থেকে ছাড়: - ৳${loyaltyDiscountValue.toInt()}",
                                            color = SuccessGreen,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Manual Discount field
                    item {
                        OutlinedTextField(
                            value = discountInput,
                            onValueChange = { discountInput = it },
                            label = { Text(Strings.discount) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Grand Total Display
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = IndigoLight),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(Strings.grandTotal, fontWeight = FontWeight.Bold, color = NavyPrimary)
                                    Text(
                                        text = FormatUtils.formatCurrency(effectiveGrandTotal, settings.currencySymbol),
                                        fontWeight = FontWeight.Bold,
                                        color = NavyPrimary
                                    )
                                }
                                if (selectedCustomer != null && pointsEarnedThisSale > 0) {
                                    Text(
                                        text = "🎁 ${Strings.pointsEarnedMsg} +$pointsEarnedThisSale pt",
                                        color = WarningAmber,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }

                    // Paid Amount Input
                    item {
                        OutlinedTextField(
                            value = paidInput,
                            onValueChange = { paidInput = it },
                            label = { Text(Strings.paidAmount) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("pos_paid_amount_input")
                        )
                    }

                    // Remaining Due Display
                    if (remainingDueVal > 0) {
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = DueRedLight),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(Strings.remainingDue, fontWeight = FontWeight.Bold, color = DueRed)
                                    Text(
                                        text = FormatUtils.formatCurrency(remainingDueVal, settings.currencySymbol),
                                        fontWeight = FontWeight.Bold,
                                        color = DueRed
                                    )
                                }
                            }
                        }
                        if (selectedCustomer == null) {
                            item {
                                Text(
                                    "সতর্কতা: বাকি বিক্রির জন্য অবশ্যই কাস্টমার নির্বাচন করতে হবে।",
                                    color = DueRed,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    // Payment method chips
                    item {
                        Text(Strings.paymentMethod, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            paymentMethods.take(3).forEach { method ->
                                FilterChip(
                                    selected = currentMethod == method,
                                    onClick = { currentMethod = method },
                                    label = { Text(method, fontSize = 11.sp) }
                                )
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            paymentMethods.drop(3).forEach { method ->
                                FilterChip(
                                    selected = currentMethod == method,
                                    onClick = { currentMethod = method },
                                    label = { Text(method, fontSize = 11.sp) }
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (remainingDueVal > 0 && selectedCustomer == null) {
                            Toast.makeText(context, "বাকি বিক্রি করতে কাস্টমার নির্বাচন করুন", Toast.LENGTH_LONG).show()
                            return@Button
                        }
                        viewModel.setDiscount(manualDiscount)
                        viewModel.setRedeemedPoints(pointsToRedeem)
                        viewModel.setPaidAmount(currentPaidVal)
                        viewModel.setPaymentMethod(currentMethod)

                        viewModel.checkoutSale { sale, items ->
                            showCheckoutDialog = false
                            Toast.makeText(context, "বিক্রয় সফল হয়েছে! মেমো: ${sale.invoiceNumber}", Toast.LENGTH_LONG).show()
                            onSaleCompleted(sale, items)
                            InvoicePdfGenerator.printOrShareSaleInvoice(context, sale, items, settings)
                        }
                    },
                    modifier = Modifier.testTag("pos_confirm_checkout_btn")
                ) {
                    Text(Strings.confirmSale)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCheckoutDialog = false }) {
                    Text(Strings.cancel)
                }
            }
        )
    }
}
