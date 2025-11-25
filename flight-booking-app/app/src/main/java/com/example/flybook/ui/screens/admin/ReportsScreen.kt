package com.example.flybook.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.flybook.data.models.MonthlyRevenue
import com.example.flybook.data.models.RevenueSummary
import com.example.flybook.data.models.TopAirline
import com.example.flybook.data.repository.ReportsRepository
import com.example.flybook.ui.components.BottomNavigationBar
import com.example.flybook.ui.components.adminBottomNavItems
import com.example.flybook.ui.theme.*
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(navController: NavController) {
    val repository = remember { ReportsRepository() }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var revenueSummary by remember { mutableStateOf<RevenueSummary?>(null) }
    var monthlyRevenue by remember { mutableStateOf<List<MonthlyRevenue>>(emptyList()) }
    var topAirlines by remember { mutableStateOf<List<TopAirline>>(emptyList()) }
    
    val calendar = Calendar.getInstance()
    val startOfYear = Calendar.getInstance().apply {
        set(Calendar.MONTH, Calendar.JANUARY)
        set(Calendar.DAY_OF_MONTH, 1)
    }
    
    var tuNgay by remember { 
        mutableStateOf(
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(startOfYear.time)
        )
    }
    var denNgay by remember { 
        mutableStateOf(
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
        )
    }
    
    fun loadReports() {
        loading = true
        error = null
    }
    
    LaunchedEffect(tuNgay, denNgay) {
        loading = true
        error = null
        
        val summaryResult = repository.getRevenueSummary()
        val monthlyResult = repository.getMonthlyRevenue(tuNgay, denNgay)
        val topAirlinesResult = repository.getTopAirlines(10)
        
        summaryResult.onSuccess { revenueSummary = it }
            .onFailure { error = it.message }
        
        monthlyResult.onSuccess { monthlyRevenue = it }
            .onFailure { if (error == null) error = it.message }
        
        topAirlinesResult.onSuccess { topAirlines = it }
            .onFailure { if (error == null) error = it.message }
        
        loading = false
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Báo Cáo Tổng Hợp") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
                items = adminBottomNavItems
            )
        }
    ) { paddingValues ->
        if (loading && revenueSummary == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CircularProgressIndicator(color = PrimaryBlue)
                    Text("Đang tải báo cáo...", color = TextSecondary)
                }
            }
        } else if (error != null && revenueSummary == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(16.dp)
                ) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = "Error",
                        modifier = Modifier.size(48.dp),
                        tint = ErrorRed
                    )
                    Text("Không thể tải dữ liệu", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text(error ?: "Lỗi không xác định", color = TextSecondary)
                    Button(
                        onClick = { loadReports() },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                    ) {
                        Text("Thử lại")
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(BackgroundLight)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Date Range Filter
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = BackgroundCard)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Khoảng thời gian", fontWeight = FontWeight.Bold)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Từ ngày", fontSize = 12.sp, color = TextSecondary)
                                OutlinedTextField(
                                    value = tuNgay,
                                    onValueChange = { tuNgay = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    shape = RoundedCornerShape(8.dp),
                                    placeholder = { Text("YYYY-MM-DD") }
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Đến ngày", fontSize = 12.sp, color = TextSecondary)
                                OutlinedTextField(
                                    value = denNgay,
                                    onValueChange = { denNgay = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    shape = RoundedCornerShape(8.dp),
                                    placeholder = { Text("YYYY-MM-DD") }
                                )
                            }
                            IconButton(
                                onClick = { loadReports() },
                                modifier = Modifier
                                    .background(
                                        brush = Brush.linearGradient(
                                            colors = listOf(PrimaryBlue, PrimaryPurple)
                                        ),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                            ) {
                                Icon(
                                    Icons.Default.Refresh,
                                    contentDescription = "Refresh",
                                    tint = Color.White
                                )
                            }
                        }
                    }
                }
                
                // Summary Cards
                revenueSummary?.let { summary ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        SummaryCard(
                            title = "Tổng Doanh Thu",
                            value = formatCurrency(summary.tong_doanh_thu),
                            subtitle = "Từ ${summary.tong_dat_ve_da_thanh_toan} đặt vé đã thanh toán",
                            icon = Icons.Default.ShoppingCart,
                            gradient = listOf(Color(0xFF667eea), Color(0xFF764ba2)),
                            modifier = Modifier.weight(1f)
                        )
                        SummaryCard(
                            title = "Tổng Đặt Vé",
                            value = formatNumber(summary.tong_dat_ve),
                            subtitle = "${summary.tong_dat_ve_da_thanh_toan} đã thanh toán",
                            icon = Icons.Default.List,
                            gradient = listOf(Color(0xFFf093fb), Color(0xFFf5576c)),
                            modifier = Modifier.weight(1f)
                        )
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        SummaryCard(
                            title = "Doanh Thu TB",
                            value = formatCurrency(summary.doanh_thu_trung_binh),
                            subtitle = "Trên mỗi đặt vé",
                            icon = Icons.Default.Star,
                            gradient = listOf(Color(0xFF43e97b), Color(0xFF38f9d7)),
                            modifier = Modifier.weight(1f)
                        )
                        SummaryCard(
                            title = "Tỷ Lệ Thanh Toán",
                            value = if (summary.tong_dat_ve > 0) {
                                String.format(
                                    "%.1f%%",
                                    (summary.tong_dat_ve_da_thanh_toan.toDouble() / summary.tong_dat_ve) * 100
                                )
                            } else "0%",
                            subtitle = "${summary.tong_dat_ve_da_thanh_toan} / ${summary.tong_dat_ve} đặt vé",
                            icon = Icons.Default.CheckCircle,
                            gradient = listOf(Color(0xFFfa709a), Color(0xFFfee140)),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                
                // Monthly Revenue Section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = BackgroundCard)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Doanh Thu Theo Tháng", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        if (monthlyRevenue.isEmpty()) {
                            Text(
                                "Không có dữ liệu trong khoảng thời gian này",
                                color = TextSecondary,
                                modifier = Modifier.padding(vertical = 16.dp)
                            )
                        } else {
                            MonthlyRevenueTable(monthlyRevenue)
                        }
                    }
                }
                
                // Top Airlines Section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = BackgroundCard)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Top Hãng Hàng Không", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("Top 10 hãng có doanh thu cao nhất", fontSize = 12.sp, color = TextSecondary)
                        if (topAirlines.isEmpty()) {
                            Text("Không có dữ liệu", color = TextSecondary, modifier = Modifier.padding(vertical = 16.dp))
                        } else {
                            TopAirlinesTable(topAirlines)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SummaryCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    gradient: List<Color>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = BackgroundCard)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        brush = Brush.linearGradient(gradient),
                        shape = RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
            }
            Text(title, fontSize = 12.sp, color = TextSecondary)
            Text(value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Text(subtitle, fontSize = 10.sp, color = TextSecondary)
        }
    }
}

