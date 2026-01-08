package com.smartqr.app.ui.screens

import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import com.smartqr.app.data.HistoryItem
import com.smartqr.app.utils.QRUtils
import kotlinx.coroutines.launch

@Composable
fun GenerateScreen(onSaveHistory: (HistoryItem) -> Unit) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Text", "URL", "Wi-Fi")
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val scope = rememberCoroutineScope()

    var textInput by remember { mutableStateOf("") }
    var wifiSsid by remember { mutableStateOf("") }
    var wifiPass by remember { mutableStateOf("") }
    var qrBitmap by remember { mutableStateOf<Bitmap?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
                Tab(selected = selectedTab == index, onClick = { selectedTab = index; qrBitmap = null }, text = { Text(title) })
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (selectedTab) {
                2 -> { // Wi-Fi
                    OutlinedTextField(
                        value = wifiSsid,
                        onValueChange = { wifiSsid = it },
                        label = { Text("SSID") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = wifiPass,
                        onValueChange = { wifiPass = it },
                        label = { Text("Password") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                else -> {
                    OutlinedTextField(
                        value = textInput,
                        onValueChange = { textInput = it },
                        label = { Text(if (selectedTab == 1) "https://example.com" else "Enter text") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    keyboardController?.hide()
                    val content = if (selectedTab == 2) "WIFI:S:$wifiSsid;T:WPA;P:$wifiPass;;" else textInput
                    if (content.isNotBlank()) {
                        qrBitmap = QRUtils.generateQRCode(content)
                        onSaveHistory(HistoryItem(content = content, type = "GENERATE"))
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("توليد QR")
            }

            Spacer(modifier = Modifier.height(24.dp))

            qrBitmap?.let { bitmap ->
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "QR Code",
                    modifier = Modifier.size(250.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row {
                    Button(onClick = {
                        scope.launch {
                            QRUtils.saveImageToGallery(context, bitmap, "SmartQR")
                            Toast.makeText(context, "تم الحفظ", Toast.LENGTH_SHORT).show()
                        }
                    }) {
                        Text("حفظ")
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    FilledTonalIconButton(onClick = {
                        scope.launch {
                            val uri = QRUtils.saveImageToGallery(context, bitmap, "SmartQR_Share")
                            uri?.let {
                                val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                    type = "image/png"
                                    putExtra(android.content.Intent.EXTRA_STREAM, it)
                                }
                                context.startActivity(android.content.Intent.createChooser(intent, "Share QR"))
                            }
                        }
                    }) {
                        Icon(Icons.Default.Share, "Share")
                    }
                }
            }
        }
    }
}