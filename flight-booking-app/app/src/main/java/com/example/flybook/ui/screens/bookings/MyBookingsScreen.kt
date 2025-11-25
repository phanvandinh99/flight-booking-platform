package com.example.flybook.ui.screens.bookings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.flybook.data.repository.BookingRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyBookingsScreen(navController: NavController) {
    val bookingRepository = remember { BookingRepository() }
    val scope = rememberCoroutineScope()
    
    var isLoading by remember { mutableStateOf(true) }
    var bookings by remember { mutableStateOf<List<com.example.flybook.data.models.Booking>>(emptyList()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    LaunchedEffect(Unit) {
        scope.launch {
            bookingRepository.getBookings()
                .onSuccess {
                    bookings = it
                    errorMessage = null
                }
                .onFailure { e ->
                    errorMessage = e.message ?: "Không thể tải danh sách đặt vé"
                }
            isLoading = false
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Đặt vé của tôi") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Text("←")
                    }
                }
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
                }
            }
        } else if (bookings.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Chưa có đặt vé nào",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "Hãy đặt vé để xem ở đây",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                bookings.forEach { booking ->
                    BookingCard(booking = booking)
                }
            }
        }
    }
}

@Composable
fun BookingCard(booking: com.example.flybook.data.models.Booking) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = booking.ma_dat_ve,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = when (booking.trang_thai) {
                        "cho_thanh_toan" -> "Chờ thanh toán"
                        "da_thanh_toan" -> "Đã thanh toán"
                        "da_huy" -> "Đã hủy"
                        else -> booking.trang_thai
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = when (booking.trang_thai) {
                        "cho_thanh_toan" -> MaterialTheme.colorScheme.primary
                        "da_thanh_toan" -> MaterialTheme.colorScheme.tertiary
                        "da_huy" -> MaterialTheme.colorScheme.error
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
            
            if (booking.chuyen_bay != null) {
                Text(
                    text = "${booking.chuyen_bay!!.tuyen_bay?.san_bay_di?.ma_san_bay} → ${booking.chuyen_bay!!.tuyen_bay?.san_bay_den?.ma_san_bay}",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            
            Text(
                text = "Tổng tiền: ${com.example.flybook.ui.components.formatCurrency(booking.tong_tien)}",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

