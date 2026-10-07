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
import com.example.data.model.Expense
import com.example.ui.theme.*
import com.example.util.FormatUtils
import com.example.viewmodel.ShopViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpensesScreen(
    viewModel: ShopViewModel
) {
    val context = LocalContext.current
    val expenses by viewModel.expenses.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()

    var showAddExpenseDialog by remember { mutableStateOf(false) }
    var expenseToDelete by remember { mutableStateOf<Expense?>(null) }

    val totalExpense = remember(expenses) { expenses.sumOf { it.amount } }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddExpenseDialog = true },
                containerColor = DueRed,
                contentColor = Color.White,
                modifier = Modifier.testTag("add_expense_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "নতুন খরচ যোগ")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("expenses_screen")
        ) {
            // Header summary
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
                        Text("দোকানের মোট খরচ হিসাব", fontSize = 12.sp, color = DueRed)
                        Text(
                            text = FormatUtils.formatCurrency(totalExpense, settings.currencySymbol),
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = DueRed
                        )
                    }
                    Text("মোট এন্ট্রি: ${expenses.size} টি", fontSize = 12.sp, color = TextPrimaryLight)
                }
            }

            if (expenses.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(56.dp), tint = Color.Gray)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("কোন খরচ রেকর্ড করা নেই।", color = Color.Gray)
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(expenses) { expense ->
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
                                        text = expense.category,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                    if (expense.description.isNotBlank()) {
                                        Text(
                                            text = expense.description,
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Text(
                                        text = "${FormatUtils.formatDateTime(expense.timestamp)} • মাধ্যম: ${expense.paymentMethod}",
                                        fontSize = 11.sp,
                                        color = Color.Gray
                                    )
                                }

                                Text(
                                    text = FormatUtils.formatCurrency(expense.amount, settings.currencySymbol),
                                    fontWeight = FontWeight.Bold,
                                    color = DueRed,
                                    fontSize = 15.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp)
                                )

                                IconButton(onClick = { expenseToDelete = expense }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete Expense", tint = Color.Gray)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Expense Dialog
    if (showAddExpenseDialog) {
        val categories = listOf(
            "দোকান ভাড়া", "বিদ্যুৎ বিল", "কর্মচারীর বেতন", "চা-নাস্তা ও আপ্যায়ন",
            "মালামাল পরিবহন", "মোবাইল/ইন্টারনেট", "মেরামত ও পরিষ্কার", "অন্যান্য খরচ"
        )
        var selectedCategory by remember { mutableStateOf(categories[0]) }
        var amountInput by remember { mutableStateOf("") }
        var descInput by remember { mutableStateOf("") }
        var methodInput by remember { mutableStateOf("Cash") }

        AlertDialog(
            onDismissRequest = { showAddExpenseDialog = false },
            title = { Text("নতুন খরচ যুক্ত করুন") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("খরচের খাত / ক্যাটাগরি:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    ScrollableTabRow(
                        selectedTabIndex = categories.indexOf(selectedCategory).coerceAtLeast(0),
                        edgePadding = 0.dp,
                        divider = {}
                    ) {
                        categories.forEach { cat ->
                            FilterChip(
                                selected = selectedCategory == cat,
                                onClick = { selectedCategory = cat },
                                label = { Text(cat, fontSize = 11.sp) }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = amountInput,
                        onValueChange = { amountInput = it },
                        label = { Text("খরচের পরিমাণ (৳) *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("expense_amount_input")
                    )

                    OutlinedTextField(
                        value = descInput,
                        onValueChange = { descInput = it },
                        label = { Text("খরচের বিবরণ / নোট") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf("Cash", "বিকাশ", "নগদ", "ব্যাংক").forEach { m ->
                            FilterChip(
                                selected = methodInput == m,
                                onClick = { methodInput = m },
                                label = { Text(m, fontSize = 11.sp) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amt = amountInput.toDoubleOrNull() ?: 0.0
                        if (amt <= 0) {
                            Toast.makeText(context, "সঠিক খরচের পরিমাণ দিন", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        viewModel.addExpense(
                            category = selectedCategory,
                            amount = amt,
                            desc = descInput.trim(),
                            method = methodInput
                        )
                        Toast.makeText(context, "খরচ যুক্ত হয়েছে এবং ক্যাশবইতে আপডেট হয়েছে", Toast.LENGTH_SHORT).show()
                        showAddExpenseDialog = false
                    },
                    modifier = Modifier.testTag("save_expense_btn")
                ) {
                    Text("সংরক্ষণ করুন")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddExpenseDialog = false }) {
                    Text("বাতিল")
                }
            }
        )
    }

    if (expenseToDelete != null) {
        AlertDialog(
            onDismissRequest = { expenseToDelete = null },
            title = { Text("খরচ মুছে ফেলতে চান?") },
            text = { Text("'${expenseToDelete?.category}' এর ৳${expenseToDelete?.amount} মুছে ফেলতে চান?") },
            confirmButton = {
                Button(
                    onClick = {
                        expenseToDelete?.let { viewModel.deleteExpense(it) }
                        expenseToDelete = null
                        Toast.makeText(context, "খরচ মুছে ফেলা হয়েছে", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DueRed)
                ) {
                    Text("মুছে ফেলুন")
                }
            },
            dismissButton = {
                TextButton(onClick = { expenseToDelete = null }) {
                    Text("বাতিল")
                }
            }
        )
    }
}
