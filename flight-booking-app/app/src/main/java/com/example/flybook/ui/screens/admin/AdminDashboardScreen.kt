package com.example.flybook.ui.screens.admin

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
import com.example.flybook.ui.theme.*
import com.example.flybook.ui.components.BottomNavigationBar
import com.example.flybook.ui.components.adminBottomNavItems

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Trang Quản Trị") },
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
                items = adminBottomNavItems
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
            // Welcome card - compact version
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
                        imageVector = Icons.Default.Person,
                        contentDescription = "Admin",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column {
                    Text(
                        text = "Trang Quản Trị",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                    Text(
                        text = "Quản lý hệ thống đặt vé máy bay",
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
                    title = "Phê duyệt hãng",
                    description = "Quản lý và phê duyệt các hãng hàng không mới",
                    icon = Icons.Default.CheckCircle,
                    gradientColors = listOf(PrimaryBlue, PrimaryPurple),
                    onClick = { navController.navigate(Screen.AdminAirlineApproval.route) }
                )
                
                DashboardCard(
                    title = "Quản lý sân bay",
                    description = "Thêm, sửa, xóa thông tin sân bay",
                    icon = Icons.Default.LocationOn,
                    gradientColors = listOf(Color(0xFFF093FB), Color(0xFFF5576C)),
                    onClick = { navController.navigate(Screen.AdminAirportManagement.route) }
                )
                
                DashboardCard(
                    title = "Quản lý tuyến bay",
                    description = "Thiết lập và quản lý các tuyến bay",
                    icon = Icons.Default.LocationOn,
                    gradientColors = listOf(Color(0xFF4FACFE), Color(0xFF00F2FE)),
                    onClick = { navController.navigate(Screen.AdminRouteManagement.route) }
                )
                
                DashboardCard(
                    title = "Báo cáo tổng hợp",
                    description = "Xem các báo cáo và thống kê hệ thống",
                    icon = Icons.Default.Home,
                    gradientColors = listOf(Color(0xFF43E97B), Color(0xFF38F9D7)),
                    onClick = { /* TODO */ }
                )
                
                DashboardCard(
                    title = "Cấu hình hệ thống",
                    description = "Thiết lập các thông số hệ thống",
                    icon = Icons.Default.Settings,
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

