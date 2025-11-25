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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.flybook.data.models.*
import com.example.flybook.data.repository.AirlineReportsRepository
import com.example.flybook.ui.components.BottomNavigationBar
import com.example.flybook.ui.components.airlineBottomNavItems
import com.example.flybook.ui.components.DateTimePicker
import com.example.flybook.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

fun formatCurrency(amount: Double): String {
    return String.format(Locale("vi", "VN"), "%,.0f VND", amount)
}

fun formatNumber(num: Int): String {
    return String.format(Locale("vi", "VN"), "%,d", num)
}

fun formatDate(dateString: String): String {
    return try {
        val date = LocalDate.parse(dateString)
        date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
    } catch (e: Exception) {
        dateString
    }
}

fun getHangVeLabel(hangVe: String): String {
    return when (hangVe) {
        "pho_thong" -> "Phổ thông"
        "thuong_gia" -> "Thương gia"
        "hang_nhat" -> "Hạng nhất"
        else -> hangVe
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AirlineReportsScreen(navController: NavController) {
    val repository = remember { AirlineReportsRepository() }
    val scope = rememberCoroutineScope()
    
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var activeTab by remember { mutableStateOf("overview") }
    
    val calendar = Calendar.getInstance()
    val startOfMonth = Calendar.getInstance().apply {
        set(Calendar.DAY_OF_MONTH, 1)
    }
    
    var tuNgay by remember {
        mutableStateOf(
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(startOfMonth.time)
        )
    }
    var denNgay by remember {
        mutableStateOf(
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
        )
    }
    
    // Data states
    var overview by remember { mutableStateOf<OverviewReport?>(null) }
    var dailyRevenue by remember { mutableStateOf<List<DailyRevenue>>(emptyList()) }
    var weeklyRevenue by remember { mutableStateOf<List<WeeklyRevenue>>(emptyList()) }
    var monthlyRevenue by remember { mutableStateOf<List<AirlineMonthlyRevenue>>(emptyList()) }
    var flightReport by remember { mutableStateOf<List<FlightReport>>(emptyList()) }
    var fareClassReport by remember { mutableStateOf<List<FareClassReport>>(emptyList()) }
    
    fun loadReports() {
        scope.launch {
            loading = true
            error = null
            
            try {
                when (activeTab) {
                    "overview" -> {
                        repository.getOverviewReport(tuNgay, denNgay)
                            .onSuccess { overview = it }
                            .onFailure { error = it.message ?: "Không thể tải báo cáo tổng quan" }
                    }
                    "daily" -> {
                        repository.getDailyRevenue(tuNgay, denNgay)
                            .onSuccess { dailyRevenue = it }
                            .onFailure { error = it.message ?: "Không thể tải báo cáo theo ngày" }
                    }
                    "weekly" -> {
                        repository.getWeeklyRevenue(tuNgay, denNgay)
                            .onSuccess { weeklyRevenue = it }
                            .onFailure { error = it.message ?: "Không thể tải báo cáo theo tuần" }
                    }
                    "monthly" -> {
                        repository.getMonthlyRevenue(tuNgay, denNgay)
                            .onSuccess { monthlyRevenue = it }
                            .onFailure { error = it.message ?: "Không thể tải báo cáo theo tháng" }
                    }
                    "flights" -> {
                        repository.getFlightReport(tuNgay, denNgay)
                            .onSuccess { flightReport = it }
                            .onFailure { error = it.message ?: "Không thể tải báo cáo theo chuyến bay" }
                    }
                    "fare-class" -> {
                        repository.getFareClassReport(tuNgay, denNgay)
                            .onSuccess { fareClassReport = it }
                            .onFailure { error = it.message ?: "Không thể tải báo cáo theo hạng vé" }
                    }
                }
            } catch (e: Exception) {
                error = e.message ?: "Không thể tải báo cáo"
            } finally {
                loading = false
            }
        }
    }
    
    LaunchedEffect(activeTab, tuNgay, denNgay) {
        loadReports()
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Báo Cáo & Thống Kê") },
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
                    IconButton(onClick = { loadReports() }) {
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(BackgroundLight)
        ) {
            // Date Range Filter
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Từ ngày:",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        DateTimePicker(
                            label = "",
                            value = tuNgay,
                            onValueChange = { tuNgay = it },
                            supportingText = "YYYY-MM-DD"
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Đến ngày:",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        DateTimePicker(
                            label = "",
                            value = denNgay,
                            onValueChange = { denNgay = it },
                            supportingText = "YYYY-MM-DD"
                        )
                    }
                }
            }
            
            // Tabs
            val selectedTabIndex = when (activeTab) {
                "overview" -> 0
                "daily" -> 1
                "weekly" -> 2
                "monthly" -> 3
                "flights" -> 4
                "fare-class" -> 5
                else -> 0
            }
            ScrollableTabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = Color.White,
                contentColor = PrimaryBlue
            ) {
                Tab(
                    selected = activeTab == "overview",
                    onClick = { activeTab = "overview" },
                    text = { Text("Tổng quan") }
                )
                Tab(
                    selected = activeTab == "daily",
                    onClick = { activeTab = "daily" },
                    text = { Text("Theo ngày") }
                )
                Tab(
                    selected = activeTab == "weekly",
                    onClick = { activeTab = "weekly" },
                    text = { Text("Theo tuần") }
                )
                Tab(
                    selected = activeTab == "monthly",
                    onClick = { activeTab = "monthly" },
                    text = { Text("Theo tháng") }
                )
                Tab(
                    selected = activeTab == "flights",
                    onClick = { activeTab = "flights" },
                    text = { Text("Theo chuyến bay") }
                )
                Tab(
                    selected = activeTab == "fare-class",
                    onClick = { activeTab = "fare-class" },
                    text = { Text("Theo hạng vé") }
                )
            }
            
            // Content
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(BackgroundLight)
            ) {
                when {
                    loading -> {
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
                                    text = "Đang tải báo cáo...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                    error != null -> {
                        ErrorView(
                            message = error!!,
                            onRetry = { loadReports() }
                        )
                    }
                    else -> {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            when (activeTab) {
                                "overview" -> {
                                    if (overview != null) {
                                        item {
                                            OverviewContent(overview = overview!!)
                                        }
                                    } else {
                                        item {
                                            EmptyView(message = "Không có dữ liệu")
                                        }
                                    }
                                }
                                "daily" -> {
                                    if (dailyRevenue.isEmpty()) {
                                        item {
                                            EmptyView(message = "Không có dữ liệu trong khoảng thời gian này")
                                        }
                                    } else {
                                        items(dailyRevenue) { item ->
                                            DailyRevenueCard(item = item)
                                        }
                                    }
                                }
                                "weekly" -> {
                                    if (weeklyRevenue.isEmpty()) {
                                        item {
                                            EmptyView(message = "Không có dữ liệu trong khoảng thời gian này")
                                        }
                                    } else {
                                        items(weeklyRevenue) { item ->
                                            WeeklyRevenueCard(item = item)
                                        }
                                    }
                                }
                                "monthly" -> {
                                    if (monthlyRevenue.isEmpty()) {
                                        item {
                                            EmptyView(message = "Không có dữ liệu trong khoảng thời gian này")
                                        }
                                    } else {
                                        items(monthlyRevenue) { item ->
                                            MonthlyRevenueCard(item = item)
                                        }
                                    }
                                }
                                "flights" -> {
                                    if (flightReport.isEmpty()) {
                                        item {
                                            EmptyView(message = "Không có dữ liệu trong khoảng thời gian này")
                                        }
                                    } else {
                                        items(flightReport) { item ->
                                            FlightReportCard(item = item)
                                        }
                                    }
                                }
                                "fare-class" -> {
                                    if (fareClassReport.isEmpty()) {
                                        item {
                                            EmptyView(message = "Không có dữ liệu trong khoảng thời gian này")
                                        }
                                    } else {
                                        items(fareClassReport) { item ->
                                            FareClassReportCard(item = item)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OverviewContent(overview: OverviewReport) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OverviewCard(
            title = "Tổng số chuyến bay",
            value = formatNumber(overview.tong_so_chuyen_bay),
            icon = Icons.Default.LocationOn,
            gradientColors = listOf(PrimaryBlue, PrimaryPurple)
        )
        OverviewCard(
            title = "Tổng số đặt vé",
            value = formatNumber(overview.tong_so_dat_ve),
            icon = Icons.Default.ShoppingCart,
            gradientColors = listOf(Color(0xFF4FACFE), Color(0xFF00F2FE))
        )
        OverviewCard(
            title = "Tổng doanh thu",
            value = formatCurrency(overview.tong_doanh_thu),
            icon = Icons.Default.Info,
            gradientColors = listOf(Color(0xFF43E97B), Color(0xFF38F9D7))
        )
        OverviewCard(
            title = "Doanh thu trung bình",
            value = formatCurrency(overview.doanh_thu_trung_binh),
            icon = Icons.Default.List,
            gradientColors = listOf(Color(0xFFF093FB), Color(0xFFF5576C))
        )
        if (overview.ty_le_thanh_cong != null) {
            OverviewCard(
                title = "Tỷ lệ thành công",
                value = "${String.format(Locale.getDefault(), "%.1f", overview.ty_le_thanh_cong)}%",
                icon = Icons.Default.Star,
                gradientColors = listOf(Color(0xFFFA709A), Color(0xFFFEE140))
            )
        }
    }
}

@Composable
fun OverviewCard(
    title: String,
    value: String,
    icon: ImageVector,
    gradientColors: List<Color>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
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
                        brush = Brush.linearGradient(gradientColors),
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
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
            }
        }
    }
}

@Composable
fun DailyRevenueCard(item: DailyRevenue) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                    text = formatDate(item.ngay),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${formatNumber(item.so_dat_ve)} đặt vé",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
            Text(
                text = formatCurrency(item.doanh_thu),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = PrimaryBlue
            )
        }
    }
}

