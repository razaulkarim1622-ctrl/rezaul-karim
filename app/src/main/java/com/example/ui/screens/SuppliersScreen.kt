package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Supplier
import com.example.ui.theme.*
import com.example.util.FormatUtils
import com.example.viewmodel.ShopViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuppliersScreen(
    viewModel: ShopViewModel
) {
    val context = LocalContext.current
    val suppliers by viewModel.suppliers.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    var showAddSupplierDialog by remember { mutableStateOf(false) }
    var supplierToEdit by remember { mutableStateOf<Supplier?>(null) }
    var supplierForPayment by remember { mutableStateOf<Supplier?>(null) }
    var selectedSupplierForLedger by remember { mutableStateOf<Supplier?>(null) }

    val totalSupplierDue = remember(suppliers) { suppliers.sumOf { it.currentDue } }

    val filteredSuppliers = remember(suppliers, searchQuery) {
        suppliers.filter { supp ->
            supp.name.contains(searchQuery, ignoreCase = true) ||
                    supp.company.contains(searchQuery, ignoreCase = true) ||
                    supp.phone.contains(searchQuery, ignoreCase = true)
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    supplierToEdit = null
                    showAddSupplierDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                modifier = Modifier.testTag("add_supplier_fab")
            ) {
                Icon(Icons.Default.AddBusiness, contentDescription = "নতুন সাপ্লায়ার")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("suppliers_screen")
        ) {
            // Header Stats Banner
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = WarningAmberLight)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("মোট মহাজন / সাপ্লায়ার দেনা", fontSize = 12.sp, color = WarningAmber)
                        Text(
                            text = FormatUtils.formatCurrency(totalSupplierDue, settings.currencySymbol),
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = WarningAmber
                        )
                    }
                    Text(
                        text = "মোট: ${suppliers.size} জন",
                        fontWeight = FontWeight.SemiBold,
                        color = NavyDark,
                        fontSize = 13.sp
                    )
                }
            }

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("সাপ্লায়ার বা কোম্পানির নাম খুঁজুন...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = null)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
                    .testTag("supplier_search_input"),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Suppliers List
            if (filteredSuppliers.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.LocalShipping, contentDescription = null, modifier = Modifier.size(56.dp), tint = Color.Gray)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("কোন সাপ্লায়ার পাওয়া যায়নি", color = Color.Gray)
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredSuppliers) { supplier ->
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedSupplierForLedger = supplier }
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(CircleShape)
                                            .background(if (supplier.currentDue > 0) WarningAmberLight else IndigoLight),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.LocalShipping,
                                            contentDescription = null,
                                            tint = if (supplier.currentDue > 0) WarningAmber else NavyPrimary,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = supplier.name,
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                        Text(
                                            text = "${supplier.company} • ${supplier.phone}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "দেনা: ${FormatUtils.formatCurrency(supplier.currentDue, settings.currencySymbol)}",
                                            fontWeight = FontWeight.Bold,
                                            color = if (supplier.currentDue > 0) WarningAmber else SuccessGreen,
                                            fontSize = 15.sp
                                        )
                                        Text(
                                            text = "মোট কেনা: ৳${supplier.totalPurchaseAmount.toInt()}",
                                            fontSize = 11.sp,
                                            color = Color.Gray
                                        )
                                    }
                                }

                                Divider(modifier = Modifier.padding(vertical = 8.dp), color = BorderLight)

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedButton(
                                        onClick = { selectedSupplierForLedger = supplier },
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                        modifier = Modifier.height(36.dp)
                                    ) {
                                        Text("লেনদেন হিস্ট্রি", fontSize = 12.sp)
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Button(
                                        onClick = { supplierForPayment = supplier },
                                        colors = ButtonDefaults.buttonColors(containerColor = WarningAmber),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                        modifier = Modifier.height(36.dp)
                                    ) {
                                        Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("দেনা পরিশোধ", fontSize = 12.sp, color = Color.Black)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add / Edit Supplier Dialog
    if (showAddSupplierDialog) {
        var name by remember { mutableStateOf(supplierToEdit?.name ?: "") }
        var company by remember { mutableStateOf(supplierToEdit?.company ?: "") }
        var phone by remember { mutableStateOf(supplierToEdit?.phone ?: "") }
        var address by remember { mutableStateOf(supplierToEdit?.address ?: "") }
        var initialDue by remember { mutableStateOf(supplierToEdit?.currentDue?.toString() ?: "0") }

        AlertDialog(
            onDismissRequest = { showAddSupplierDialog = false },
            title = { Text(if (supplierToEdit == null) "নতুন সাপ্লায়ার যোগ করুন" else "সাপ্লায়ার তথ্য এডিট") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("সাপ্লায়ারের নাম *") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("supplier_name_input")
                    )
                    OutlinedTextField(
                        value = company,
                        onValueChange = { company = it },
                        label = { Text("কোম্পানি / এজেন্সির নাম") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("মোবাইল নম্বর") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("ঠিকানা") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (supplierToEdit == null) {
                        OutlinedTextField(
                            value = initialDue,
                            onValueChange = { initialDue = it },
                            label = { Text("পূর্বের প্রারম্ভিক দেনা (৳)") },
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
                        if (name.isBlank()) {
                            Toast.makeText(context, "সাপ্লায়ারের নাম দিন", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        val due = initialDue.toDoubleOrNull() ?: 0.0
                        if (supplierToEdit == null) {
                            val newSupplier = Supplier(
                                name = name.trim(),
                                company = company.trim(),
                                phone = phone.trim(),
                                address = address.trim(),
                                totalPurchaseAmount = due,
                                totalPaidAmount = 0.0,
                                currentDue = due
                            )
                            viewModel.addSupplier(newSupplier)
                            Toast.makeText(context, "সাপ্লায়ার যুক্ত হয়েছে", Toast.LENGTH_SHORT).show()
                        } else {
                            viewModel.updateSupplier(
                                supplierToEdit!!.copy(
                                    name = name.trim(),
                                    company = company.trim(),
                                    phone = phone.trim(),
                                    address = address.trim()
                                )
                            )
                            Toast.makeText(context, "তথ্য আপডেট হয়েছে", Toast.LENGTH_SHORT).show()
                        }
                        showAddSupplierDialog = false
                    },
                    modifier = Modifier.testTag("save_supplier_btn")
                ) {
                    Text("সংরক্ষণ করুন")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddSupplierDialog = false }) {
                    Text("বাতিল")
                }
            }
        )
    }

    // Supplier Payment Dialog
    if (supplierForPayment != null) {
        val supp = supplierForPayment!!
        var payAmount by remember { mutableStateOf(supp.currentDue.toString()) }
        var payMethod by remember { mutableStateOf("Cash") }
        var payNote by remember { mutableStateOf("") }

        val paymentMethods = listOf("Cash", "বিকাশ", "নগদ", "ব্যাংক")

        AlertDialog(
            onDismissRequest = { supplierForPayment = null },
            title = { Text("মহাজন / সাপ্লায়ার দেনা পরিশোধ") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("সাপ্লায়ার: ${supp.name} (${supp.company})", fontWeight = FontWeight.Bold)
                    Text("বর্তমান দেনা: ৳${supp.currentDue}", color = WarningAmber, fontWeight = FontWeight.Bold)

                    OutlinedTextField(
                        value = payAmount,
                        onValueChange = { payAmount = it },
                        label = { Text("পরিশোধের পরিমাণ (৳) *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("pay_supplier_amount_input")
                    )

                    Text("পেমেন্ট মাধ্যম:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        paymentMethods.forEach { method ->
                            FilterChip(
                                selected = payMethod == method,
                                onClick = { payMethod = method },
                                label = { Text(method, fontSize = 11.sp) }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = payNote,
                        onValueChange = { payNote = it },
                        label = { Text("নোট / রেফারেন্স") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amount = payAmount.toDoubleOrNull() ?: 0.0
                        if (amount <= 0) {
                            Toast.makeText(context, "সঠিক টাকার পরিমাণ দিন", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        viewModel.paySupplier(
                            supplierId = supp.id,
                            amount = amount,
                            method = payMethod,
                            note = payNote
                        ) {
                            Toast.makeText(context, "দেনা পরিশোধ সম্পন্ন হয়েছে", Toast.LENGTH_SHORT).show()
                            supplierForPayment = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = WarningAmber),
                    modifier = Modifier.testTag("confirm_pay_supplier_btn")
                ) {
                    Text("পরিশোধ নিশ্চিত করুন", color = Color.Black)
                }
            },
            dismissButton = {
                TextButton(onClick = { supplierForPayment = null }) {
                    Text("বাতিল")
                }
            }
        )
    }

    // Supplier Ledger Sheet Dialog
    if (selectedSupplierForLedger != null) {
        val supp = selectedSupplierForLedger!!
        val transactions by viewModel.getTransactionsForSupplier(supp.id).collectAsStateWithLifecycle(emptyList())

        AlertDialog(
            onDismissRequest = { selectedSupplierForLedger = null },
            title = {
                Column {
                    Text("${supp.name} - খাতা বিবরণী", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("কোম্পানি: ${supp.company} | বর্তমান দেনা: ৳${supp.currentDue}", fontSize = 12.sp, color = WarningAmber)
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    if (transactions.isEmpty()) {
                        Text("কোন লেনদেন রেকর্ড নেই।", modifier = Modifier.padding(16.dp), color = Color.Gray)
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 400.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(transactions) { tx ->
                                val isPayment = tx.type == "PAYMENT"
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isPayment) SuccessGreenLight else WarningAmberLight
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = if (isPayment) "টাকা পরিশোধ (${tx.paymentMethod})" else "মালামাল ক্রয় (বাকি)",
                                                fontWeight = FontWeight.Bold,
                                                color = if (isPayment) SuccessGreen else WarningAmber,
                                                fontSize = 13.sp
                                            )
                                            Text(
                                                text = FormatUtils.formatDateTime(tx.timestamp),
                                                fontSize = 11.sp,
                                                color = Color.DarkGray
                                            )
                                            if (tx.note.isNotBlank()) {
                                                Text(text = tx.note, fontSize = 11.sp, color = Color.Black)
                                            }
                                        }

                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(
                                                text = (if (isPayment) "- ৳" else "+ ৳") + tx.amount,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isPayment) SuccessGreen else WarningAmber,
                                                fontSize = 14.sp
                                            )
                                            Text(
                                                text = "অবশিষ্ট দেনা: ৳${tx.remainingDue}",
                                                fontSize = 11.sp,
                                                color = Color.DarkGray
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedSupplierForLedger = null }) {
                    Text("বন্ধ করুন")
                }
            }
        )
    }
}
