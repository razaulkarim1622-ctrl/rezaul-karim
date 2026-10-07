package com.example.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Print
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
import com.example.data.model.SaleItem
import com.example.ui.theme.*
import com.example.util.FormatUtils
import com.example.util.InvoicePdfGenerator
import com.example.viewmodel.ShopViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SaleDetailScreen(
    saleId: Long,
    viewModel: ShopViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val sales by viewModel.sales.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()

    val sale = remember(sales, saleId) { sales.find { it.id == saleId } }
    var saleItems by remember { mutableStateOf<List<SaleItem>>(emptyList()) }

    LaunchedEffect(saleId) {
        saleItems = viewModel.getSaleItems(saleId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(sale?.invoiceNumber ?: "বিক্রয় মেমো") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "পিছনে")
                    }
                },
                actions = {
                    if (sale != null) {
                        IconButton(onClick = {
                            InvoicePdfGenerator.printOrShareSaleInvoice(context, sale, saleItems, settings)
                        }) {
                            Icon(Icons.Default.Print, contentDescription = "প্রিন্ট")
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        if (sale == null) {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .testTag("sale_detail_screen"),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header card
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "ক্রেতা: ${sale.customerName}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                Text(
                                    text = if (sale.dueAmount > 0) "বাকি মেমো" else "পরিশোধিত",
                                    color = if (sale.dueAmount > 0) DueRed else SuccessGreen,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "তারিখ: ${FormatUtils.formatDateTime(sale.timestamp)}",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                            Text(
                                text = "পেমেন্ট মাধ্যম: ${sale.paymentMethod}",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }

                item {
                    Text(
                        text = "ক্রয়কৃত পণ্যসমূহ (${saleItems.size} টি)",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                items(saleItems) { item ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(item.productName, fontWeight = FontWeight.SemiBold)
                                Text(
                                    text = "${item.quantity} ${item.unit} x ৳${item.unitPrice}",
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                            }
                            Text(
                                text = FormatUtils.formatCurrency(item.totalPrice, settings.currencySymbol),
                                fontWeight = FontWeight.Bold,
                                color = NavyPrimary
                            )
                        }
                    }
                }

                // Financial Breakdown
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = IndigoLight),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("সাবটোটাল:")
                                Text(FormatUtils.formatCurrency(sale.subtotal, settings.currencySymbol))
                            }
                            if (sale.discount > 0) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("ডিসকাউন্ট / ছাড়:", color = DueRed)
                                    Text("- " + FormatUtils.formatCurrency(sale.discount, settings.currencySymbol), color = DueRed)
                                }
                            }
                            Divider(color = Color.LightGray)
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("সর্বমোট বিল:", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text(FormatUtils.formatCurrency(sale.grandTotal, settings.currencySymbol), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = NavyPrimary)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("আদায়কৃত টাকা:", color = SuccessGreen, fontWeight = FontWeight.SemiBold)
                                Text(FormatUtils.formatCurrency(sale.paidAmount, settings.currencySymbol), color = SuccessGreen, fontWeight = FontWeight.SemiBold)
                            }
                            if (sale.dueAmount > 0) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("বাকি রয়েছে:", color = DueRed, fontWeight = FontWeight.Bold)
                                    Text(FormatUtils.formatCurrency(sale.dueAmount, settings.currencySymbol), color = DueRed, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                item {
                    Button(
                        onClick = {
                            InvoicePdfGenerator.printOrShareSaleInvoice(context, sale, saleItems, settings)
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Print, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("ইনভয়েস প্রিন্ট / PDF শেয়ার")
                    }
                }
            }
        }
    }
}
