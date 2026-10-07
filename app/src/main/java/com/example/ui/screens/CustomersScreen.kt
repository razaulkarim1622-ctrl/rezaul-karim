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
import com.example.data.model.Customer
import com.example.data.model.CustomerTransaction
import com.example.ui.theme.*
import com.example.util.FormatUtils
import com.example.util.InvoicePdfGenerator
import com.example.viewmodel.ShopViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomersScreen(
    viewModel: ShopViewModel
) {
    val context = LocalContext.current
    val customers by viewModel.customers.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    var filterOnlyDue by remember { mutableStateOf(false) }

    var selectedCustomerForLedger by remember { mutableStateOf<Customer?>(null) }
    var showAddCustomerDialog by remember { mutableStateOf(false) }
    var customerToEdit by remember { mutableStateOf<Customer?>(null) }
    var customerForPayment by remember { mutableStateOf<Customer?>(null) }
    var customerForManualDue by remember { mutableStateOf<Customer?>(null) }

    val filteredCustomers = remember(customers, searchQuery, filterOnlyDue) {
        customers.filter { cust ->
            val matchSearch = cust.name.contains(searchQuery, ignoreCase = true) ||
                    cust.phone.contains(searchQuery, ignoreCase = true) ||
                    cust.address.contains(searchQuery, ignoreCase = true)
            val matchDue = if (filterOnlyDue) cust.currentDue > 0 else true
            matchSearch && matchDue
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    customerToEdit = null
                    showAddCustomerDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                modifier = Modifier.testTag("add_customer_fab")
            ) {
                Icon(Icons.Default.PersonAdd, contentDescription = "নতুন খরিদ্দার যোগ")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("customers_screen")
        ) {
            // Top Summary Bar
            val totalCustomerDue = remember(customers) { customers.sumOf { it.currentDue } }
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = DueRedLight)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("মোট বাকি / পাওনা খাতা", fontSize = 12.sp, color = DueRed)
                        Text(
                            text = FormatUtils.formatCurrency(totalCustomerDue, settings.currencySymbol),
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = DueRed
                        )
                    }
                    FilterChip(
                        selected = filterOnlyDue,
                        onClick = { filterOnlyDue = !filterOnlyDue },
                        label = { Text("শুধু বাকিদার (${customers.count { it.currentDue > 0 }})") },
                        leadingIcon = {
                            if (filterOnlyDue) Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    )
                }
            }

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("কাস্টমারের নাম বা মোবাইল নম্বর খুঁজুন...") },
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
                    .testTag("customer_search_input"),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Customer List
            if (filteredCustomers.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.PeopleOutline, contentDescription = null, modifier = Modifier.size(56.dp), tint = Color.Gray)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("কোন কাস্টমার পাওয়া যায়নি", color = Color.Gray)
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredCustomers) { customer ->
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedCustomerForLedger = customer }
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
                                            .background(if (customer.currentDue > 0) DueRedLight else IndigoLight),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = customer.name.take(1),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 18.sp,
                                            color = if (customer.currentDue > 0) DueRed else NavyPrimary
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = customer.name,
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                        Text(
                                            text = if (customer.phone.isNotBlank()) customer.phone else "ফোন নম্বর নেই",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "বাকি: ${FormatUtils.formatCurrency(customer.currentDue, settings.currencySymbol)}",
                                            fontWeight = FontWeight.Bold,
                                            color = if (customer.currentDue > 0) DueRed else SuccessGreen,
                                            fontSize = 15.sp
                                        )
                                        Text(
                                            text = "⭐ লয়্যালটি: ${customer.loyaltyPoints} pt",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = WarningAmber
                                        )
                                        Text(
                                            text = "মোট কেনা: ৳${customer.totalPurchase.toInt()}",
                                            fontSize = 11.sp,
                                            color = Color.Gray
                                        )
                                    }
                                }

                                Divider(modifier = Modifier.padding(vertical = 8.dp), color = BorderLight)

                                // Action Buttons Row: Pay, Add Due, Ledger
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedButton(
                                        onClick = { selectedCustomerForLedger = customer },
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                        modifier = Modifier.height(36.dp)
                                    ) {
                                        Icon(Icons.Default.MenuBook, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("খাতা / হিস্ট্রি", fontSize = 12.sp)
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    FilledTonalButton(
                                        onClick = { customerForManualDue = customer },
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                        modifier = Modifier.height(36.dp)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("বাকি দিন", fontSize = 12.sp)
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Button(
                                        onClick = { customerForPayment = customer },
                                        colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                        modifier = Modifier.height(36.dp)
                                    ) {
                                        Icon(Icons.Default.Paid, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("টাকা নিন", fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add / Edit Customer Dialog
    if (showAddCustomerDialog) {
        var name by remember { mutableStateOf(customerToEdit?.name ?: "") }
        var phone by remember { mutableStateOf(customerToEdit?.phone ?: "") }
        var address by remember { mutableStateOf(customerToEdit?.address ?: "") }
        var note by remember { mutableStateOf(customerToEdit?.note ?: "") }
        var initialDue by remember { mutableStateOf(customerToEdit?.currentDue?.toString() ?: "0") }

        AlertDialog(
            onDismissRequest = { showAddCustomerDialog = false },
            title = { Text(if (customerToEdit == null) "নতুন কাস্টমার যোগ করুন" else "কাস্টমার তথ্য সংশোধন") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("কাস্টমারের নাম *") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("customer_name_input")
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
                    if (customerToEdit == null) {
                        OutlinedTextField(
                            value = initialDue,
                            onValueChange = { initialDue = it },
                            label = { Text("পূর্বের প্রারম্ভিক বাকি (যদি থাকে)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        label = { Text("নোট / বিবরণ") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (name.isBlank()) {
                            Toast.makeText(context, "কাস্টমারের নাম আবশ্যক", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        val dueVal = initialDue.toDoubleOrNull() ?: 0.0
                        if (customerToEdit == null) {
                            val newCustomer = Customer(
                                name = name.trim(),
                                phone = phone.trim(),
                                address = address.trim(),
                                note = note.trim(),
                                totalPurchase = dueVal,
                                totalPaid = 0.0,
                                currentDue = dueVal
                            )
                            viewModel.addCustomer(newCustomer)
                            Toast.makeText(context, "কাস্টমার যোগ করা হয়েছে", Toast.LENGTH_SHORT).show()
                        } else {
                            viewModel.updateCustomer(
                                customerToEdit!!.copy(
                                    name = name.trim(),
                                    phone = phone.trim(),
                                    address = address.trim(),
                                    note = note.trim()
                                )
                            )
                            Toast.makeText(context, "তথ্য আপডেট হয়েছে", Toast.LENGTH_SHORT).show()
                        }
                        showAddCustomerDialog = false
                    },
                    modifier = Modifier.testTag("save_customer_btn")
                ) {
                    Text("সংরক্ষণ করুন")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddCustomerDialog = false }) {
                    Text("বাতিল")
                }
            }
        )
    }

    // Customer Payment Collection Dialog
    if (customerForPayment != null) {
        val cust = customerForPayment!!
        var payAmount by remember { mutableStateOf(cust.currentDue.toString()) }
        var payMethod by remember { mutableStateOf("Cash") }
        var payNote by remember { mutableStateOf("") }

        val paymentMethods = listOf("Cash", "বিকাশ", "নগদ", "রকেট", "ব্যাংক")

        AlertDialog(
            onDismissRequest = { customerForPayment = null },
            title = { Text("বাকি টাকা আদায় / জমা গ্রহণ") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("কাস্টমার: ${cust.name}", fontWeight = FontWeight.Bold)
                    Text(
                        "বর্তমান বাকি: ৳${cust.currentDue}",
                        color = DueRed,
                        fontWeight = FontWeight.SemiBold
                    )

                    OutlinedTextField(
                        value = payAmount,
                        onValueChange = { payAmount = it },
                        label = { Text("আদায়ের পরিমাণ (৳) *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("collect_payment_amount_input")
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
                        label = { Text("নোট (ঐচ্ছিক)") },
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
                            Toast.makeText(context, "সঠিক টাকার পরিমাণ লিখুন", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        viewModel.collectCustomerPayment(
                            customerId = cust.id,
                            amount = amount,
                            method = payMethod,
                            note = payNote
                        ) {
                            Toast.makeText(context, "টাকা জমা সফল হয়েছে", Toast.LENGTH_SHORT).show()
                            customerForPayment = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                    modifier = Modifier.testTag("confirm_collect_payment_btn")
                ) {
                    Text("জমা গ্রহণ করুন")
                }
            },
            dismissButton = {
                TextButton(onClick = { customerForPayment = null }) {
                    Text("বাতিল")
                }
            }
        )
    }

    // Manual Add Due Dialog
    if (customerForManualDue != null) {
        val cust = customerForManualDue!!
        var dueAmount by remember { mutableStateOf("") }
        var dueNote by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { customerForManualDue = null },
            title = { Text("বাকি যোগ করুন") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("কাস্টমার: ${cust.name}", fontWeight = FontWeight.Bold)
                    Text("বর্তমান বাকি: ৳${cust.currentDue}", color = DueRed)

                    OutlinedTextField(
                        value = dueAmount,
                        onValueChange = { dueAmount = it },
                        label = { Text("নতুন বাকি টাকা (৳) *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = dueNote,
                        onValueChange = { dueNote = it },
                        label = { Text("বিবরণ / কারণ *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amount = dueAmount.toDoubleOrNull() ?: 0.0
                        if (amount <= 0) {
                            Toast.makeText(context, "সঠিক টাকার পরিমাণ লিখুন", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        viewModel.addCustomerDueManually(
                            customerId = cust.id,
                            amount = amount,
                            note = dueNote
                        ) {
                            Toast.makeText(context, "বাকি যুক্ত হয়েছে", Toast.LENGTH_SHORT).show()
                            customerForManualDue = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DueRed)
                ) {
                    Text("বাকি যুক্ত করুন")
                }
            },
            dismissButton = {
                TextButton(onClick = { customerForManualDue = null }) {
                    Text("বাতিল")
                }
            }
        )
    }

    // Customer Ledger / History Full Sheet Dialog
    if (selectedCustomerForLedger != null) {
        val cust = selectedCustomerForLedger!!
        val transactions by viewModel.getTransactionsForCustomer(cust.id).collectAsStateWithLifecycle(emptyList())

        AlertDialog(
            onDismissRequest = { selectedCustomerForLedger = null },
            title = {
                Column {
                    Text("${cust.name} - খাতা ও লেনদেন বিবরণী", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("মোবাইল: ${cust.phone} | বর্তমান বাকি: ৳${cust.currentDue}", fontSize = 12.sp, color = DueRed)
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    if (transactions.isEmpty()) {
                        Text("এই কাস্টমারের কোন লেনদেন রেকর্ড নেই।", modifier = Modifier.padding(16.dp), color = Color.Gray)
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
                                        containerColor = if (isPayment) SuccessGreenLight else DueRedLight
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
                                                text = if (isPayment) "টাকা জমা (${tx.paymentMethod})" else "বাকি নেওয়া",
                                                fontWeight = FontWeight.Bold,
                                                color = if (isPayment) SuccessGreen else DueRed,
                                                fontSize = 13.sp
                                            )
                                            Text(
                                                text = FormatUtils.formatDateTime(tx.timestamp),
                                                fontSize = 11.sp,
                                                color = Color.DarkGray
                                            )
                                            if (tx.note.isNotBlank()) {
                                                Text(
                                                    text = tx.note,
                                                    fontSize = 11.sp,
                                                    color = Color.Black
                                                )
                                            }
                                        }

                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(
                                                text = (if (isPayment) "- ৳" else "+ ৳") + tx.amount,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isPayment) SuccessGreen else DueRed,
                                                fontSize = 14.sp
                                            )
                                            Text(
                                                text = "অবশিষ্ট: ৳${tx.remainingDue}",
                                                fontSize = 11.sp,
                                                color = Color.DarkGray
                                            )
                                            // Receipt print option for payment
                                            if (isPayment) {
                                                TextButton(
                                                    onClick = {
                                                        InvoicePdfGenerator.printCustomerPaymentReceipt(
                                                            context,
                                                            tx,
                                                            cust.name,
                                                            settings
                                                        )
                                                    },
                                                    contentPadding = PaddingValues(0.dp),
                                                    modifier = Modifier.height(24.dp)
                                                ) {
                                                    Text("রিসিট প্রিন্ট", fontSize = 10.sp)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedCustomerForLedger = null }) {
                    Text("বন্ধ করুন")
                }
            }
        )
    }
}
