package com.example.flybook.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.example.flybook.data.models.Airport
import com.example.flybook.data.models.Route
import com.example.flybook.data.repository.AirportRepository
import com.example.flybook.data.repository.RouteRepository
import com.example.flybook.ui.components.BottomNavigationBar
import com.example.flybook.ui.components.adminBottomNavItems
import com.example.flybook.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RouteManagementScreen(navController: NavController) {
    val routeRepository = remember { RouteRepository() }
    val airportRepository = remember { AirportRepository() }
    val scope = rememberCoroutineScope()
    
    var routes by remember { mutableStateOf<List<Route>>(emptyList()) }
    var airports by remember { mutableStateOf<List<Airport>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showDialog by remember { mutableStateOf(false) }
    var editingRoute by remember { mutableStateOf<Route?>(null) }
    var searchTerm by remember { mutableStateOf("") }
    
    // Form state
    var sanBayDiId by remember { mutableStateOf<Int?>(null) }
    var sanBayDenId by remember { mutableStateOf<Int?>(null) }
    var duocPheDuyet by remember { mutableStateOf(false) }
    var formErrors by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var isSubmitting by remember { mutableStateOf(false) }
    
    fun loadData() {
        scope.launch {
            isLoading = true
            errorMessage = null
            
            // Load airports first
            airportRepository.getAirports()
                .onSuccess { airports = it }
                .onFailure { e ->
                    errorMessage = e.message ?: "Không thể tải danh sách sân bay"
                }
            
            // Load routes
            routeRepository.getRoutes()
                .onSuccess {
                    routes = it
                    errorMessage = null
                }
                .onFailure { e ->
                    errorMessage = e.message ?: "Không thể tải danh sách tuyến bay"
                }
            
            isLoading = false
        }
    }
    
    LaunchedEffect(Unit) {
        loadData()
    }
    
    fun openDialog(route: Route? = null) {
        editingRoute = route
        if (route != null) {
            sanBayDiId = route.san_bay_di?.id
            sanBayDenId = route.san_bay_den?.id
            duocPheDuyet = route.duoc_phe_duyet ?: false
        } else {
            sanBayDiId = null
            sanBayDenId = null
            duocPheDuyet = false
        }
        formErrors = emptyMap()
        showDialog = true
    }
    
    fun closeDialog() {
        showDialog = false
        editingRoute = null
        sanBayDiId = null
        sanBayDenId = null
        duocPheDuyet = false
        formErrors = emptyMap()
    }
    
    fun validateForm(): Boolean {
        val errors = mutableMapOf<String, String>()
        if (sanBayDiId == null) errors["sanBayDi"] = "Vui lòng chọn sân bay đi"
        if (sanBayDenId == null) errors["sanBayDen"] = "Vui lòng chọn sân bay đến"
        if (sanBayDiId == sanBayDenId) errors["general"] = "Sân bay đi và sân bay đến không được trùng nhau"
        formErrors = errors
        return errors.isEmpty()
    }
    
    fun handleSubmit() {
        if (!validateForm()) return
        
        scope.launch {
            isSubmitting = true
            val result = if (editingRoute != null) {
                routeRepository.updateRoute(
                    id = editingRoute!!.id,
                    sanBayDi = sanBayDiId,
                    sanBayDen = sanBayDenId,
                    duocPheDuyet = duocPheDuyet
                )
            } else {
                routeRepository.createRoute(
                    sanBayDi = sanBayDiId!!,
                    sanBayDen = sanBayDenId!!,
                    duocPheDuyet = duocPheDuyet
                )
            }
            
            result.onSuccess {
                loadData()
                closeDialog()
            }.onFailure { e ->
                formErrors = mapOf("general" to (e.message ?: "Có lỗi xảy ra"))
            }
            isSubmitting = false
        }
    }
    
    fun handleDelete(route: Route) {
        scope.launch {
            routeRepository.deleteRoute(route.id)
                .onSuccess {
                    loadData()
                }
                .onFailure { e ->
                    errorMessage = e.message ?: "Không thể xóa tuyến bay"
                }
        }
    }
    
    fun handleApprove(route: Route) {
        scope.launch {
            routeRepository.approveRoute(route.id)
                .onSuccess {
                    loadData()
                }
                .onFailure { e ->
                    errorMessage = e.message ?: "Không thể phê duyệt tuyến bay"
                }
        }
    }
    
    fun handleRevoke(route: Route) {
        scope.launch {
            routeRepository.revokeRoute(route.id)
                .onSuccess {
                    loadData()
                }
                .onFailure { e ->
                    errorMessage = e.message ?: "Không thể thu hồi phê duyệt"
                }
        }
    }
    
    val filteredRoutes = routes.filter {
        searchTerm.isBlank() ||
        it.san_bay_di?.ten_san_bay?.contains(searchTerm, ignoreCase = true) == true ||
        it.san_bay_di?.ma_san_bay?.contains(searchTerm, ignoreCase = true) == true ||
        it.san_bay_den?.ten_san_bay?.contains(searchTerm, ignoreCase = true) == true ||
        it.san_bay_den?.ma_san_bay?.contains(searchTerm, ignoreCase = true) == true
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Quản lý Tuyến Bay") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { loadData() },
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
        },
        bottomBar = {
            BottomNavigationBar(
                navController = navController,
                items = adminBottomNavItems
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { openDialog() },
                containerColor = PrimaryBlue
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Thêm tuyến bay",
                    tint = Color.White
                )
            }
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
                // Search bar
                OutlinedTextField(
                    value = searchTerm,
                    onValueChange = { searchTerm = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Tìm kiếm tuyến bay...") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    },
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
                
                // Content
                when {
                    isLoading && routes.isEmpty() -> {
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
                                    text = "Đang tải danh sách tuyến bay...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                    
                    errorMessage != null -> {
                        RouteErrorView(
                            message = errorMessage!!,
                            onRetry = { loadData() }
                        )
                    }
                    
                    filteredRoutes.isEmpty() -> {
                        RouteEmptyView(
                            hasSearch = searchTerm.isNotEmpty(),
                            onAddClick = { openDialog() }
                        )
                    }
                    
                    else -> {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(filteredRoutes) { route ->
                                RouteCard(
                                    route = route,
                                    onEdit = { openDialog(route) },
                                    onDelete = { handleDelete(route) },
                                    onApprove = { handleApprove(route) },
                                    onRevoke = { handleRevoke(route) }
                                )
                            }
                        }
                    }
                }
            }
        }
        
        // Dialog for Add/Edit
        if (showDialog) {
            RouteDialog(
                route = editingRoute,
                airports = airports,
                sanBayDiId = sanBayDiId,
                sanBayDenId = sanBayDenId,
                duocPheDuyet = duocPheDuyet,
                onSanBayDiChange = { sanBayDiId = it },
                onSanBayDenChange = { sanBayDenId = it },
                onDuocPheDuyetChange = { duocPheDuyet = it },
                formErrors = formErrors,
                isSubmitting = isSubmitting,
                onDismiss = { closeDialog() },
                onSubmit = { handleSubmit() }
            )
        }
    }
}

