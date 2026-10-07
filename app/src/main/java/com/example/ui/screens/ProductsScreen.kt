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
import com.example.data.model.Product
import com.example.ui.components.BarcodeScannerDialog
import com.example.ui.theme.*
import com.example.util.FormatUtils
import com.example.util.Strings
import com.example.viewmodel.ShopViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductsScreen(
    viewModel: ShopViewModel
) {
    val context = LocalContext.current
    val products by viewModel.products.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("সব") }
    var showAddEditDialog by remember { mutableStateOf(false) }
    var editingProduct by remember { mutableStateOf<Product?>(null) }
    var productToDelete by remember { mutableStateOf<Product?>(null) }

    var showBarcodeScannerForSearch by remember { mutableStateOf(false) }
    var showBarcodeScannerForForm by remember { mutableStateOf(false) }
    var scannedBarcodeForForm by remember { mutableStateOf("") }

    val categories = remember(products) {
        listOf("সব") + products.map { it.category }.distinct()
    }

    val filteredProducts = remember(products, searchQuery, selectedCategory) {
        products.filter { prod ->
            val matchCategory = selectedCategory == "সব" || prod.category == selectedCategory
            val matchSearch = prod.name.contains(searchQuery, ignoreCase = true) ||
                    prod.barcode.contains(searchQuery, ignoreCase = true) ||
                    prod.category.contains(searchQuery, ignoreCase = true)
            matchCategory && matchSearch
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingProduct = null
                    scannedBarcodeForForm = ""
                    showAddEditDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                modifier = Modifier.testTag("add_product_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = Strings.addProduct)
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("products_screen")
        ) {
            // Search & Filter header
            Surface(tonalElevation = 2.dp, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
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
                                .testTag("product_search_input"),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        FilledTonalIconButton(
                            onClick = { showBarcodeScannerForSearch = true },
                            modifier = Modifier.size(54.dp)
                        ) {
                            Icon(Icons.Default.QrCodeScanner, contentDescription = Strings.scanBarcode, tint = NavyPrimary)
                        }
                    }

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

            // Products List
            if (filteredProducts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Inventory2, contentDescription = null, modifier = Modifier.size(56.dp), tint = Color.Gray)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(Strings.cartEmpty, fontWeight = FontWeight.Bold, color = Color.Gray)
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredProducts) { product ->
                        val isLowStock = product.stockQuantity <= product.minStockAlert

                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = product.name,
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                        Text(
                                            text = "${Strings.category}: ${product.category} ${if (product.barcode.isNotBlank()) "• 🔍 ${product.barcode}" else ""}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    IconButton(onClick = {
                                        editingProduct = product
                                        scannedBarcodeForForm = product.barcode
                                        showAddEditDialog = true
                                    }) {
                                        Icon(Icons.Default.Edit, contentDescription = Strings.editProduct, tint = NavyPrimary)
                                    }
                                    IconButton(onClick = { productToDelete = product }) {
                                        Icon(Icons.Default.Delete, contentDescription = Strings.deleteProduct, tint = DueRed)
                                    }
                                }

                                Divider(modifier = Modifier.padding(vertical = 8.dp), color = BorderLight)

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(Strings.sellingPrice, fontSize = 11.sp, color = Color.Gray)
                                        Text(
                                            text = "${FormatUtils.formatCurrency(product.sellingPrice, settings.currencySymbol)} / ${product.unit}",
                                            fontWeight = FontWeight.Bold,
                                            color = SuccessGreen,
                                            fontSize = 14.sp
                                        )
                                    }

                                    Column {
                                        Text(Strings.purchasePrice, fontSize = 11.sp, color = Color.Gray)
                                        Text(
                                            text = FormatUtils.formatCurrency(product.purchasePrice, settings.currencySymbol),
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 13.sp
                                        )
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("বর্তমান স্টক", fontSize = 11.sp, color = Color.Gray)
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            if (isLowStock) {
                                                Icon(
                                                    Icons.Default.Warning,
                                                    contentDescription = "Low Stock",
                                                    tint = DueRed,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                            }
                                            Text(
                                                text = "${product.stockQuantity} ${product.unit}",
                                                fontWeight = FontWeight.Bold,
                                                color = if (isLowStock) DueRed else NavyDark,
                                                fontSize = 14.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Barcode scanner for inventory search
    if (showBarcodeScannerForSearch) {
        BarcodeScannerDialog(
            onBarcodeScanned = { barcode ->
                searchQuery = barcode
                Toast.makeText(context, "বারকোড সার্চ: $barcode", Toast.LENGTH_SHORT).show()
            },
            onDismiss = { showBarcodeScannerForSearch = false }
        )
    }

    // Add or Edit Product Dialog
    if (showAddEditDialog) {
        var name by remember { mutableStateOf(editingProduct?.name ?: "") }
        var category by remember { mutableStateOf(editingProduct?.category ?: "মুদি সামগ্রী") }
        var unit by remember { mutableStateOf(editingProduct?.unit ?: "টি") }
        var purchasePrice by remember { mutableStateOf(editingProduct?.purchasePrice?.toString() ?: "") }
        var sellingPrice by remember { mutableStateOf(editingProduct?.sellingPrice?.toString() ?: "") }
        var stockQuantity by remember { mutableStateOf(editingProduct?.stockQuantity?.toString() ?: "") }
        var minStockAlert by remember { mutableStateOf(editingProduct?.minStockAlert?.toString() ?: "5") }
        var barcode by remember { mutableStateOf(if (scannedBarcodeForForm.isNotBlank()) scannedBarcodeForForm else (editingProduct?.barcode ?: "")) }

        AlertDialog(
            onDismissRequest = { showAddEditDialog = false },
            title = { Text(if (editingProduct == null) Strings.addProduct else Strings.editProduct) },
            text = {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text(Strings.productName) },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("product_name_input")
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = category,
                            onValueChange = { category = it },
                            label = { Text(Strings.category) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = purchasePrice,
                                onValueChange = { purchasePrice = it },
                                label = { Text(Strings.purchasePrice) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = sellingPrice,
                                onValueChange = { sellingPrice = it },
                                label = { Text(Strings.sellingPrice) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    item {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = stockQuantity,
                                onValueChange = { stockQuantity = it },
                                label = { Text(Strings.stockQty) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = unit,
                                onValueChange = { unit = it },
                                label = { Text(Strings.unit) },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    item {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = barcode,
                                onValueChange = { barcode = it },
                                label = { Text(Strings.barcode) },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            FilledTonalIconButton(onClick = { showBarcodeScannerForForm = true }) {
                                Icon(Icons.Default.QrCodeScanner, contentDescription = Strings.scanBarcode, tint = NavyPrimary)
                            }
                        }
                    }
                    item {
                        OutlinedTextField(
                            value = minStockAlert,
                            onValueChange = { minStockAlert = it },
                            label = { Text(Strings.minStockLimit) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (name.isBlank() || sellingPrice.toDoubleOrNull() == null) {
                            Toast.makeText(context, "পণ্যের নাম ও বিক্রয় মূল্য পূরণ করুন", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        val product = Product(
                            id = editingProduct?.id ?: 0,
                            name = name.trim(),
                            category = category.trim().ifBlank { "সাধারণ" },
                            unit = unit.trim().ifBlank { "টি" },
                            purchasePrice = purchasePrice.toDoubleOrNull() ?: 0.0,
                            sellingPrice = sellingPrice.toDoubleOrNull() ?: 0.0,
                            stockQuantity = stockQuantity.toDoubleOrNull() ?: 0.0,
                            minStockAlert = minStockAlert.toDoubleOrNull() ?: 5.0,
                            barcode = barcode.trim()
                        )
                        if (editingProduct == null) {
                            viewModel.addProduct(product)
                            Toast.makeText(context, "পণ্য সফলভাবে যুক্ত হয়েছে", Toast.LENGTH_SHORT).show()
                        } else {
                            viewModel.updateProduct(product)
                            Toast.makeText(context, "পণ্য তথ্য আপডেট হয়েছে", Toast.LENGTH_SHORT).show()
                        }
                        showAddEditDialog = false
                    },
                    modifier = Modifier.testTag("save_product_btn")
                ) {
                    Text(Strings.save)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddEditDialog = false }) {
                    Text(Strings.cancel)
                }
            }
        )

        // Barcode scanner inside form dialog
        if (showBarcodeScannerForForm) {
            BarcodeScannerDialog(
                onBarcodeScanned = { scanned ->
                    barcode = scanned
                    scannedBarcodeForForm = scanned
                    Toast.makeText(context, "বারকোড স্ক্যান হয়েছে: $scanned", Toast.LENGTH_SHORT).show()
                },
                onDismiss = { showBarcodeScannerForForm = false }
            )
        }
    }

    // Delete Confirmation Dialog
    if (productToDelete != null) {
        AlertDialog(
            onDismissRequest = { productToDelete = null },
            title = { Text(Strings.deleteProduct) },
            text = { Text("'${productToDelete?.name}' ${Strings.deleteProduct}?") },
            confirmButton = {
                Button(
                    onClick = {
                        productToDelete?.let { viewModel.deleteProduct(it) }
                        productToDelete = null
                        Toast.makeText(context, "পণ্য মুছে ফেলা হয়েছে", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DueRed)
                ) {
                    Text(Strings.delete)
                }
            },
            dismissButton = {
                TextButton(onClick = { productToDelete = null }) {
                    Text(Strings.cancel)
                }
            }
        )
    }
}
