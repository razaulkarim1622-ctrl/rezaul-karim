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
import com.example.ui.theme.*
import com.example.util.FormatUtils
import com.example.viewmodel.ShopViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CashbookScreen(
    viewModel: ShopViewModel
) {
    val context = LocalContext.current
    val entries by viewModel.cashbookEntries.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()

    var showAddEntryDialog by remember { mutableStateOf(false) }

    val totalIn = remember(entries) { entries.filter { it.type == "IN" }.sumOf { it.amount } }
    val totalOut = remember(entries) { entries.filter { it.type == "OUT" }.sumOf { it.amount } }
    val netCashBalance = totalIn - totalOut

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddEntryDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                modifier = Modifier.testTag("add_cashbook_entry_fab")
            ) {
                Icon(Icons.Default.AddCard, contentDescription = "ক্যাশবই এন্ট্রি")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("cashbook_screen")
        ) {
            // Net Balance Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "দোকানের বর্তমান ক্যাশ ব্যালেন্স",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = FormatUtils.formatCurrency(netCashBalance, settings.currencySymbol),
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (netCashBalance >= 0) SuccessGreen else DueRed
                    )

                    Divider(modifier = Modifier.padding(vertical = 12.dp), color = BorderLight)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("মোট জমা (+)", fontSize = 12.sp, color = SuccessGreen, fontWeight = FontWeight.SemiBold)
                            Text(FormatUtils.formatCurrency(totalIn, settings.currencySymbol), fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text("মোট খরচ / প্রদান (-)", fontSize = 12.sp, color = DueRed, fontWeight = FontWeight.SemiBold)
                            Text(FormatUtils.formatCurrency(totalOut, settings.currencySymbol), fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }
                }
            }

            // Entries List
            if (entries.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("ক্যাশবইতে কোন হিসাব পাওয়া যায়নি", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(entries) { entry ->
                        val isDeposit = entry.type == "IN"

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
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(if (isDeposit) SuccessGreenLight else DueRedLight),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (isDeposit) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                                        contentDescription = null,
                                        tint = if (isDeposit) SuccessGreen else DueRed,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = entry.category,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    if (entry.description.isNotBlank()) {
                                        Text(
                                            text = entry.description,
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Text(
                                        text = "${FormatUtils.formatDateTime(entry.timestamp)} • ${entry.paymentMethod}",
                                        fontSize = 11.sp,
                                        color = Color.Gray
                                    )
                                }

                                Text(
                                    text = (if (isDeposit) "+ " else "- ") + FormatUtils.formatCurrency(entry.amount, settings.currencySymbol),
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDeposit) SuccessGreen else DueRed,
                                    fontSize = 15.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Cash Entry Dialog (Deposit or Withdraw)
    if (showAddEntryDialog) {
        var entryType by remember { mutableStateOf("IN") }
        var category by remember { mutableStateOf("") }
        var amountInput by remember { mutableStateOf("") }
        var descInput by remember { mutableStateOf("") }
        var methodInput by remember { mutableStateOf("Cash") }

        AlertDialog(
            onDismissRequest = { showAddEntryDialog = false },
            title = { Text("নতুন ক্যাশ এন্ট্রি") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = entryType == "IN",
                            onClick = { entryType = "IN" },
                            label = { Text("ক্যাশ জমা (IN)", color = if (entryType == "IN") SuccessGreen else Color.Black) },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = entryType == "OUT",
                            onClick = { entryType = "OUT" },
                            label = { Text("ক্যাশ প্রদান (OUT)", color = if (entryType == "OUT") DueRed else Color.Black) },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text("খাত / বিষয় (যেমন: প্রারম্ভিক জমা, ব্যাংক থেকে উত্তোলন) *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = amountInput,
                        onValueChange = { amountInput = it },
                        label = { Text("টাকার পরিমাণ (৳) *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = descInput,
                        onValueChange = { descInput = it },
                        label = { Text("বিবরণ / নোট") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amt = amountInput.toDoubleOrNull() ?: 0.0
                        if (category.isBlank() || amt <= 0) {
                            Toast.makeText(context, "খাত এবং সঠিক টাকার পরিমাণ লিখুন", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        viewModel.addCashbookEntry(
                            type = entryType,
                            category = category.trim(),
                            amount = amt,
                            method = methodInput,
                            desc = descInput.trim()
                        )
                        Toast.makeText(context, "ক্যাশবইতে এন্ট্রি সম্পন্ন হয়েছে", Toast.LENGTH_SHORT).show()
                        showAddEntryDialog = false
                    }
                ) {
                    Text("সংরক্ষণ করুন")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddEntryDialog = false }) {
                    Text("বাতিল")
                }
            }
        )
    }
}
