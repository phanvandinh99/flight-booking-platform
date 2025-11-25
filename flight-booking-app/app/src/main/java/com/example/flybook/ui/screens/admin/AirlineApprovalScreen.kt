package com.example.flybook.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.flybook.data.models.Airline
import com.example.flybook.data.repository.AdminRepository
import com.example.flybook.navigation.Screen
import com.example.flybook.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AirlineApprovalScreen(navController: NavController) {
    val adminRepository = remember { AdminRepository() }
    val scope = rememberCoroutineScope()
    
    var airlines by remember { mutableStateOf<List<Airline>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var actionLoading by remember { mutableStateOf<Map<Int, Boolean>>(emptyMap()) }
    
    fun loadAirlines() {
        scope.launch {
            isLoading = true
            errorMessage = null
            adminRepository.getPendingAirlines()
                .onSuccess {
                    airlines = it
                    errorMessage = null
                }
                .onFailure { e ->
                    errorMessage = e.message ?: "Không thể tải danh sách hãng hàng không"
                }
            isLoading = false
        }
    }
    
    LaunchedEffect(Unit) {
        loadAirlines()
    }
    
    fun handleAction(airlineId: Int, action: String) {
        scope.launch {
            actionLoading = actionLoading + (airlineId to true)
            val result = when (action) {
                "approve" -> adminRepository.approveAirline(airlineId)
                "reject" -> adminRepository.rejectAirline(airlineId)
                "activate" -> adminRepository.activateAirline(airlineId)
                "suspend" -> adminRepository.suspendAirline(airlineId)
                else -> return@launch
            }
            
            result.onSuccess {
                loadAirlines()
            }.onFailure { e ->
                errorMessage = e.message ?: "Thao tác thất bại"
            }
            actionLoading = actionLoading - airlineId
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Phê duyệt Hãng Hàng Không") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { loadAirlines() },
                        enabled = !isLoading
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = if (isLoading) Color.Gray else Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryBlue,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(BackgroundLight)
        ) {
            when {
                isLoading && airlines.isEmpty() -> {
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
                                text = "Đang tải danh sách hãng hàng không...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        }
                    }
                }
                
                errorMessage != null -> {
                    ErrorView(
                        message = errorMessage!!,
                        onRetry = { loadAirlines() }
                    )
                }
                
                airlines.isEmpty() -> {
                    EmptyView()
                }
                
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Text(
                                text = "Xem và phê duyệt các hãng hàng không đăng ký mới",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                        
                        items(airlines) { airline ->
                            AirlineCard(
                                airline = airline,
                                actionLoading = actionLoading[airline.id] ?: false,
                                onAction = { action -> handleAction(airline.id, action) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AirlineCard(
    airline: Airline,
    actionLoading: Boolean,
    onAction: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Logo/Avatar
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(PrimaryBlue, PrimaryPurple)
                                ),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = airline.ten_hang.firstOrNull()?.toString() ?: "A",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            ),
                            color = Color.White
                        )
                    }
                    
                    Column {
                        Text(
                            text = airline.ten_hang,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                        Text(
                            text = airline.ma_hang ?: "N/A",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
                
                // Status badge
                StatusBadge(status = airline.trang_thai)
            }
            
            // Description
            airline.mo_ta?.let { description ->
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            // Meta info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                airline.quoc_gia?.let { country ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Country",
                            modifier = Modifier.size(16.dp),
                            tint = TextSecondary
                        )
                        Text(
                            text = country,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            }
            
            Divider()
            
            // Action buttons
            ActionButtons(
                status = airline.trang_thai,
                isLoading = actionLoading,
                onAction = onAction
            )
        }
    }
}

@Composable
fun StatusBadge(status: String?) {
    val (label, color) = when (status) {
        "cho_duyet", null -> "Chờ duyệt" to Color(0xFFFFA726)
        "hoat_dong" -> "Hoạt động" to Color(0xFF66BB6A)
        "dinh_chi" -> "Đình chỉ" to Color(0xFFEF5350)
        "tu_choi" -> "Từ chối" to Color(0xFF78909C)
        else -> status to TextSecondary
    }
    
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.1f)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.SemiBold,
                color = color
            )
        )
    }
}

@Composable
fun ActionButtons(
    status: String?,
    isLoading: Boolean,
    onAction: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        when (status) {
            "cho_duyet", null -> {
                ActionButton(
                    text = "Phê duyệt",
                    icon = Icons.Default.CheckCircle,
                    onClick = { onAction("approve") },
                    enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF66BB6A)
                    ),
                    modifier = Modifier.weight(1f)
                )
                ActionButton(
                    text = "Từ chối",
                    icon = Icons.Default.Close,
                    onClick = { onAction("reject") },
                    enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFEF5350)
                    ),
                    modifier = Modifier.weight(1f)
                )
            }
            "hoat_dong" -> {
                ActionButton(
                    text = "Đình chỉ",
                    icon = Icons.Default.Delete,
                    onClick = { onAction("suspend") },
                    enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFA726)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            "dinh_chi" -> {
                ActionButton(
                    text = "Kích hoạt",
                    icon = Icons.Default.CheckCircle,
                    onClick = { onAction("activate") },
                    enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF66BB6A)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            "tu_choi" -> {
                ActionButton(
                    text = "Phê duyệt lại",
                    icon = Icons.Default.CheckCircle,
                    onClick = { onAction("approve") },
                    enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF66BB6A)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun ActionButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    enabled: Boolean,
    colors: ButtonColors,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        colors = colors,
        modifier = modifier.height(40.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        if (!enabled) {
            CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                color = Color.White,
                strokeWidth = 2.dp
            )
        } else {
            Icon(
                imageVector = icon,
                contentDescription = text,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium
        )
    }
}

@Composable
fun ErrorView(message: String, onRetry: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "Error",
                modifier = Modifier.size(48.dp),
                tint = ErrorRed
            )
            Text(
                text = "Không thể tải dữ liệu",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
            Button(onClick = onRetry) {
                Text("Thử lại")
            }
        }
    }
}

@Composable
fun EmptyView() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Text(
                text = "✈️",
                fontSize = 48.sp
            )
            Text(
                text = "Không có hãng hàng không nào",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "Hiện tại không có hãng hàng không nào cần phê duyệt",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        }
    }
}

