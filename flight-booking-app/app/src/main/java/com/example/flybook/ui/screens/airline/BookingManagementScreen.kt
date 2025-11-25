package com.example.flybook.ui.screens.airline

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.example.flybook.data.models.AirlineBooking
import com.example.flybook.data.models.AirlineFlight
import com.example.flybook.data.models.BookingStatistics
import com.example.flybook.data.repository.AirlineBookingRepository
import com.example.flybook.ui.components.BottomNavigationBar
import com.example.flybook.ui.components.airlineBottomNavItems
import com.example.flybook.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingManagementScreen(navController: NavController) {
    val repository = remember { AirlineBookingRepository() }
    val scope = rememberCoroutineScope()
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var bookings by remember { mutableStateOf<List<AirlineBooking>>(emptyList()) }
    var flights by remember { mutableStateOf<List<AirlineFlight>>(emptyList()) }
    var statistics by remember { mutableStateOf<BookingStatistics?>(null) }
    var searchTerm by remember { mutableStateOf("") }
    var filterTrangThai by remember { mutableStateOf("") }
    var filterMaChuyenBay by remember { mutableStateOf<Int?>(null) }
    var filterNgayDat by remember { mutableStateOf("") }
    var showDetailDialog by remember { mutableStateOf(false) }
    var selectedBooking by remember { mutableStateOf<AirlineBooking?>(null) }
    var showStatusDialog by remember { mutableStateOf(false) }
    var updatingStatus by remember { mutableStateOf(false) }
    
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val displayDateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    
    fun loadBookings() {
        scope.launch {
            loading = true
            error = null
            val result = repository.getBookings(
                maChuyenBay = filterMaChuyenBay,
                trangThai = if (filterTrangThai.isNotEmpty()) filterTrangThai else null,
                ngayDat = if (filterNgayDat.isNotEmpty()) filterNgayDat else null,
                maDatVe = if (searchTerm.isNotEmpty()) searchTerm else null
            )
            result.onSuccess {
                bookings = it
                error = null
            }.onFailure {
                error = it.message
            }
            loading = false
        }
    }
    
    fun loadFlights() {
        scope.launch {
            repository.getFlightsForBookings()
                .onSuccess { flights = it }
                .onFailure { }
        }
    }
    
    fun loadStatistics() {
        scope.launch {
            repository.getBookingStatistics()
                .onSuccess { statistics = it }
                .onFailure { }
        }
    }
    
    LaunchedEffect(Unit) {
        loadBookings()
        loadFlights()
        loadStatistics()
    }
    
    LaunchedEffect(filterTrangThai, filterMaChuyenBay, filterNgayDat) {
        loadBookings()
    }
    
    fun openDetailDialog(booking: AirlineBooking) {
        selectedBooking = booking
        showDetailDialog = true
    }
    
    fun closeDetailDialog() {
        showDetailDialog = false
        selectedBooking = null
    }
    
    fun openStatusDialog(booking: AirlineBooking) {
        selectedBooking = booking
        showStatusDialog = true
    }
    
    fun closeStatusDialog() {
        showStatusDialog = false
        selectedBooking = null
    }
    
    fun updateStatus(newStatus: String) {
        selectedBooking?.let { booking ->
            scope.launch {
                updatingStatus = true
                repository.updateBookingStatus(booking.id, newStatus)
                    .onSuccess {
                        loadBookings()
                        loadStatistics()
                        closeStatusDialog()
                    }
                    .onFailure {
                        error = it.message
                    }
                updatingStatus = false
            }
        }
    }
    
    val filteredBookings = bookings.filter {
        val matchesSearch = searchTerm.isBlank() ||
                it.ma_dat_ve.contains(searchTerm, ignoreCase = true) ||
                it.khach_hang?.ten_day_du?.contains(searchTerm, ignoreCase = true) == true ||
                it.chuyen_bay?.ma_chuyen_bay?.contains(searchTerm, ignoreCase = true) == true
        
        val matchesStatus = filterTrangThai.isBlank() || it.trang_thai == filterTrangThai
        val matchesFlight = filterMaChuyenBay == null || it.ma_chuyen_bay == filterMaChuyenBay
        
        matchesSearch && matchesStatus && matchesFlight
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Quản Lý Đặt Vé") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryBlue,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                ),
                actions = {
                    IconButton(onClick = { 
                        loadBookings()
                        loadStatistics()
                    }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = Color.White)
                    }
                }
            )
        },
        bottomBar = {
            BottomNavigationBar(
                navController = navController,
                items = airlineBottomNavItems
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(BackgroundLight)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Statistics Cards
                statistics?.let { stats ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StatisticCard(
                            title = "Tổng đặt vé",
                            value = stats.tong_so_dat_ve.toString(),
                            icon = Icons.Default.ShoppingCart,
                            color = PrimaryBlue,
                            modifier = Modifier.weight(1f)
                        )
                        StatisticCard(
                            title = "Đã thanh toán",
                            value = stats.da_thanh_toan.toString(),
                            icon = Icons.Default.CheckCircle,
                            color = SuccessGreen,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StatisticCard(
                            title = "Giữ chỗ",
                            value = stats.giu_cho.toString(),
                            icon = Icons.Default.Info,
                            color = WarningYellow,
                            modifier = Modifier.weight(1f)
                        )
                        StatisticCard(
                            title = "Đã hủy",
                            value = stats.da_huy.toString(),
                            icon = Icons.Default.Close,
                            color = ErrorRed,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = PrimaryBlue.copy(alpha = 0.1f))
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
                                    text = "Tổng doanh thu",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                                Text(
                                    text = "${String.format("%,.0f", stats.tong_doanh_thu)} VNĐ",
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                    color = PrimaryBlue
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "Trung bình",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                                Text(
                                    text = "${String.format("%,.0f", stats.doanh_thu_trung_binh)} VNĐ",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = TextPrimary
                                )
                            }
                        }
                    }
                }
                
                // Search bar
                OutlinedTextField(
                    value = searchTerm,
                    onValueChange = { searchTerm = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Tìm kiếm theo mã đặt vé, tên khách hàng...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                    trailingIcon = {
                        if (searchTerm.isNotEmpty()) {
                            IconButton(onClick = { searchTerm = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )
                
                // Filters
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Flight filter
                    var flightExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = flightExpanded,
                        onExpandedChange = { flightExpanded = !flightExpanded },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = flights.find { it.id == filterMaChuyenBay }?.let {
                                "${it.ma_chuyen_bay} - ${it.tuyen_bay?.san_bay_di?.ma_san_bay} → ${it.tuyen_bay?.san_bay_den?.ma_san_bay}"
                            } ?: "Tất cả chuyến bay",
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = flightExpanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = flightExpanded,
                            onDismissRequest = { flightExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Tất cả chuyến bay") },
                                onClick = {
                                    filterMaChuyenBay = null
                                    flightExpanded = false
                                }
                            )
                            flights.forEach { flight ->
                                DropdownMenuItem(
                                    text = { Text("${flight.ma_chuyen_bay} - ${flight.tuyen_bay?.san_bay_di?.ma_san_bay} → ${flight.tuyen_bay?.san_bay_den?.ma_san_bay}") },
                                    onClick = {
                                        filterMaChuyenBay = flight.id
                                        flightExpanded = false
                                    }
                                )
                            }
                        }
                    }
                    
                    // Status filter
                    var statusExpanded by remember { mutableStateOf(false) }
                    val statusOptions = mapOf(
                        "" to "Tất cả",
                        "giu_cho" to "Giữ chỗ",
                        "da_thanh_toan" to "Đã thanh toán",
                        "da_huy" to "Đã hủy"
                    )
                    ExposedDropdownMenuBox(
                        expanded = statusExpanded,
                        onExpandedChange = { statusExpanded = !statusExpanded },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = statusOptions[filterTrangThai] ?: "Tất cả",
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusExpanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = statusExpanded,
                            onDismissRequest = { statusExpanded = false }
                        ) {
                            statusOptions.forEach { (key, value) ->
                                DropdownMenuItem(
                                    text = { Text(value) },
                                    onClick = {
                                        filterTrangThai = key
                                        statusExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
                
                // Date filter
                OutlinedTextField(
                    value = filterNgayDat,
                    onValueChange = { filterNgayDat = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Lọc theo ngày đặt (YYYY-MM-DD)") },
                    leadingIcon = { Icon(Icons.Default.Info, contentDescription = "Ngày đặt") },
                    trailingIcon = {
                        if (filterNgayDat.isNotEmpty()) {
                            IconButton(onClick = { filterNgayDat = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )
                
                // Content
                when {
                    loading && bookings.isEmpty() -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                CircularProgressIndicator(color = PrimaryBlue)
                                Text(
                                    text = "Đang tải danh sách đặt vé...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                    
                    error != null -> {
                        BookingErrorView(
                            message = error!!,
                            onRetry = { loadBookings() }
                        )
                    }
                    
                    filteredBookings.isEmpty() -> {
                        EmptyBookingView(
                            hasSearch = searchTerm.isNotEmpty() || filterTrangThai.isNotEmpty() || filterMaChuyenBay != null || filterNgayDat.isNotEmpty()
                        )
                    }
                    
                    else -> {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(filteredBookings) { booking ->
                                BookingCard(
                                    booking = booking,
                                    displayDateFormat = displayDateFormat,
                                    onViewDetail = { openDetailDialog(booking) },
                                    onUpdateStatus = { openStatusDialog(booking) }
                                )
                            }
                        }
                    }
                }
            }
        }
        
        // Detail Dialog
        if (showDetailDialog && selectedBooking != null) {
            BookingDetailDialog(
                booking = selectedBooking!!,
                displayDateFormat = displayDateFormat,
                onDismiss = { closeDetailDialog() },
                onUpdateStatus = { openStatusDialog(selectedBooking!!) }
            )
        }
        
        // Status Update Dialog
        if (showStatusDialog && selectedBooking != null) {
            StatusUpdateDialog(
                booking = selectedBooking!!,
                onStatusChange = { newStatus -> updateStatus(newStatus) },
                onDismiss = { closeStatusDialog() },
                updating = updatingStatus
            )
        }
    }
}

@Composable
fun StatisticCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = color
                )
            }
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = color,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

@Composable
fun BookingCard(
    booking: AirlineBooking,
    displayDateFormat: SimpleDateFormat,
    onViewDetail: () -> Unit,
    onUpdateStatus: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = booking.ma_dat_ve,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = PrimaryBlue
                    )
                    Text(
                        text = booking.khach_hang?.ten_day_du ?: "N/A",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
                Badge(
                    containerColor = when (booking.trang_thai) {
                        "giu_cho" -> WarningYellow
                        "da_thanh_toan" -> SuccessGreen
                        "da_huy" -> ErrorRed
                        else -> TextSecondary
                    },
                    contentColor = Color.White
                ) {
                    Text(
                        text = when (booking.trang_thai) {
                            "giu_cho" -> "Giữ chỗ"
                            "da_thanh_toan" -> "Đã thanh toán"
                            "da_huy" -> "Đã hủy"
                            else -> booking.trang_thai
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Chuyến bay:", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    Text(
                        text = "${booking.chuyen_bay?.tuyen_bay?.san_bay_di?.ma_san_bay ?: "N/A"} → ${booking.chuyen_bay?.tuyen_bay?.san_bay_den?.ma_san_bay ?: "N/A"}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary
                    )
                    Text(
                        text = booking.chuyen_bay?.ma_chuyen_bay ?: "N/A",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Tổng tiền:", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    Text(
                        text = "${String.format("%,.0f", booking.tong_tien)} VNĐ",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = PrimaryBlue
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Divider(color = BorderLight, thickness = 1.dp)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Ngày đặt:", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    Text(
                        text = try {
                            SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(booking.created_at)?.let {
                                displayDateFormat.format(it)
                            } ?: booking.created_at
                        } catch (e: Exception) {
                            booking.created_at
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = onViewDetail,
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text("Chi tiết", fontSize = 12.sp)
                    }
                    Button(
                        onClick = onUpdateStatus,
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text("Cập nhật", fontSize = 12.sp, color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun BookingDetailDialog(
    booking: AirlineBooking,
    displayDateFormat: SimpleDateFormat,
    onDismiss: () -> Unit,
    onUpdateStatus: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Chi tiết đặt vé",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }
                
                Divider()
                
                // Booking Info
                InfoRow("Mã đặt vé:", booking.ma_dat_ve)
                InfoRow("Khách hàng:", booking.khach_hang?.ten_day_du ?: "N/A")
                InfoRow("Email:", booking.khach_hang?.email ?: "N/A")
                InfoRow("Số điện thoại:", booking.khach_hang?.so_dien_thoai ?: "N/A")
                
                Divider()
                
                // Flight Info
                Text("Thông tin chuyến bay", fontWeight = FontWeight.Bold)
                InfoRow("Mã chuyến bay:", booking.chuyen_bay?.ma_chuyen_bay ?: "N/A")
                InfoRow("Tuyến bay:", "${booking.chuyen_bay?.tuyen_bay?.san_bay_di?.ten_san_bay ?: "N/A"} → ${booking.chuyen_bay?.tuyen_bay?.san_bay_den?.ten_san_bay ?: "N/A"}")
                InfoRow("Giờ khởi hành:", try {
                    SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(booking.chuyen_bay?.gio_khoi_hanh ?: "")?.let {
                        displayDateFormat.format(it)
                    } ?: booking.chuyen_bay?.gio_khoi_hanh ?: "N/A"
                } catch (e: Exception) {
                    booking.chuyen_bay?.gio_khoi_hanh ?: "N/A"
                })
                
                Divider()
                
                // Passengers
                booking.hanh_khach?.let { passengers ->
                    Text("Hành khách (${passengers.size})", fontWeight = FontWeight.Bold)
                    passengers.forEachIndexed { index, passenger ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = BackgroundLight)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("Hành khách ${index + 1}", fontWeight = FontWeight.SemiBold)
                                InfoRow("Họ tên:", passenger.ho_ten)
                                passenger.ngay_sinh?.let { InfoRow("Ngày sinh:", it) }
                                passenger.cmnd_cccd?.let { InfoRow("CMND/CCCD:", it) }
                                passenger.loai_hanh_khach?.let { 
                                    InfoRow("Loại:", when(it) {
                                        "nguoi_lon" -> "Người lớn"
                                        "tre_em" -> "Trẻ em"
                                        "em_be" -> "Em bé"
                                        else -> it
                                    })
                                }
                            }
                        }
                    }
                }
                
                Divider()
                
                // Payment Info
                Text("Thông tin thanh toán", fontWeight = FontWeight.Bold)
                InfoRow("Tổng tiền:", "${String.format("%,.0f", booking.tong_tien)} VNĐ")
                InfoRow("Trạng thái:", when (booking.trang_thai) {
                    "giu_cho" -> "Giữ chỗ"
                    "da_thanh_toan" -> "Đã thanh toán"
                    "da_huy" -> "Đã hủy"
                    else -> booking.trang_thai
                })
                booking.ma_giao_dich?.let { InfoRow("Mã giao dịch:", it) }
                booking.thoi_gian_thanh_toan?.let {
                    InfoRow("Thời gian thanh toán:", try {
                        SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(it)?.let { date ->
                            displayDateFormat.format(date)
                        } ?: it
                    } catch (e: Exception) {
                        it
                    })
                }
                booking.thoi_gian_het_han_giu_cho?.let {
                    InfoRow("Hết hạn giữ chỗ:", try {
                        SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(it)?.let { date ->
                            displayDateFormat.format(date)
                        } ?: it
                    } catch (e: Exception) {
                        it
                    })
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Đóng")
                    }
                    Button(
                        onClick = {
                            onDismiss()
                            onUpdateStatus()
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                    ) {
                        Text("Cập nhật trạng thái", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = TextPrimary,
            modifier = Modifier.weight(1f),
            textAlign = androidx.compose.ui.text.style.TextAlign.End
        )
    }
}

@Composable
fun StatusUpdateDialog(
    booking: AirlineBooking,
    onStatusChange: (String) -> Unit,
    onDismiss: () -> Unit,
    updating: Boolean
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Cập nhật trạng thái") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Mã đặt vé: ${booking.ma_dat_ve}")
                Text("Trạng thái hiện tại: ${when (booking.trang_thai) {
                    "giu_cho" -> "Giữ chỗ"
                    "da_thanh_toan" -> "Đã thanh toán"
                    "da_huy" -> "Đã hủy"
                    else -> booking.trang_thai
                }}")
                Spacer(modifier = Modifier.height(8.dp))
                Text("Chọn trạng thái mới:", fontWeight = FontWeight.Bold)
                val statusOptions = mapOf(
                    "giu_cho" to "Giữ chỗ",
                    "da_thanh_toan" to "Đã thanh toán",
                    "da_huy" to "Đã hủy"
                )
                statusOptions.forEach { (key, label) ->
                    if (key != booking.trang_thai) {
                        Button(
                            onClick = { onStatusChange(key) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                            enabled = !updating
                        ) {
                            Text(label, color = Color.White)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss, enabled = !updating) {
                Text("Hủy")
            }
        }
    )
}

@Composable
fun BookingErrorView(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = "Error",
            tint = ErrorRed,
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Không thể tải dữ liệu",
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            modifier = Modifier.padding(top = 4.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry, colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)) {
            Text("Thử lại", color = Color.White)
        }
    }
}

@Composable
fun EmptyBookingView(hasSearch: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.ShoppingCart,
            contentDescription = "Empty",
            modifier = Modifier.size(64.dp),
            tint = TextSecondary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = if (hasSearch) "Không tìm thấy đặt vé nào" else "Chưa có đặt vé nào",
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary
        )
        Text(
            text = if (hasSearch) "Thử tìm kiếm với từ khóa hoặc bộ lọc khác" else "Đặt vé sẽ hiển thị ở đây khi có khách hàng đặt",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            modifier = Modifier.padding(top = 4.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

