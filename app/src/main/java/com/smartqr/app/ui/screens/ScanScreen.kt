package com.smartqr.app.ui.screens

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.smartqr.app.data.HistoryItem
import com.smartqr.app.utils.BarcodeAnalyzer
import kotlinx.coroutines.launch
import java.util.concurrent.Executors

@Composable
fun ScanScreen(onSaveHistory: (HistoryItem) -> Unit) {
    var hasPermission by remember { mutableStateOf(false) }
    
    // Simple permission check (in real app use Accompanist or ActivityResultLauncher)
    // Assuming MainActivity handles initial request or user granted it.
    // For simplicity in this generator, we check basic status.
    val context = LocalContext.current
    val permissionCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
    hasPermission = (permissionCheck == android.content.pm.PackageManager.PERMISSION_GRANTED)

    if (hasPermission) {
        CameraView(onBarcodeDetected = { code ->
             onSaveHistory(HistoryItem(content = code, type = "SCAN"))
        })
    } else {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("الرجاء منح صلاحية الكاميرا من الإعدادات")
        }
    }
}

@Composable
fun CameraView(onBarcodeDetected: (String) -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var flashEnabled by remember { mutableStateOf(false) }
    var cameraProvider: ProcessCameraProvider? by remember { mutableStateOf(null) }
    var cameraControl: androidx.camera.core.CameraControl? by remember { mutableStateOf(null) }
    var lastScannedCode by remember { mutableStateOf("") }
    
    // Debounce
    val scope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx)
                val executor = ContextCompat.getMainExecutor(ctx)
                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

                cameraProviderFuture.addListener({
                    cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }
                    val imageAnalyzer = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()
                        .also {
                            it.setAnalyzer(Executors.newSingleThreadExecutor(), BarcodeAnalyzer { code ->
                                if (code.isNotEmpty() && code != lastScannedCode) {
                                    lastScannedCode = code
                                    scope.launch {
                                        onBarcodeDetected(code)
                                    }
                                }
                            })
                        }
                    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                    try {
                        cameraProvider?.unbindAll()
                        val camera = cameraProvider?.bindToLifecycle(
                            lifecycleOwner, cameraSelector, preview, imageAnalyzer
                        )
                        cameraControl = camera?.cameraControl
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }, executor)
                previewView
            },
            modifier = Modifier.fillMaxSize()
        )

        // Overlay UI
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            if (lastScannedCode.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "تم المسح:", style = MaterialTheme.typography.labelMedium)
                        Text(text = lastScannedCode, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(vertical = 8.dp))
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Button(onClick = {
                                val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                val clip = android.content.ClipData.newPlainText("Scanned QR", lastScannedCode)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "تم النسخ", Toast.LENGTH_SHORT).show()
                            }) { Text("نسخ") }
                            
                            Button(onClick = {
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, lastScannedCode)
                                    type = "text/plain"
                                }
                                context.startActivity(Intent.createChooser(sendIntent, null))
                            }) { Text("مشاركة") }

                            if (lastScannedCode.startsWith("http")) {
                                Button(onClick = {
                                    try {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(lastScannedCode))
                                        context.startActivity(intent)
                                    } catch(e:Exception){}
                                }) { Text("فتح") }
                            }
                        }
                        // Reset button
                         Button(
                             onClick = { lastScannedCode = "" },
                             modifier = Modifier.fillMaxWidth().padding(top=8.dp),
                             colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                         ) { Text("مسح جديد") }
                    }
                }
            }
        }

        // Flash Button
        IconButton(
            onClick = {
                flashEnabled = !flashEnabled
                cameraControl?.enableTorch(flashEnabled)
            },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
                .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(50))
        ) {
            Icon(
                imageVector = if (flashEnabled) Icons.Default.FlashOn else Icons.Default.FlashOff,
                contentDescription = "Flash",
                tint = Color.White
            )
        }
    }
}