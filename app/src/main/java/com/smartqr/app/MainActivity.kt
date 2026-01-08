package com.smartqr.app

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.smartqr.app.ui.screens.GenerateScreen
import com.smartqr.app.ui.screens.HistoryScreen
import com.smartqr.app.ui.screens.ScanScreen
import com.smartqr.app.ui.theme.SmartQRTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        
        val db = (application as SmartQRApplication).database
        val dao = db.historyDao()

        setContent {
            SmartQRTheme {
                val navController = rememberNavController()
                val scope = rememberCoroutineScope()
                
                Scaffold(
                    bottomBar = {
                        NavigationBar {
                            NavigationBarItem(
                                selected = navController.currentDestination?.route == "scan",
                                onClick = { navController.navigate("scan") },
                                icon = { Icon(Icons.Default.QrCodeScanner, null) },
                                label = { Text("مسح") }
                            )
                            NavigationBarItem(
                                selected = navController.currentDestination?.route == "generate",
                                onClick = { navController.navigate("generate") },
                                icon = { Icon(Icons.Default.Create, null) },
                                label = { Text("إنشاء") }
                            )
                            NavigationBarItem(
                                selected = navController.currentDestination?.route == "history",
                                onClick = { navController.navigate("history") },
                                icon = { Icon(Icons.Default.History, null) },
                                label = { Text("السجل") }
                            )
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "scan",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("scan") {
                            ScanScreen { item ->
                                scope.launch(Dispatchers.IO) { dao.insert(item) }
                            }
                        }
                        composable("generate") {
                            GenerateScreen { item ->
                                scope.launch(Dispatchers.IO) { dao.insert(item) }
                            }
                        }
                        composable("history") {
                            HistoryScreen(dao)
                        }
                    }
                }
            }
        }
    }
}