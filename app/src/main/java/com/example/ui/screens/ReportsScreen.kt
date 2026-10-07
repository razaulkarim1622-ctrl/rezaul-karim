package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Sale
import com.example.ui.components.StatCard
import com.example.ui.theme.*
import com.example.util.FormatUtils
import com.example.util.InvoicePdfGenerator
import com.example.viewmodel.ShopViewModel
import kotlinx.coroutines.launch
import java.util.*

@Composable
fun ReportsScreen(
    viewModel: ShopViewModel
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val sales by viewModel.sales.collectAsStateWithLifecycle()
    val expenses by viewModel.expenses.collectAsStateWithLifecycle()
    val customers by viewModel.customers.collectAsStateWithLifecycle()
    val suppliers by viewModel.suppliers.collectAsStateWithLifecycle()
    val products by viewModel.products.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()

    var selectedFilter by remember { mutableStateOf("আজকে") }
    val filterOptions = listOf("আজকে", "গত ৭ দিন", "এই মাস", "সব সময়")

    val currentTime = System.currentTimeMillis()
    val filterStartTime = remember(selectedFilter) {
        val cal = Calendar.getInstance()
        when (selectedFilter) {
            "আজকে" -> {
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.timeInMillis
            }
            "গত ৭ দিন" -> currentTime - 7 * 86400000L
            "এই মাস" -> {
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.timeInMillis
            }
            else -> 0L
        }
    }

    val filteredSales = remember(sales, filterStartTime) {
        sales.filter { it.timestamp >= filterStartTime }
    }
    val filteredExpenses = remember(expenses, filterStartTime) {
        expenses.filter { it.timestamp >= filterStartTime }
    }

    val totalSalesAmt = remember(filteredSales) { filteredSales.sumOf { it.grandTotal } }
    val totalPaidAmt = remember(filteredSales) { filteredSales.sumOf { it.paidAmount } }
    val totalDueAmt = remember(filteredSales) { filteredSales.sumOf { it.dueAmount } }
    val totalExpenseAmt = remember(filteredExpenses) { filteredExpenses.sumOf { it.amount } }

    val estGrossProfit = remember(filteredSales) {
        // Approximate 15% margin or accurate from sale
        filteredSales.sumOf { it.grandTotal * 0.15 }
    }
    val netProfit = estGrossProfit - totalExpenseAmt

    var selectedSaleToPrint by remember { mutableStateOf<Sale?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("reports_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Date Range Filters
        item {
            ScrollableTabRow(
                selectedTabIndex = filterOptions.indexOf(selectedFilter).coerceAtLeast(0),
                edgePadding = 0.dp,
                divider = {}
            ) {
                filterOptions.forEach { opt ->
                    FilterChip(
                        selected = selectedFilter == opt,
                        onClick = { selectedFilter = opt },
                        label = { Text(opt) },
                        modifier = Modifier.padding(end = 6.dp)
                    )
                }
            }
        }

        // Summary Card Grid
        item {
            Text(
                text = "$selectedFilter এর আর্থিক রিপোর্ট",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard(
                    title = "মোট বিক্রি",
                    value = FormatUtils.formatCurrency(totalSalesAmt, settings.currencySymbol),
                    subtitle = "${filteredSales.size} টি বিক্রয় মেমো",
                    icon = Icons.Default.TrendingUp,
                    iconBgColor = IndigoLight,
                    iconColor = NavyPrimary,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "নগদ আদায়",
                    value = FormatUtils.formatCurrency(totalPaidAmt, settings.currencySymbol),
                    subtitle = "ক্যাশে জমা",
                    icon = Icons.Default.CheckCircle,
                    iconBgColor = SuccessGreenLight,
                    iconColor = SuccessGreen,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard(
                    title = "বাকি বিক্রি",
                    value = FormatUtils.formatCurrency(totalDueAmt, settings.currencySymbol),
                    subtitle = "বাকি মেমো",
                    icon = Icons.Default.AssignmentLate,
                    iconBgColor = DueRedLight,
                    iconColor = DueRed,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "মোট খরচ",
                    value = FormatUtils.formatCurrency(totalExpenseAmt, settings.currencySymbol),
                    subtitle = "${filteredExpenses.size} টি খরচ খাত",
                    icon = Icons.Default.ReceiptLong,
                    iconBgColor = WarningAmberLight,
                    iconColor = WarningAmber,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Net Profit Calculation Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (netProfit >= 0) SuccessGreenLight else DueRedLight
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "আনুমানিক নিট লাভ (Net Profit)",
                        fontWeight = FontWeight.Bold,
                        color = if (netProfit >= 0) SuccessGreen else DueRed,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = FormatUtils.formatCurrency(netProfit, settings.currencySymbol),
                        fontWeight = FontWeight.Bold,
                        fontSize = 26.sp,
                        color = if (netProfit >= 0) SuccessGreen else DueRed
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "(মোট লাভ: ${FormatUtils.formatCurrency(estGrossProfit, settings.currencySymbol)} - খরচ: ${FormatUtils.formatCurrency(totalExpenseAmt, settings.currencySymbol)})",
                        fontSize = 12.sp,
                        color = Color.DarkGray
                    )
                }
            }
        }

        // Overall Store Asset Stats
        item {
            Text(
                text = "দোকানের বর্তমান মোট স্থিতি",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    val totalCustDue = customers.sumOf { it.currentDue }
                    val totalSuppDue = suppliers.sumOf { it.currentDue }
                    val totalStockVal = products.sumOf { it.stockQuantity * it.purchasePrice }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("মোট কাস্টমার পাওনা (বাকি):")
                        Text(FormatUtils.formatCurrency(totalCustDue, settings.currencySymbol), fontWeight = FontWeight.Bold, color = DueRed)
                    }
                    Divider(color = BorderLight)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("মোট মহাজন দেনা (সাপ্লায়ার):")
                        Text(FormatUtils.formatCurrency(totalSuppDue, settings.currencySymbol), fontWeight = FontWeight.Bold, color = WarningAmber)
                    }
                    Divider(color = BorderLight)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("দোকানের মোট স্টক মালামালের মূল্য:")
                        Text(FormatUtils.formatCurrency(totalStockVal, settings.currencySymbol), fontWeight = FontWeight.Bold, color = NavyPrimary)
                    }
                }
            }
        }

        // Filtered Sales List
        item {
            Text(
                text = "বিক্রয় মেমো তালিকা (${filteredSales.size} টি)",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        if (filteredSales.isEmpty()) {
            item {
                Text("নির্বাচিত সময়ে কোন বিক্রি পাওয়া যায়নি।", color = Color.Gray, modifier = Modifier.padding(vertical = 8.dp))
            }
        } else {
            items(filteredSales) { sale ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(sale.customerName, fontWeight = FontWeight.Bold)
                            Text(
                                text = "${sale.invoiceNumber} • ${FormatUtils.formatDateTime(sale.timestamp)}",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = FormatUtils.formatCurrency(sale.grandTotal, settings.currencySymbol),
                                fontWeight = FontWeight.Bold,
                                color = NavyPrimary
                            )
                            if (sale.dueAmount > 0) {
                                Text(
                                    text = "বাকি: ৳${sale.dueAmount.toInt()}",
                                    color = DueRed,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        IconButton(onClick = {
                            coroutineScope.launch {
                                val items = viewModel.getSaleItems(sale.id)
                                InvoicePdfGenerator.printOrShareSaleInvoice(context, sale, items, settings)
                            }
                        }) {
                            Icon(Icons.Default.Print, contentDescription = "ইনভয়েস প্রিন্ট", tint = NavyPrimary)
                        }
                    }
                }
            }
        }
    }
}
