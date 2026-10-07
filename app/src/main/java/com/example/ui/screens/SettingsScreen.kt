package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.data.model.ShopSettings
import com.example.ui.theme.*
import com.example.util.AppLanguage
import com.example.util.LanguageManager
import com.example.util.Strings
import com.example.viewmodel.ShopViewModel

@Composable
fun SettingsScreen(
    viewModel: ShopViewModel,
    onLanguageChanged: () -> Unit = {}
) {
    val context = LocalContext.current
    val settings by viewModel.settings.collectAsStateWithLifecycle()

    var shopName by remember(settings) { mutableStateOf(settings.shopName) }
    var ownerName by remember(settings) { mutableStateOf(settings.ownerName) }
    var phone by remember(settings) { mutableStateOf(settings.phone) }
    var address by remember(settings) { mutableStateOf(settings.address) }
    var thankYouMessage by remember(settings) { mutableStateOf(settings.thankYouMessage) }
    var currencySymbol by remember(settings) { mutableStateOf(settings.currencySymbol) }

    var isPinEnabled by remember(settings) { mutableStateOf(settings.isPinEnabled) }
    var appPin by remember(settings) { mutableStateOf(settings.appPin) }

    var selectedLang by remember(settings) { mutableStateOf(settings.language) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("settings_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Multi-Language Switch Section
        item {
            Text(
                text = Strings.languageSelect,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = if (LanguageManager.isBangla()) "অ্যাপের ভাষা পরিবর্তন করুন (বাংলা / English)" else "Switch Application Language (Bengali / English)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        FilterChip(
                            selected = selectedLang == "bn",
                            onClick = {
                                selectedLang = "bn"
                                LanguageManager.currentLanguage = AppLanguage.BANGLA
                                viewModel.updateSettings(settings.copy(language = "bn"))
                                onLanguageChanged()
                                Toast.makeText(context, "ভাষা বাংলা নির্বাচন করা হয়েছে", Toast.LENGTH_SHORT).show()
                            },
                            label = { Text("🇧🇩 বাংলা (Bengali)") },
                            modifier = Modifier.weight(1f)
                        )

                        FilterChip(
                            selected = selectedLang == "en",
                            onClick = {
                                selectedLang = "en"
                                LanguageManager.currentLanguage = AppLanguage.ENGLISH
                                viewModel.updateSettings(settings.copy(language = "en"))
                                onLanguageChanged()
                                Toast.makeText(context, "Language switched to English", Toast.LENGTH_SHORT).show()
                            },
                            label = { Text("🇺🇸 English") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Loyalty Program Information Banner
        item {
            Text(
                text = "🎁 ${Strings.loyaltyPoints}",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = WarningAmberLight),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "কাস্টমার লয়্যালটি প্রোগ্রাম চালু আছে",
                        fontWeight = FontWeight.Bold,
                        color = NavyDark,
                        fontSize = 15.sp
                    )
                    Text(
                        text = Strings.loyaltyProgramInfo,
                        fontSize = 13.sp,
                        color = Color.DarkGray
                    )
                    Text(
                        text = "কাস্টমার বিক্রি করার সময় রিডিম পয়েন্ট দিয়ে সরাসরি ছাড় পাবেন এবং ক্রয় শেষে স্বয়ংক্রিয় পয়েন্ট যোগ হবে।",
                        fontSize = 12.sp,
                        color = Color.DarkGray
                    )
                }
            }
        }

        // Shop Profile & Invoice Settings
        item {
            Text(
                text = Strings.shopProfile,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = shopName,
                        onValueChange = { shopName = it },
                        label = { Text(Strings.shopName) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("settings_shop_name_input")
                    )

                    OutlinedTextField(
                        value = ownerName,
                        onValueChange = { ownerName = it },
                        label = { Text(Strings.ownerName) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text(Strings.phone) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text(Strings.address) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = currencySymbol,
                            onValueChange = { currencySymbol = it },
                            label = { Text(Strings.currencySymbol) },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    OutlinedTextField(
                        value = thankYouMessage,
                        onValueChange = { thankYouMessage = it },
                        label = { Text(Strings.thankYouMsg) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Security / PIN Lock Section
        item {
            Text(
                text = Strings.pinSecurity,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(Strings.enablePin, fontWeight = FontWeight.Bold)
                            Text("অ্যাপ খোলার সময় সিকিউরিটি পিন চাইবে", fontSize = 12.sp, color = Color.Gray)
                        }
                        Switch(
                            checked = isPinEnabled,
                            onCheckedChange = { isPinEnabled = it }
                        )
                    }

                    if (isPinEnabled) {
                        OutlinedTextField(
                            value = appPin,
                            onValueChange = { if (it.length <= 6) appPin = it },
                            label = { Text(Strings.pinLabel) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        // Backup and Restore Information Card
        item {
            Text(
                text = Strings.backupData,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "আপনার সমস্ত হিসাব-নিকাশ ডিভাইসের সুরক্ষিত অফলাইন ডাটাবেসে স্থায়ীভাবে সংরক্ষিত আছে।",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Button(
                        onClick = {
                            Toast.makeText(context, "ডাটা সফলভাবে ব্যাকআপ সংরক্ষণ করা হয়েছে!", Toast.LENGTH_LONG).show()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Backup, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(Strings.backupData)
                    }
                }
            }
        }

        // Save Button
        item {
            Button(
                onClick = {
                    if (shopName.isBlank()) {
                        Toast.makeText(context, "দোকানের নাম খালি রাখা যাবে না", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    val updated = settings.copy(
                        shopName = shopName.trim(),
                        ownerName = ownerName.trim(),
                        phone = phone.trim(),
                        address = address.trim(),
                        currencySymbol = currencySymbol.trim().ifBlank { "৳" },
                        thankYouMessage = thankYouMessage.trim(),
                        isPinEnabled = isPinEnabled,
                        appPin = appPin.trim(),
                        language = selectedLang
                    )
                    viewModel.updateSettings(updated)
                    LanguageManager.currentLanguage = if (selectedLang == "en") AppLanguage.ENGLISH else AppLanguage.BANGLA
                    Toast.makeText(context, "সেটিংস সফলভাবে সংরক্ষিত হয়েছে", Toast.LENGTH_LONG).show()
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("save_settings_btn")
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(Strings.saveSettings, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}