@Composable
fun RouteCard(
    route: Route,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onApprove: () -> Unit,
    onRevoke: () -> Unit
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
            // Route info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // From airport
                    Column {
                        Text(
                            text = route.san_bay_di?.ma_san_bay ?: "N/A",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = PrimaryBlue
                        )
                        Text(
                            text = route.san_bay_di?.ten_san_bay ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    
                    // Arrow
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "To",
                        tint = PrimaryBlue,
                        modifier = Modifier.size(24.dp)
                    )
                    
                    // To airport
                    Column {
                        Text(
                            text = route.san_bay_den?.ma_san_bay ?: "N/A",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = PrimaryBlue
                        )
                        Text(
                            text = route.san_bay_den?.ten_san_bay ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                
                // Status badge
                StatusBadge(approved = route.duoc_phe_duyet ?: false)
            }
            
            Divider()
            
            // Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (route.duoc_phe_duyet == true) {
                    Button(
                        onClick = onRevoke,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFFA726)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Revoke",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Thu hồi")
                    }
                } else {
                    Button(
                        onClick = onApprove,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF66BB6A)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Approve",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Phê duyệt")
                    }
                }
                
                IconButton(onClick = onEdit) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = PrimaryBlue
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = ErrorRed
                    )
                }
            }
        }
    }
}

