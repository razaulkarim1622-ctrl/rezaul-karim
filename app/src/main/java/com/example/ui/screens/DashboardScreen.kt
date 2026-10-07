package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.SectionHeader
import com.example.ui.components.StatCard
import com.example.ui.navigation.Screen
import com.example.ui.theme.*
import com.example.util.FormatUtils
import com.example.viewmodel.ShopViewModel

@Composable
fun DashboardScreen(
    viewModel: ShopViewModel,
    onNavigate: (Screen) -> Unit,
    onViewSaleDetails: (Long) -> Unit
) {
    val summary by viewModel.dashboardSummary.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val recentSales by viewModel.sales.collectAsStateWithLifecycle()
    val recentTx by viewModel.recentCustomerTransactions.collectAsStateWithLifecycle()
    val lowStockList by viewModel.lowStockProducts.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("dashboard_screen"),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        // Top Store Banner
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = settings.shopName,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                            Text(
                                text = "${settings.ownerName} • ${settings.phone}",
                                style = MaterialTheme.typography.bodySmall.copy(color = Color.White.copy(alpha = 0.85f))
                            )
                        }
                        IconButton(
                            onClick = { onNavigate(Screen.SETTINGS) },
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f))
                        ) {
                            Icon(Icons.Default.Storefront, contentDescription = "Shop Profile", tint = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Quick POS Launch Button
                    Button(
                        onClick = { onNavigate(Screen.POS_SALES) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("dashboard_quick_pos_btn")
                    ) {
                        Icon(Icons.Default.PointOfSale, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("নতুন বিক্রি শুরু করুন (POS)", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Low stock alert banner (if any)
        if (lowStockList.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .clickable { onNavigate(Screen.PRODUCTS) },
                    colors = CardDefaults.cardColors(containerColor = DueRedLight),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = DueRed)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "স্টক সতর্কবার্তা!",
                                fontWeight = FontWeight.Bold,
                                color = DueRed,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "${lowStockList.size} টি পণ্যের স্টক কমে গেছে। দ্রুত মালামাল কিনুন।",
                                fontSize = 12.sp,
                                color = TextPrimaryLight
                            )
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = DueRed)
                    }
                }
            }
        }

        // Main Financial Summary Grid
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Text(
                    text = "আজকের হিসাব নিকাশ",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatCard(
                        title = "আজকের বিক্রি",
                        value = FormatUtils.formatCurrency(summary.todaySales, settings.currencySymbol),
                        icon = Icons.Default.TrendingUp,
                        iconBgColor = IndigoLight,
                        iconColor = NavyPrimary,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate(Screen.REPORTS) }
                    )
                    StatCard(
                        title = "আজকের আদায়",
                        value = FormatUtils.formatCurrency(summary.todayCollection, settings.currencySymbol),
                        icon = Icons.Default.Paid,
                        iconBgColor = SuccessGreenLight,
                        iconColor = SuccessGreen,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate(Screen.CASHBOOK) }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatCard(
                        title = "আজকের খরচ",
                        value = FormatUtils.formatCurrency(summary.todayExpense, settings.currencySymbol),
                        icon = Icons.Default.ReceiptLong,
                        iconBgColor = WarningAmberLight,
                        iconColor = WarningAmber,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate(Screen.EXPENSES) }
                    )
                    StatCard(
                        title = "আজকের নিট লাভ",
                        value = FormatUtils.formatCurrency(summary.todayNetProfit, settings.currencySymbol),
                        icon = Icons.Default.Savings,
                        iconBgColor = if (summary.todayNetProfit >= 0) SuccessGreenLight else DueRedLight,
                        iconColor = if (summary.todayNetProfit >= 0) SuccessGreen else DueRed,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate(Screen.REPORTS) }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Customer Due vs Supplier Due
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatCard(
                        title = "মোট কাস্টমার বাকি",
                        value = FormatUtils.formatCurrency(summary.totalCustomerDue, settings.currencySymbol),
                        subtitle = "পাওনা টাকা",
                        icon = Icons.Default.AssignmentLate,
                        iconBgColor = DueRedLight,
                        iconColor = DueRed,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate(Screen.CUSTOMERS) }
                    )
                    StatCard(
                        title = "মোট সাপ্লায়ার দেনা",
                        value = FormatUtils.formatCurrency(summary.totalSupplierDue, settings.currencySymbol),
                        subtitle = "দেনা টাকা",
                        icon = Icons.Default.LocalShipping,
                        iconBgColor = WarningAmberLight,
                        iconColor = WarningAmber,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate(Screen.SUPPLIERS) }
                    )
                }
            }
        }

        // Quick Navigation Tiles
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    QuickActionTile(Icons.Default.People, "কাস্টমার", "${summary.totalCustomersCount} জন") {
                        onNavigate(Screen.CUSTOMERS)
                    }
                    QuickActionTile(Icons.Default.Inventory2, "পণ্য তালিকা", "${summary.totalProductsCount} টি") {
                        onNavigate(Screen.PRODUCTS)
                    }
                    QuickActionTile(Icons.Default.LocalShipping, "সাপ্লায়ার", "${summary.totalSuppliersCount} জন") {
                        onNavigate(Screen.SUPPLIERS)
                    }
                    QuickActionTile(Icons.Default.AccountBalanceWallet, "ক্যাশবই", "ব্যালেন্স") {
                        onNavigate(Screen.CASHBOOK)
                    }
                }
            }
        }

        // Recent Sales Section
        item {
            SectionHeader(
                title = "সাম্প্রতিক বিক্রয়সমূহ",
                actionText = "সবগুলো দেখুন",
                onActionClick = { onNavigate(Screen.REPORTS) }
            )
        }

        if (recentSales.isEmpty()) {
            item {
                Text(
                    text = "এখনো কোন বিক্রি রেকর্ড করা হয়নি।",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        } else {
            items(recentSales.take(5)) { sale ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .clickable { onViewSaleDetails(sale.id) },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
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
                                .background(if (sale.dueAmount > 0) DueRedLight else SuccessGreenLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (sale.dueAmount > 0) Icons.Default.Receipt else Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = if (sale.dueAmount > 0) DueRed else SuccessGreen,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = sale.customerName,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "${sale.invoiceNumber} • ${FormatUtils.formatDateTime(sale.timestamp)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = FormatUtils.formatCurrency(sale.grandTotal, settings.currencySymbol),
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            if (sale.dueAmount > 0) {
                                Text(
                                    text = "বাকি: ${FormatUtils.formatCurrency(sale.dueAmount, settings.currencySymbol)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = DueRed,
                                    fontWeight = FontWeight.SemiBold
                                )
                            } else {
                                Text(
                                    text = "পরিশোধিত",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = SuccessGreen
                                )
                            }
                        }
                    }
                }
            }
        }

        // Recent Customer Transactions
        item {
            Spacer(modifier = Modifier.height(12.dp))
            SectionHeader(
                title = "সাম্প্রতিক বাকি লেনদেন / জমা",
                actionText = "খাতা দেখুন",
                onActionClick = { onNavigate(Screen.CUSTOMERS) }
            )
        }

        if (recentTx.isEmpty()) {
            item {
                Text(
                    text = "কোন লেনদেন ইতিহাস পাওয়া যায়নি।",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        } else {
            items(recentTx.take(4)) { tx ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val isPayment = tx.type == "PAYMENT"
                        Icon(
                            imageVector = if (isPayment) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                            contentDescription = null,
                            tint = if (isPayment) SuccessGreen else DueRed,
                            modifier = Modifier.size(24.dp)
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = tx.customerName,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "${if (isPayment) "টাকা জমা" else "বাকি নেওয়া"} • ${FormatUtils.formatDateTime(tx.timestamp)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = (if (isPayment) "- " else "+ ") + FormatUtils.formatCurrency(tx.amount, settings.currencySymbol),
                                fontWeight = FontWeight.Bold,
                                color = if (isPayment) SuccessGreen else DueRed,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "অবশিষ্ট: ${FormatUtils.formatCurrency(tx.remainingDue, settings.currencySymbol)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickActionTile(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    badge: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick() }
            .padding(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(IndigoLight),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = label, tint = NavyPrimary, modifier = Modifier.size(24.dp))
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(text = label, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
        Text(text = badge, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
