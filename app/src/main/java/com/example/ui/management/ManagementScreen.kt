package com.example.ui.management

import com.example.ui.shared.*
import com.example.ui.finance.FinanceViewModel


import android.content.Context
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.BudgetGoal
import com.example.data.model.Transaction
import com.example.data.model.CategoryItem
import com.example.data.model.LearnedRule
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*


@Composable
fun ManagementTab(
    viewModel: FinanceViewModel,
    labels: Map<String, String>,
    isDarkMode: Boolean
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val isBiometricsActive by viewModel.isBiometricsEnabled.collectAsState()
    val checkedLang by viewModel.language.collectAsState()
    val selectCurrency by viewModel.selectedCurrency.collectAsState()
    val liveRates by viewModel.exchangeRatesState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            labels["config_security"] ?: "Configuration",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = if (isDarkMode) Color.White else Color.Black
        )

        // EXPORT FILES HUB CARD
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isDarkMode) Color(0xFF161F30) else Color.White
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = if (labels["app_tag"] == "COACH CON INTELIGENCIA ARTIFICIAL") "Módulo De Exportación E2EE" else "E2EE Secured Data Export",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDarkMode) Color.White else Color.Black
                )

                Button(
                    onClick = {
                        val csv = viewModel.getCsvContent()
                        clipboardManager.setText(AnnotatedString(csv))
                        Toast.makeText(context, "${labels["export_csv"]}: ${labels["copied_clipboard"]}", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isDarkMode) Color(0xFF3F4759) else MaterialTheme.colorScheme.primaryContainer,
                        contentColor = if (isDarkMode) Color(0xFFD1E4FF) else MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("export_csv_btn")
                ) {
                    Icon(Icons.Filled.InsertDriveFile, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(labels["export_csv"] ?: "Export CSV")
                }

                Button(
                    onClick = {
                        val pdf = viewModel.getPdfReportContent()
                        clipboardManager.setText(AnnotatedString(pdf))
                        Toast.makeText(context, "${labels["export_pdf"]}: ${labels["copied_clipboard"]}", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isDarkMode) Color(0xFF3F4759) else MaterialTheme.colorScheme.primaryContainer,
                        contentColor = if (isDarkMode) Color(0xFFD1E4FF) else MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("export_pdf_btn")
                ) {
                    Icon(Icons.Filled.PictureAsPdf, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(labels["export_pdf"] ?: "Export PDF")
                }
            }
        }

        // SETTINGS TOGGLES LAYOUT
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isDarkMode) Color(0xFF1A1C1E) else Color.White
            ),
            border = if (isDarkMode) BorderStroke(1.dp, Color(0xFF2D3135)) else null,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Biometrics switch
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(labels["config_biometrics"] ?: "", color = if (isDarkMode) Color.White else Color.Black, fontWeight = FontWeight.Medium)
                    Switch(
                        checked = isBiometricsActive,
                        onCheckedChange = { viewModel.toggleBiometrics() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = if (isDarkMode) Color(0xFFD1E4FF) else MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.testTag("biometric_toggle")
                    )
                }

                Divider(color = if (isDarkMode) Color(0xFF2D3135) else Color(0xFFE5E5E5))

                // Dark mode switch
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(labels["config_darkmode"] ?: "", color = if (isDarkMode) Color.White else Color.Black, fontWeight = FontWeight.Medium)
                    Switch(
                        checked = isDarkMode,
                        onCheckedChange = { viewModel.toggleDarkMode() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = if (isDarkMode) Color(0xFFD1E4FF) else MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.testTag("dark_mode_toggle")
                    )
                }

                Divider(color = if (isDarkMode) Color(0xFF2D3135) else Color(0xFFE5E5E5))

                // Language Switcher Toggle Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(labels["config_language"] ?: "", color = if (isDarkMode) Color.White else Color.Black, fontWeight = FontWeight.Medium)

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Button(
                            onClick = { viewModel.setLanguage("es") },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (checkedLang == "es") (if (isDarkMode) Color(0xFF3F4759) else Color(0xFFE3EDF7)) else Color.Transparent,
                                contentColor = if (checkedLang == "es") (if (isDarkMode) Color(0xFFD1E4FF) else Color.Black) else Color.Gray
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp).testTag("lang_toggle_es")
                        ) {
                            Text("ESP 🇪🇸", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { viewModel.setLanguage("en") },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (checkedLang == "en") (if (isDarkMode) Color(0xFF3F4759) else Color(0xFFE3EDF7)) else Color.Transparent,
                                contentColor = if (checkedLang == "en") (if (isDarkMode) Color(0xFFD1E4FF) else Color.Black) else Color.Gray
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp).testTag("lang_toggle_en")
                        ) {
                            Text("ENG 🇬🇧", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Divider(color = if (isDarkMode) Color(0xFF2D3135) else Color(0xFFE5E5E5))

                // Pet Style Settings Toggle Row
                val currentPetStyle by viewModel.petStyle.collectAsState()
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                ) {
                    Text(
                        text = if (checkedLang == "es") "Estilo de Mascota" else "Pet Style",
                        color = if (isDarkMode) Color.White else Color.Black,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("FUTURISTIC" to (if (checkedLang == "es") "Futurista" else "Futuristic"),
                                "TRADITIONAL" to (if (checkedLang == "es") "Tradicional" else "Traditional"),
                                "ZEN" to "Zen").forEach { (style, label) ->
                            Button(
                                onClick = { viewModel.setPetStyle(style) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (currentPetStyle == style) (if (isDarkMode) Color(0xFF3F4759) else Color(0xFFE3EDF7)) else Color.Transparent,
                                    contentColor = if (currentPetStyle == style) (if (isDarkMode) Color(0xFFD1E4FF) else Color.Black) else Color.Gray
                                ),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.height(32.dp).testTag("pet_style_$style")
                            ) {
                                Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Divider(color = if (isDarkMode) Color(0xFF2D3135) else Color(0xFFE5E5E5))

                // Base Currency Switcher Toggle Row
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (checkedLang == "es") "Divisa Base Global" else "Global Base Currency",
                            color = if (isDarkMode) Color.White else Color.Black,
                            fontWeight = FontWeight.Medium
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.horizontalScroll(rememberScrollState())
                        ) {
                            listOf("EUR", "USD", "GBP", "JPY").forEach { curr ->
                                val label = when(curr) {
                                    "EUR" -> "EUR 🇪🇺"
                                    "USD" -> "USD 🇺🇸"
                                    "GBP" -> "GBP 🇬🇧"
                                    "JPY" -> "JPY 🇯🇵"
                                    else -> curr
                                }
                                Button(
                                    onClick = { viewModel.setBaseCurrency(curr) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (selectCurrency == curr) (if (isDarkMode) Color(0xFF3F4759) else Color(0xFFE3EDF7)) else Color.Transparent,
                                        contentColor = if (selectCurrency == curr) (if (isDarkMode) Color(0xFFD1E4FF) else Color.Black) else Color.Gray
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                    modifier = Modifier.height(32.dp).testTag("currency_toggle_$curr")
                                ) {
                                    Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Simulated live stock rates panel
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isDarkMode) Color(0x0AFFFFFF) else Color(0x05000000))
                            .border(1.dp, if (isDarkMode) Color(0x11FFFFFF) else Color(0x0A000000), RoundedCornerShape(12.dp))
                            .padding(10.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF107C41))
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (checkedLang == "es") "MULTIACTIVOS EN TIEMPO REAL" else "REAL-TIME SYNCED ASSETS",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF107C41)
                                    )
                                }
                                Text(
                                    text = "Base: 1 $selectCurrency",
                                    fontSize = 9.sp,
                                    color = Color.Gray
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                listOf("EUR", "USD", "GBP", "JPY").filter { it != selectCurrency }.forEach { alt ->
                                    val amountInAlt = viewModel.convertCurrency(1.0, selectCurrency, alt)
                                    val symbol = viewModel.getCurrencySymbol(alt)
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(alt, fontSize = 10.sp, color = Color.Gray)
                                        Text(
                                            text = if (alt == "JPY") "${"%,.1f".format(amountInAlt)}$symbol" else "${"%,.3f".format(amountInAlt)}$symbol",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isDarkMode) Color.White else Color.Black
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // DANGER ZONE CARD
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isDarkMode) Color(0xFF261217) else Color(0xFFFFF0F1)
            ),
            border = BorderStroke(1.dp, Color(0xFFE53935)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(labels["config_danger_zone"] ?: "", color = Color(0xFFE53935), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = {
                        viewModel.clearAllTransactions()
                        Toast.makeText(context, "Base de datos restablecida.", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFD32F2F),
                        contentColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("danger_wipe_btn")
                ) {
                    Icon(Icons.Filled.DeleteForever, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(labels["config_delete_all"] ?: "Clear SQLite Database")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // SESSION CARD
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isDarkMode) Color(0xFF1A1C1E) else Color.White
            ),
            border = if (isDarkMode) BorderStroke(1.dp, Color(0xFF2D3135)) else null,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Button(
                    onClick = {
                        viewModel.logOut()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isDarkMode) Color(0xFF3F4759) else Color(0xFFE3EDF7),
                        contentColor = if (isDarkMode) Color(0xFFD1E4FF) else MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("logout_btn"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Filled.Logout, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(labels["logout"] ?: "Log out")
                }
            }
        }
    }
}

// --- FULL FORM MODALS (DIALOG POPUPS) ---