@Composable
fun WeeklyRevenueCard(item: WeeklyRevenue) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                    text = if (item.tuan != null && item.nam != null) {
                        "Tuần ${item.tuan}, ${item.nam}"
                    } else if (item.tuan != null) {
                        "Tuần ${item.tuan}"
                    } else {
                        "Tuần"
                    },
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${formatNumber(item.so_dat_ve)} đặt vé",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
            Text(
                text = formatCurrency(item.doanh_thu),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = PrimaryBlue
            )
        }
    }
}

@Composable
fun MonthlyRevenueCard(item: AirlineMonthlyRevenue) {
    val monthLabel = remember(item) {
        when {
            item.thang != null && item.thang.contains("-") -> {
                try {
                    val date = LocalDate.parse("${item.thang}-01")
                    date.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale("vi", "VN")))
                } catch (e: Exception) {
                    item.thang ?: "N/A"
                }
            }
            item.thang != null && item.nam != null -> {
                try {
                    val date = LocalDate.of(item.nam, item.thang.toIntOrNull() ?: 1, 1)
                    date.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale("vi", "VN")))
                } catch (e: Exception) {
                    "${item.thang}/${item.nam}"
                }
            }
            else -> item.thang ?: "N/A"
        }
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                    text = monthLabel,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${formatNumber(item.so_dat_ve)} đặt vé",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
            Text(
                text = formatCurrency(item.doanh_thu),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = PrimaryBlue
            )
        }
    }
}

