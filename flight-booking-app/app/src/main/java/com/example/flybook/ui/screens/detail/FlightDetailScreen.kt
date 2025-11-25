package com.example.flybook.ui.screens.detail

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.flybook.data.repository.CustomerFlightRepository
import com.example.flybook.navigation.Screen
import com.example.flybook.ui.components.formatCurrency
import com.example.flybook.ui.components.formatTime
import com.example.flybook.ui.components.BottomNavigationBar
import com.example.flybook.ui.components.customerBottomNavItems
import com.example.flybook.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlightDetailScreen(
    navController: NavController,
    flightId: Int
) {
    val flightRepository = remember { CustomerFlightRepository() }
    val scope = rememberCoroutineScope()
    
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var flight by remember { mutableStateOf<com.example.flybook.data.models.Flight?>(null) }
    
    LaunchedEffect(flightId) {
        scope.launch {
            flightRepository.getFlightDetail(flightId)
                .onSuccess {
                    flight = it
                    errorMessage = null
                }
                .onFailure { e ->
                    errorMessage = e.message ?: "Không thể tải chi tiết chuyến bay"
                }
            isLoading = false
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chi tiết chuyến bay") },
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
                items = customerBottomNavItems
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (errorMessage != null) {
            Card(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Lỗi",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = errorMessage ?: "",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Button(onClick = { navController.popBackStack() }) {
                        Text("Quay lại")
                    }
                }
            }
        } else if (flight != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = flight!!.tuyen_bay?.san_bay_di?.ma_san_bay ?: "",
                                    style = MaterialTheme.typography.headlineMedium
                                )
                                Text(
                                    text = flight!!.tuyen_bay?.san_bay_di?.ten_san_bay ?: "",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = formatTime(flight!!.gio_khoi_hanh),
                                    style = MaterialTheme.typography.titleLarge,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }
                            
                            Text(
                                text = "→",
                                style = MaterialTheme.typography.headlineLarge
                            )
                            
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = flight!!.tuyen_bay?.san_bay_den?.ma_san_bay ?: "",
                                    style = MaterialTheme.typography.headlineMedium
                                )
                                Text(
                                    text = flight!!.tuyen_bay?.san_bay_den?.ten_san_bay ?: "",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = formatTime(flight!!.gio_ha_canh),
                                    style = MaterialTheme.typography.titleLarge,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }
                        }
                        
                        Divider()
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Hãng hàng không:",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = flight!!.hang_hang_khong?.ten_hang ?: "",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Mã chuyến bay:",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = flight!!.ma_chuyen_bay,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        
                        if (flight!!.may_bay != null) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Loại máy bay:",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = flight!!.may_bay!!.loai_may_bay,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
                
                if (flight!!.gia_ve != null && flight!!.gia_ve!!.isNotEmpty()) {
                    Text(
                        text = "Giá vé",
                        style = MaterialTheme.typography.titleLarge
                    )
                    
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        flight!!.gia_ve!!.forEach { price ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = {
                                    navController.navigate(Screen.Booking.route)
                                }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = when (price.hang_ve) {
                                                "pho_thong" -> "Phổ thông"
                                                "thuong_gia" -> "Thương gia"
                                                "hang_nhat" -> "Hạng nhất"
                                                else -> price.hang_ve
                                            },
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                        if (price.so_ghe_trong != null) {
                                            Text(
                                                text = "${price.so_ghe_trong} ghế trống",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                    Text(
                                        text = formatCurrency(price.gia),
                                        style = MaterialTheme.typography.titleLarge,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