@Composable
fun MonthlyRevenueTable(data: List<MonthlyRevenue>) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(PrimaryBlue.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Tháng", fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.weight(2f))
            Text("Số Đơn", fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.weight(1f))
            Text("Doanh Thu", fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.weight(1.5f))
            Text("TB/Đơn", fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.weight(1.5f))
        }
        
        // Rows
        data.forEach { item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BackgroundLight, RoundedCornerShape(8.dp))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    formatMonth(item.month),
                    fontSize = 12.sp,
                    modifier = Modifier.weight(2f)
                )
                Text(
                    formatNumber(item.orders),
                    fontSize = 12.sp,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    formatCurrency(item.revenue),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = PrimaryBlue,
                    modifier = Modifier.weight(1.5f)
                )
                Text(
                    formatCurrency(if (item.orders > 0) item.revenue / item.orders else 0.0),
                    fontSize = 12.sp,
                    modifier = Modifier.weight(1.5f)
                )
            }
        }
    }
}

@Composable
fun TopAirlinesTable(data: List<TopAirline>) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(PrimaryBlue.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Hạng", fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.width(40.dp))
            Text("Mã", fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.weight(1f))
            Text("Tên Hãng", fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.weight(2f))
            Text("Số ĐV", fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.weight(1f))
            Text("Doanh Thu", fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.weight(1.5f))
        }
        
        // Rows
        data.forEachIndexed { index, airline ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BackgroundLight, RoundedCornerShape(8.dp))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .background(
                            when (index) {
                                0 -> Color(0xFFFFD700) // Gold
                                1 -> Color(0xFFC0C0C0) // Silver
                                2 -> Color(0xFFCD7F32) // Bronze
                                else -> PrimaryBlue.copy(alpha = 0.2f)
                            },
                            RoundedCornerShape(4.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "${index + 1}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (index < 3) Color.White else TextPrimary
                    )
                }
                Text(
                    airline.ma_hang,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    airline.ten_hang,
                    fontSize = 12.sp,
                    modifier = Modifier.weight(2f)
                )
                Text(
                    formatNumber(airline.so_dat_ve),
                    fontSize = 12.sp,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    formatCurrency(airline.tong_doanh_thu),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = PrimaryBlue,
                    modifier = Modifier.weight(1.5f)
                )
            }
        }
    }
}

fun formatCurrency(amount: Double): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
    return formatter.format(amount)
}

fun formatNumber(num: Int): String {
    val formatter = NumberFormat.getNumberInstance(Locale("vi", "VN"))
    return formatter.format(num)
}

fun formatMonth(monthString: String): String {
    return try {
        val parts = monthString.split("-")
        val year = parts[0].toInt()
        val month = parts[1].toInt()
        val monthNames = arrayOf(
            "Tháng 1", "Tháng 2", "Tháng 3", "Tháng 4", "Tháng 5", "Tháng 6",
            "Tháng 7", "Tháng 8", "Tháng 9", "Tháng 10", "Tháng 11", "Tháng 12"
        )
        "${monthNames[month - 1]} $year"
    } catch (e: Exception) {
        monthString
    }
}

