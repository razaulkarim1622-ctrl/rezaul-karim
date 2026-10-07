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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Product
import com.example.data.model.Supplier
import com.example.ui.theme.*
import com.example.util.FormatUtils
import com.example.viewmodel.ShopViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PurchasesScreen(
    viewModel: ShopViewModel
) {
    val context = LocalContext.current
    val purchases by viewModel.purchases.collectAsStateWithLifecycle()
    val suppliers by viewModel.suppliers.collectAsStateWithLifecycle()
    val products by viewModel.products.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()

    var showAddPurchaseDialog by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddPurchaseDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = androidx.compose.ui.graphics.Color.White,
                modifier = Modifier.testTag("add_purchase_fab")
            ) {
                Icon(Icons.Default.AddShoppingCart, contentDescription = "নতুন মালামাল ক্রয়")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("purchases_screen")
        ) {
            // Header summary
            val totalPurchaseAmount = remember(purchases) { purchases.sumOf { it.totalAmount } }
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = IndigoLight)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("মোট মালামাল ক্রয় ইনভয়েস", fontSize = 12.sp, color = NavyPrimary)
                        Text(
                            text = FormatUtils.formatCurrency(totalPurchaseAmount, settings.currencySymbol),
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = NavyPrimary
                        )
                    }
                    Text("মোট চালানের সংখ্যা: ${purchases.size} টি", fontSize = 12.sp, color = TextPrimaryLight)
                }
            }

            if (purchases.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.ShoppingBag, contentDescription = null, modifier = Modifier.size(56.dp), tint = androidx.compose.ui.graphics.Color.Gray)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("কোন মালামাল ক্রয়ের রেকর্ড নেই।", color = androidx.compose.ui.graphics.Color.Gray)
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(purchases) { purchase ->
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = purchase.supplierName,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp
                                        )
                                        Text(
                                            text = FormatUtils.formatDateTime(purchase.timestamp),
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = FormatUtils.formatCurrency(purchase.totalAmount, settings.currencySymbol),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = NavyPrimary
                                        )
                                        if (purchase.dueAmount > 0) {
                                            Text(
                                                text = "বাকি: ৳${purchase.dueAmount.toInt()}",
                                                color = WarningAmber,
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 11.sp
                                            )
                                        } else {
                                            Text(
                                                text = "সম্পূর্ণ পরিশোধিত",
                                                color = SuccessGreen,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                }

                                if (purchase.note.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "নোট: ${purchase.note}",
                                        fontSize = 11.sp,
                                        color = androidx.compose.ui.graphics.Color.DarkGray
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Purchase Dialog
    if (showAddPurchaseDialog) {
        var selectedSupplier by remember { mutableStateOf<Supplier?>(suppliers.firstOrNull()) }
        var selectedProduct by remember { mutableStateOf<Product?>(products.firstOrNull()) }
        var quantityInput by remember { mutableStateOf("1") }
        var unitPriceInput by remember { mutableStateOf(selectedProduct?.purchasePrice?.toString() ?: "0") }
        var paidInput by remember { mutableStateOf("0") }
        var noteInput by remember { mutableStateOf("") }

        var showSupplierDropdown by remember { mutableStateOf(false) }
        var showProductDropdown by remember { mutableStateOf(false) }

        val q = quantityInput.toDoubleOrNull() ?: 0.0
        val p = unitPriceInput.toDoubleOrNull() ?: 0.0
        val totalCost = q * p

        AlertDialog(
            onDismissRequest = { showAddPurchaseDialog = false },
            title = { Text("নতুন মালামাল স্টক ইন / ক্রয় হিসাব") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Supplier Selector
                    OutlinedButton(
                        onClick = { showSupplierDropdown = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("সাপ্লায়ার: ${selectedSupplier?.name ?: "সাপ্লায়ার নির্বাচন করুন"}")
                    }
                    DropdownMenu(
                        expanded = showSupplierDropdown,
                        onDismissRequest = { showSupplierDropdown = false }
                    ) {
                        suppliers.forEach { s ->
                            DropdownMenuItem(
                                text = { Text("${s.name} (${s.company})") },
                                onClick = {
                                    selectedSupplier = s
                                    showSupplierDropdown = false
                                }
                            )
                        }
                    }

                    // Product Selector
                    OutlinedButton(
                        onClick = { showProductDropdown = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("পণ্য: ${selectedProduct?.name ?: "পণ্য নির্বাচন করুন"}")
                    }
                    DropdownMenu(
                        expanded = showProductDropdown,
                        onDismissRequest = { showProductDropdown = false }
                    ) {
                        products.forEach { prod ->
                            DropdownMenuItem(
                                text = { Text("${prod.name} (বর্তমান স্টক: ${prod.stockQuantity})") },
                                onClick = {
                                    selectedProduct = prod
                                    unitPriceInput = prod.purchasePrice.toString()
                                    showProductDropdown = false
                                }
                            )
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = quantityInput,
                            onValueChange = { quantityInput = it },
                            label = { Text("ক্রয় পরিমাণ *") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = unitPriceInput,
                            onValueChange = { unitPriceInput = it },
                            label = { Text("দর / কেনা দাম *") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = IndigoLight),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("মোট বিল:", fontWeight = FontWeight.Bold, color = NavyPrimary)
                            Text(FormatUtils.formatCurrency(totalCost, settings.currencySymbol), fontWeight = FontWeight.Bold, color = NavyPrimary)
                        }
                    }

                    OutlinedTextField(
                        value = paidInput,
                        onValueChange = { paidInput = it },
                        label = { Text("সাপ্লায়ারকে পরিশোধকৃত টাকা (৳)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = noteInput,
                        onValueChange = { noteInput = it },
                        label = { Text("চালান / মেমো নম্বর বা নোট") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val prod = selectedProduct
                        if (prod == null || q <= 0 || p <= 0) {
                            Toast.makeText(context, "সঠিক পণ্য, পরিমাণ ও মূল্য প্রদান করুন", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        val paid = paidInput.toDoubleOrNull() ?: 0.0
                        viewModel.addPurchase(
                            supplier = selectedSupplier,
                            product = prod,
                            quantity = q,
                            unitPrice = p,
                            paid = paid,
                            note = noteInput
                        )
                        Toast.makeText(context, "মালামাল ক্রয় সফলভাবে যুক্ত ও স্টকে যোগ হয়েছে", Toast.LENGTH_SHORT).show()
                        showAddPurchaseDialog = false
                    },
                    modifier = Modifier.testTag("save_purchase_btn")
                ) {
                    Text("ক্রয় নিশ্চিত করুন")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddPurchaseDialog = false }) {
                    Text("বাতিল")
                }
            }
        )
    }
}
