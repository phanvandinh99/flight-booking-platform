package com.example.flybook.ui.screens.bookings

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.flybook.data.models.Booking
import com.example.flybook.data.models.Flight
import com.example.flybook.data.repository.BookingRepository
import com.example.flybook.ui.components.BottomNavigationBar
import com.example.flybook.ui.components.customerBottomNavItems
import com.example.flybook.ui.components.formatCurrency
import com.example.flybook.ui.components.formatTime
import com.example.flybook.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyBookingsScreen(navController: NavController) {
    val context = LocalContext.current
    val bookingRepository = remember { BookingRepository() }
    val scope = rememberCoroutineScope()
    
    var isLoading by remember { mutableStateOf(true) }
    var bookings by remember { mutableStateOf<List<Booking>>(emptyList()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var processingPayment by remember { mutableStateOf<Int?>(null) }
    var cancelling by remember { mutableStateOf<Int?>(null) }
    var showCancelDialog by remember { mutableStateOf<Booking?>(null) }
    
    fun loadBookings() {
        scope.launch {
            isLoading = true
            errorMessage = null
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
    
    LaunchedEffect(Unit) {
        loadBookings()
    }
    
    fun handlePayment(bookingId: Int) {
        scope.launch {
            processingPayment = bookingId
            errorMessage = null
            bookingRepository.createPayment(bookingId)
                .onSuccess { paymentResponse ->
                    // Mở browser để thanh toán VNPay
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(paymentResponse.payment_url))
                    context.startActivity(intent)
                }
                .onFailure { e ->
                    errorMessage = e.message ?: "Không thể tạo URL thanh toán"
                }
            processingPayment = null
        }
    }
    
    fun handleCancel(booking: Booking) {
        showCancelDialog = null
        scope.launch {
            cancelling = booking.id
            errorMessage = null
            bookingRepository.cancelBooking(booking.id)
                .onSuccess {
                    // Reload danh sách
                    loadBookings()
                }
                .onFailure { e ->
                    errorMessage = e.message ?: "Không thể hủy đặt vé"
                }
            cancelling = null
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Đặt vé của tôi") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryBlue,
                    titleContentColor = Color.White
                ),
                actions = {
                    IconButton(onClick = { loadBookings() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Làm mới")
                    }
                }
            )
        },
        bottomBar = {
            BottomNavigationBar(
                navController = navController,
                items = customerBottomNavItems
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (errorMessage != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = errorMessage ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        IconButton(onClick = { errorMessage = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Đóng")
                        }
                    }
                }
            }
            
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (bookings.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            Icons.Default.ShoppingCart,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Chưa có đặt vé nào",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            text = "Hãy đặt vé để xem ở đây",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(bookings.size) { index ->
                        BookingCard(
                            booking = bookings[index],
                            onPaymentClick = { handlePayment(it) },
                            onCancelClick = { showCancelDialog = it },
                            processingPayment = processingPayment,
                            cancelling = cancelling
                        )
                    }
                }
            }
        }
    }
    
    // Dialog xác nhận hủy
    showCancelDialog?.let { booking ->
        AlertDialog(
            onDismissRequest = { showCancelDialog = null },
            title = { Text("Xác nhận hủy đặt vé") },
            text = {
                Text("Bạn có chắc chắn muốn hủy đặt vé ${booking.ma_dat_ve} không?")
            },
            confirmButton = {
                TextButton(
                    onClick = { handleCancel(booking) },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Hủy đặt vé")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialog = null }) {
                    Text("Không")
                }
            }
        )
    }
}

