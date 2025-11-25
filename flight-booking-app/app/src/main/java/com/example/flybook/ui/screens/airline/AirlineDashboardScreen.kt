package com.example.flybook.ui.screens.airline

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.flybook.navigation.Screen
import com.example.flybook.ui.components.BottomNavigationBar
import com.example.flybook.ui.components.airlineBottomNavItems
import com.example.flybook.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AirlineDashboardScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Trang Quản Lý Hãng") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryBlue,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        bottomBar = {
            BottomNavigationBar(
                navController = navController,
                items = airlineBottomNavItems
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(BackgroundLight)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Welcome card
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(PrimaryBlue, PrimaryPurple)
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Airline",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column {
                    Text(
                        text = "Trang Quản Lý Hãng",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                    Text(
                        text = "Quản lý máy bay, chuyến bay và giá vé",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
            
            // Dashboard cards grid
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DashboardCard(
                    title = "Quản lý máy bay",
                    description = "Thêm, sửa, xóa thông tin máy bay",
                    icon = Icons.Default.LocationOn,
                    gradientColors = listOf(PrimaryBlue, PrimaryPurple),
                    onClick = { navController.navigate(Screen.AirlineAircraftManagement.route) }
                )
                
                DashboardCard(
                    title = "Quản lý chuyến bay",
                    description = "Tạo và quản lý các chuyến bay",
                    icon = Icons.Default.LocationOn,
                    gradientColors = listOf(Color(0xFF4FACFE), Color(0xFF00F2FE)),
                    onClick = { /* TODO */ }
                )
                
                DashboardCard(
                    title = "Quản lý giá vé",
                    description = "Thiết lập giá vé cho các chuyến bay",
                    icon = Icons.Default.ShoppingCart,
                    gradientColors = listOf(Color(0xFF43E97B), Color(0xFF38F9D7)),
                    onClick = { /* TODO */ }
                )
                
                DashboardCard(
                    title = "Quản lý đặt vé",
                    description = "Xem và quản lý các đặt vé",
                    icon = Icons.Default.List,
                    gradientColors = listOf(Color(0xFFF093FB), Color(0xFFF5576C)),
                    onClick = { /* TODO */ }
                )
                
                DashboardCard(
                    title = "Báo cáo & Thống kê",
                    description = "Xem báo cáo và thống kê doanh thu",
                    icon = Icons.Default.Info,
                    gradientColors = listOf(Color(0xFFFA709A), Color(0xFFFEE140)),
                    onClick = { /* TODO */ }
                )
            }
        }
    }
}

@Composable
fun DashboardCard(
    title: String,
    description: String,
    icon: ImageVector,
    gradientColors: List<Color>,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        brush = Brush.linearGradient(colors = gradientColors),
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
            
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = "Navigate",
                tint = TextSecondary
            )
        }
    }
}

