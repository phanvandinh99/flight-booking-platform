package com.example.flybook

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.flybook.navigation.NavGraph
import com.example.flybook.navigation.Screen
import com.example.flybook.ui.theme.FlyBookTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FlyBookTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    var handleIntent by remember { mutableStateOf(true) }
                    
                    // Xử lý deep link khi app được mở từ browser
                    LaunchedEffect(handleIntent) {
                        handleIntent = false
                        val intent = this@MainActivity.intent
                        val data: Uri? = intent.data
                        
                        if (data != null) {
                            // Xử lý callback từ VNPay
                            val status = data.getQueryParameter("status")
                            val bookingId = data.getQueryParameter("booking_id")
                            
                            if (status == "success" && bookingId != null) {
                                // Navigate đến màn hình đặt vé và reload
                                navController.navigate(Screen.MyBookings.route) {
                                    popUpTo(Screen.Home.route) { inclusive = false }
                                    launchSingleTop = true
                                }
                            } else if (status == "failed") {
                                // Navigate đến màn hình đặt vé để xem lỗi
                                navController.navigate(Screen.MyBookings.route) {
                                    popUpTo(Screen.Home.route) { inclusive = false }
                                    launchSingleTop = true
                                }
                            }
                        }
                    }
                    
                    NavGraph(navController = navController)
                }
            }
        }
    }
    
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        // Trigger recomposition để xử lý intent mới
        recreate()
    }
}