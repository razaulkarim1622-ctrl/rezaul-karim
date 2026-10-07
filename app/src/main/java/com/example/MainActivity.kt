package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.navigation.Screen
import com.example.ui.screens.*
import com.example.ui.theme.DokanKhataTheme
import com.example.ui.theme.IndigoLight
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NavyPrimary
import com.example.viewmodel.ShopViewModel
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    private val viewModel: ShopViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DokanKhataTheme {
                MainAppContainer(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppContainer(viewModel: ShopViewModel) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()

    var languageRefreshTrigger by remember { mutableStateOf(0) }

    LaunchedEffect(settings.language) {
        com.example.util.LanguageManager.currentLanguage =
            if (settings.language == "en") com.example.util.AppLanguage.ENGLISH else com.example.util.AppLanguage.BANGLA
    }

    var showSplash by remember { mutableStateOf(true) }
    var isUnlocked by remember { mutableStateOf(false) }
    var currentScreen by remember { mutableStateOf(Screen.DASHBOARD) }
    var selectedSaleIdForDetail by remember { mutableStateOf<Long?>(null) }

    // Splash Timer
    LaunchedEffect(Unit) {
        delay(1200)
        showSplash = false
        // Mandatory login: password is required to access the app
        isUnlocked = false
    }

    if (showSplash) {
        SplashScreen(shopName = settings.shopName)
        return
    }

    // Login / Password Screen (Mandatory default password "111111")
    val requiredPassword = if (settings.appPin.isNotBlank()) settings.appPin else "111111"
    if (!isUnlocked) {
        PinLockScreen(
            correctPin = requiredPassword,
            shopName = settings.shopName,
            onUnlocked = { isUnlocked = true }
        )
        return
    }

    // Sub-Screen Back Navigation Handling
    BackHandler(enabled = selectedSaleIdForDetail != null || currentScreen != Screen.DASHBOARD) {
        if (selectedSaleIdForDetail != null) {
            selectedSaleIdForDetail = null
        } else if (currentScreen != Screen.DASHBOARD) {
            currentScreen = Screen.DASHBOARD
        }
    }

    Scaffold(
        topBar = {
            if (selectedSaleIdForDetail == null) {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = currentScreen.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                            Text(
                                text = settings.shopName,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    actions = {
                        IconButton(onClick = { currentScreen = Screen.SETTINGS }) {
                            Icon(Icons.Default.Settings, contentDescription = "সেটিংস")
                        }
                    }
                )
            }
        },
        bottomBar = {
            if (selectedSaleIdForDetail == null) {
                NavigationBar(
                    windowInsets = WindowInsets.navigationBars,
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    val navItems = listOf(
                        Screen.DASHBOARD,
                        Screen.POS_SALES,
                        Screen.PRODUCTS,
                        Screen.CUSTOMERS,
                        Screen.REPORTS
                    )

                    navItems.forEach { screen ->
                        NavigationBarItem(
                            selected = currentScreen == screen,
                            onClick = {
                                selectedSaleIdForDetail = null
                                currentScreen = screen
                            },
                            icon = { Icon(screen.icon, contentDescription = screen.title) },
                            label = { Text(screen.title, fontSize = 11.sp, maxLines = 1) },
                            modifier = Modifier.testTag("nav_item_${screen.name.lowercase()}")
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (selectedSaleIdForDetail != null) {
                SaleDetailScreen(
                    saleId = selectedSaleIdForDetail!!,
                    viewModel = viewModel,
                    onBack = { selectedSaleIdForDetail = null }
                )
            } else {
                when (currentScreen) {
                    Screen.DASHBOARD -> DashboardScreen(
                        viewModel = viewModel,
                        onNavigate = { currentScreen = it },
                        onViewSaleDetails = { selectedSaleIdForDetail = it }
                    )
                    Screen.POS_SALES -> PosSaleScreen(
                        viewModel = viewModel,
                        onSaleCompleted = { sale, _ ->
                            selectedSaleIdForDetail = sale.id
                        }
                    )
                    Screen.PRODUCTS -> ProductsScreen(viewModel = viewModel)
                    Screen.CUSTOMERS -> CustomersScreen(viewModel = viewModel)
                    Screen.SUPPLIERS -> SuppliersScreen(viewModel = viewModel)
                    Screen.PURCHASES -> PurchasesScreen(viewModel = viewModel)
                    Screen.EXPENSES -> ExpensesScreen(viewModel = viewModel)
                    Screen.CASHBOOK -> CashbookScreen(viewModel = viewModel)
                    Screen.REPORTS -> ReportsScreen(viewModel = viewModel)
                    Screen.SETTINGS -> SettingsScreen(
                        viewModel = viewModel,
                        onLanguageChanged = { languageRefreshTrigger++ }
                    )
                }
            }
        }
    }
}

@Composable
fun SplashScreen(shopName: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NavyPrimary),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.app_logo),
                    contentDescription = "দোকান খাতা",
                    modifier = Modifier.size(72.dp)
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = shopName.ifBlank { "দোকান খাতা" },
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "আধুনিক দোকান ম্যানেজমেন্ট ও হিসাব খাতা",
                fontSize = 13.sp,
                color = Color.White.copy(alpha = 0.85f)
            )
        }
    }
}

@Composable
fun PinLockScreen(
    correctPin: String,
    shopName: String,
    onUnlocked: () -> Unit
) {
    var enteredPin by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NavyDark)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(IndigoLight),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Lock,
                        contentDescription = "Login Lock",
                        tint = NavyPrimary,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = shopName.ifBlank { "দোকান খাতা" },
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "অ্যাপে প্রবেশ করতে পাসওয়ার্ড দিন",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(20.dp))

                OutlinedTextField(
                    value = enteredPin,
                    onValueChange = {
                        if (it.length <= 10) {
                            enteredPin = it
                            errorMsg = ""
                            if (it == correctPin) {
                                onUnlocked()
                            }
                        }
                    },
                    placeholder = { Text("পাসওয়ার্ড লিখুন...") },
                    label = { Text("পাসওয়ার্ড (Password)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("login_password_input")
                )

                if (errorMsg.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMsg,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        if (enteredPin == correctPin) {
                            onUnlocked()
                        } else {
                            errorMsg = "ভুল পাসওয়ার্ড! সঠিক পাসওয়ার্ড দিন।"
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("login_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Login, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("লগইন করুন (Login)", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "ডিফল্ট পাসওয়ার্ড: 111111",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }
    }
}