@Composable
fun StatusBadge(approved: Boolean) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (approved) Color(0xFF66BB6A).copy(alpha = 0.1f) else Color(0xFFFFA726).copy(alpha = 0.1f)
    ) {
        Text(
            text = if (approved) "Đã phê duyệt" else "Chờ phê duyệt",
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.SemiBold,
                color = if (approved) Color(0xFF66BB6A) else Color(0xFFFFA726)
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RouteDialog(
    route: Route?,
    airports: List<Airport>,
    sanBayDiId: Int?,
    sanBayDenId: Int?,
    duocPheDuyet: Boolean,
    onSanBayDiChange: (Int?) -> Unit,
    onSanBayDenChange: (Int?) -> Unit,
    onDuocPheDuyetChange: (Boolean) -> Unit,
    formErrors: Map<String, String>,
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    onSubmit: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = if (route != null) "Sửa tuyến bay" else "Thêm tuyến bay mới",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
                
                // Airport from dropdown
                var expandedFrom by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expandedFrom,
                    onExpandedChange = { expandedFrom = !expandedFrom }
                ) {
                    OutlinedTextField(
                        value = airports.find { it.id == sanBayDiId }?.let { "${it.ma_san_bay} - ${it.ten_san_bay}" } ?: "",
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        label = { Text("Sân bay đi") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedFrom) },
                        isError = formErrors.containsKey("sanBayDi"),
                        supportingText = formErrors["sanBayDi"]?.let { { Text(it) } }
                    )
                    ExposedDropdownMenu(
                        expanded = expandedFrom,
                        onDismissRequest = { expandedFrom = false }
                    ) {
                        airports.forEach { airport ->
                            DropdownMenuItem(
                                text = { Text("${airport.ma_san_bay} - ${airport.ten_san_bay}") },
                                onClick = {
                                    onSanBayDiChange(airport.id)
                                    expandedFrom = false
                                }
                            )
                        }
                    }
                }
                
                // Airport to dropdown
                var expandedTo by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expandedTo,
                    onExpandedChange = { expandedTo = !expandedTo }
                ) {
                    OutlinedTextField(
                        value = airports.find { it.id == sanBayDenId }?.let { "${it.ma_san_bay} - ${it.ten_san_bay}" } ?: "",
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        label = { Text("Sân bay đến") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedTo) },
                        isError = formErrors.containsKey("sanBayDen"),
                        supportingText = formErrors["sanBayDen"]?.let { { Text(it) } }
                    )
                    ExposedDropdownMenu(
                        expanded = expandedTo,
                        onDismissRequest = { expandedTo = false }
                    ) {
                        airports.forEach { airport ->
                            DropdownMenuItem(
                                text = { Text("${airport.ma_san_bay} - ${airport.ten_san_bay}") },
                                onClick = {
                                    onSanBayDenChange(airport.id)
                                    expandedTo = false
                                }
                            )
                        }
                    }
                }
                
                // Approval checkbox
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = duocPheDuyet,
                        onCheckedChange = onDuocPheDuyetChange
                    )
                    Text(
                        text = "Phê duyệt ngay",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
                
                if (formErrors.containsKey("general")) {
                    Text(
                        text = formErrors["general"]!!,
                        color = ErrorRed,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        enabled = !isSubmitting
                    ) {
                        Text("Hủy")
                    }
                    Button(
                        onClick = onSubmit,
                        modifier = Modifier.weight(1f),
                        enabled = !isSubmitting,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryBlue
                        )
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(if (route != null) "Cập nhật" else "Thêm")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RouteErrorView(message: String, onRetry: () -> Unit) {
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
fun RouteEmptyView(hasSearch: Boolean, onAddClick: () -> Unit) {
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
                text = if (hasSearch) "Không tìm thấy tuyến bay" else "Không có tuyến bay nào",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = if (hasSearch) "Thử tìm kiếm với từ khóa khác" else "Bắt đầu bằng cách thêm tuyến bay mới",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
            if (!hasSearch) {
                Button(
                    onClick = onAddClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryBlue
                    )
                ) {
                    Text("Thêm tuyến bay đầu tiên")
                }
            }
        }
    }
}