@Composable
fun BookingCard(
    booking: Booking,
    onPaymentClick: (Int) -> Unit,
    onCancelClick: (Booking) -> Unit,
    processingPayment: Int?,
    cancelling: Int?
) {
    val statusLabel = getStatusLabel(booking.trang_thai)
    val statusColor = getStatusColor(booking.trang_thai)
    val canPayOrCancel = booking.trang_thai == "cho_thanh_toan" || 
                        booking.trang_thai == "giu_cho" ||
                        booking.trang_thai == "chờ_thanh_toan"
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header: Mã đặt vé và trạng thái
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Mã đặt vé: ${booking.ma_dat_ve}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = formatDateTime(booking.ngay_dat ?: ""),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Surface(
                    color = statusColor.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = statusLabel,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = statusColor,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            Divider()
            
            // Thông tin chuyến bay
            booking.chuyen_bay?.let { flight ->
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = flight.tuyen_bay?.san_bay_di?.ma_san_bay ?: "N/A",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = flight.tuyen_bay?.san_bay_di?.ten_san_bay ?: "",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = formatTime(flight.gio_khoi_hanh),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        
                        Icon(
                            Icons.Default.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.padding(horizontal = 8.dp),
                            tint = PrimaryBlue
                        )
                        
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.End
                        ) {
                            Text(
                                text = flight.tuyen_bay?.san_bay_den?.ma_san_bay ?: "N/A",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.End
                            )
                            Text(
                                text = flight.tuyen_bay?.san_bay_den?.ten_san_bay ?: "",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.End
                            )
                            Text(
                                text = formatTime(flight.gio_ha_canh),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.End
                            )
                        }
                    }
                    
                    Text(
                        text = "${flight.hang_hang_khong?.ten_hang ?: ""} - ${flight.ma_chuyen_bay}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Divider()
            
            // Thông tin chi tiết
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Số hành khách:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${booking.hanh_khach?.size ?: 0}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Tổng tiền:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatCurrency(booking.tong_tien),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryBlue
                    )
                }
                
                booking.thoi_gian_het_han?.let { expiryTime ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Hết hạn thanh toán:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = formatDateTime(expiryTime),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
            
            // Hành khách
            booking.hanh_khach?.takeIf { it.isNotEmpty() }?.let { passengers ->
                Divider()
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Hành khách:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium
                    )
                    passengers.forEach { passenger ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = passenger.ho_ten,
                                style = MaterialTheme.typography.bodySmall
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = getPassengerTypeLabel(passenger.loai_hanh_khach),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                passenger.so_ghe?.let { seat ->
                                    Text(
                                        text = "Ghế: $seat",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = PrimaryBlue,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            // Actions
            if (canPayOrCancel) {
                Divider()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { onCancelClick(booking) },
                        modifier = Modifier.weight(1f),
                        enabled = cancelling != booking.id
                    ) {
                        if (cancelling == booking.id) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Hủy")
                    }
                    
                    Button(
                        onClick = { onPaymentClick(booking.id) },
                        modifier = Modifier.weight(1f),
                        enabled = processingPayment != booking.id,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryBlue
                        )
                    ) {
                        if (processingPayment == booking.id) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = Color.White
                            )
                        } else {
                            Icon(
                                Icons.Default.ShoppingCart,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Thanh toán")
                    }
                }
            } else if (booking.trang_thai == "da_thanh_toan") {
                Divider()
                Surface(
                    color = SuccessGreen.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = SuccessGreen,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Đã thanh toán",
                            style = MaterialTheme.typography.bodyMedium,
                            color = SuccessGreen,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            } else if (booking.trang_thai == "da_huy") {
                Divider()
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Đã hủy",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

fun getStatusLabel(status: String): String {
    return when (status) {
        "cho_thanh_toan", "chờ_thanh_toan", "giu_cho" -> "Chờ thanh toán"
        "da_thanh_toan" -> "Đã thanh toán"
        "da_huy" -> "Đã hủy"
        "da_hoan_tien" -> "Đã hoàn tiền"
        "het_han" -> "Hết hạn"
        else -> status
    }
}

@Composable
fun getStatusColor(status: String): Color {
    return when (status) {
        "cho_thanh_toan", "chờ_thanh_toan", "giu_cho" -> WarningYellow
        "da_thanh_toan" -> SuccessGreen
        "da_huy", "het_han" -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
}

fun getHangVeLabel(hangVe: String): String {
    return when (hangVe) {
        "pho_thong" -> "Phổ thông"
        "pho_thong_cao_cap" -> "Phổ thông cao cấp"
        "thuong_gia" -> "Thương gia"
        "hang_nhat" -> "Hạng nhất"
        else -> hangVe
    }
}

fun getPassengerTypeLabel(type: String): String {
    return when (type) {
        "nguoi_lon" -> "Người lớn"
        "tre_em" -> "Trẻ em"
        "em_be" -> "Em bé"
        else -> type
    }
}

fun formatDateTime(dateString: String?): String {
    if (dateString.isNullOrBlank()) {
        return "N/A"
    }
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val date = inputFormat.parse(dateString)
        outputFormat.format(date ?: Date())
    } catch (e: Exception) {
        try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            val date = inputFormat.parse(dateString)
            outputFormat.format(date ?: Date())
        } catch (e2: Exception) {
            dateString
        }
    }
}