@Composable
fun FlightReportCard(item: FlightReport) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
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
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.ma_chuyen_bay ?: "N/A",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = PrimaryBlue
                )
                Text(
                    text = formatCurrency(item.tong_doanh_thu),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = PrimaryBlue
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.LocationOn,
                    contentDescription = "Route",
                    tint = TextSecondary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${item.tuyen_bay?.san_bay_di?.ma_san_bay ?: "N/A"} → ${item.tuyen_bay?.san_bay_den?.ma_san_bay ?: "N/A"}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary
                )
            }
            Text(
                text = "${formatNumber(item.so_dat_ve)} đặt vé",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
    }
}

@Composable
fun FareClassReportCard(item: FareClassReport) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Badge(
                    containerColor = when (item.hang_ve) {
                        "pho_thong" -> PrimaryBlue
                        "thuong_gia" -> WarningYellow
                        "hang_nhat" -> SuccessGreen
                        else -> TextSecondary
                    },
                    contentColor = Color.White
                ) {
                    Text(
                        text = getHangVeLabel(item.hang_ve),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${formatNumber(item.so_dat_ve)} đặt vé",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = formatCurrency(item.doanh_thu),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = PrimaryBlue
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "TB: ${formatCurrency(item.gia_trung_binh)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
fun ErrorView(message: String, onRetry: () -> Unit) {
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
            text = "Không thể tải báo cáo",
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            modifier = Modifier.padding(top = 4.dp),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
        ) {
            Text("Thử lại", color = Color.White)
        }
    }
}

@Composable
fun EmptyView(message: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Info,
            contentDescription = "Empty",
            modifier = Modifier.size(64.dp),
            tint = TextSecondary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary,
            textAlign = TextAlign.Center
        )
    }
}

