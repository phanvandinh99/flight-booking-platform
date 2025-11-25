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
import com.example.flybook.data.repository.AirportRepository
import com.example.flybook.ui.components.BottomNavigationBar
import com.example.flybook.ui.components.adminBottomNavItems
import com.example.flybook.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AirportManagementScreen(navController: NavController) {
    val airportRepository = remember { AirportRepository() }
    val scope = rememberCoroutineScope()
    
    var airports by remember { mutableStateOf<List<Airport>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showDialog by remember { mutableStateOf(false) }
    var editingAirport by remember { mutableStateOf<Airport?>(null) }
    var searchTerm by remember { mutableStateOf("") }
    
    // Form state
    var maSanBay by remember { mutableStateOf("") }
    var tenSanBay by remember { mutableStateOf("") }
    var thanhPho by remember { mutableStateOf("") }
    var quocGia by remember { mutableStateOf("") }
    var formErrors by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var isSubmitting by remember { mutableStateOf(false) }
    
    fun loadAirports() {
        scope.launch {
            isLoading = true
            errorMessage = null
            airportRepository.getAirports()
                .onSuccess {
                    airports = it
                    errorMessage = null
                }
                .onFailure { e ->
                    errorMessage = e.message ?: "Không thể tải danh sách sân bay"
                }
            isLoading = false
        }
    }
    
    LaunchedEffect(Unit) {
        loadAirports()
    }
    
    fun openDialog(airport: Airport? = null) {
        editingAirport = airport
        if (airport != null) {
            maSanBay = airport.ma_san_bay
            tenSanBay = airport.ten_san_bay
            thanhPho = airport.thanh_pho ?: ""
            quocGia = airport.quoc_gia ?: ""
        } else {
            maSanBay = ""
            tenSanBay = ""
            thanhPho = ""
            quocGia = ""
        }
        formErrors = emptyMap()
        showDialog = true
    }
    
    fun closeDialog() {
        showDialog = false
        editingAirport = null
        maSanBay = ""
        tenSanBay = ""
        thanhPho = ""
        quocGia = ""
        formErrors = emptyMap()
    }
    
    fun validateForm(): Boolean {
        val errors = mutableMapOf<String, String>()
        if (maSanBay.isBlank()) errors["maSanBay"] = "Mã sân bay không được để trống"
        if (tenSanBay.isBlank()) errors["tenSanBay"] = "Tên sân bay không được để trống"
        if (thanhPho.isBlank()) errors["thanhPho"] = "Thành phố không được để trống"
        if (quocGia.isBlank()) errors["quocGia"] = "Quốc gia không được để trống"
        formErrors = errors
        return errors.isEmpty()
    }
    
    fun handleSubmit() {
        if (!validateForm()) return
        
        scope.launch {
            isSubmitting = true
            val result = if (editingAirport != null) {
                airportRepository.updateAirport(
                    id = editingAirport!!.id,
                    maSanBay = maSanBay,
                    tenSanBay = tenSanBay,
                    thanhPho = thanhPho,
                    quocGia = quocGia
                )
            } else {
                airportRepository.createAirport(
                    maSanBay = maSanBay,
                    tenSanBay = tenSanBay,
                    thanhPho = thanhPho,
                    quocGia = quocGia
                )
            }
            
            result.onSuccess {
                loadAirports()
                closeDialog()
            }.onFailure { e ->
                formErrors = mapOf("general" to (e.message ?: "Có lỗi xảy ra"))
            }
            isSubmitting = false
        }
    }
    
    fun handleDelete(airport: Airport) {
        scope.launch {
            airportRepository.deleteAirport(airport.id)
                .onSuccess {
                    loadAirports()
                }
                .onFailure { e ->
                    errorMessage = e.message ?: "Không thể xóa sân bay"
                }
        }
    }
    
    val filteredAirports = airports.filter {
        searchTerm.isBlank() || 
        it.ma_san_bay.contains(searchTerm, ignoreCase = true) ||
        it.ten_san_bay.contains(searchTerm, ignoreCase = true) ||
        (it.thanh_pho?.contains(searchTerm, ignoreCase = true) == true) ||
        (it.quoc_gia?.contains(searchTerm, ignoreCase = true) == true)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Quản lý Sân Bay") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { loadAirports() },
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
                    contentDescription = "Thêm sân bay",
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
                    placeholder = { Text("Tìm kiếm sân bay...") },
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
                    isLoading && airports.isEmpty() -> {
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
                                    text = "Đang tải danh sách sân bay...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                    
                    errorMessage != null -> {
                        AirportErrorView(
                            message = errorMessage!!,
                            onRetry = { loadAirports() }
                        )
                    }
                    
                    filteredAirports.isEmpty() -> {
                        EmptyView(
                            hasSearch = searchTerm.isNotEmpty(),
                            onAddClick = { openDialog() }
                        )
                    }
                    
                    else -> {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(filteredAirports) { airport ->
                                AirportCard(
                                    airport = airport,
                                    onEdit = { openDialog(airport) },
                                    onDelete = { handleDelete(airport) }
                                )
                            }
                        }
                    }
                }
            }
        }
        
        // Dialog for Add/Edit
        if (showDialog) {
            AirportDialog(
                airport = editingAirport,
                maSanBay = maSanBay,
                tenSanBay = tenSanBay,
                thanhPho = thanhPho,
                quocGia = quocGia,
                onMaSanBayChange = { maSanBay = it },
                onTenSanBayChange = { tenSanBay = it },
                onThanhPhoChange = { thanhPho = it },
                onQuocGiaChange = { quocGia = it },
                formErrors = formErrors,
                isSubmitting = isSubmitting,
                onDismiss = { closeDialog() },
                onSubmit = { handleSubmit() }
            )
        }
    }
}

@Composable
fun AirportCard(
    airport: Airport,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = airport.ma_san_bay,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = PrimaryBlue
                    )
                }
                Text(
                    text = airport.ten_san_bay,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    airport.thanh_pho?.let {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "City",
                                modifier = Modifier.size(16.dp),
                                tint = TextSecondary
                            )
                            Text(
                                text = it,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }
                    airport.quoc_gia?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            }
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
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
fun AirportDialog(
    airport: Airport?,
    maSanBay: String,
    tenSanBay: String,
    thanhPho: String,
    quocGia: String,
    onMaSanBayChange: (String) -> Unit,
    onTenSanBayChange: (String) -> Unit,
    onThanhPhoChange: (String) -> Unit,
    onQuocGiaChange: (String) -> Unit,
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
                    text = if (airport != null) "Sửa sân bay" else "Thêm sân bay mới",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
                
                OutlinedTextField(
                    value = maSanBay,
                    onValueChange = onMaSanBayChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Mã sân bay") },
                    enabled = airport == null, // Disable when editing
                    isError = formErrors.containsKey("maSanBay"),
                    supportingText = formErrors["maSanBay"]?.let { { Text(it) } }
                )
                
                OutlinedTextField(
                    value = tenSanBay,
                    onValueChange = onTenSanBayChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Tên sân bay") },
                    isError = formErrors.containsKey("tenSanBay"),
                    supportingText = formErrors["tenSanBay"]?.let { { Text(it) } }
                )
                
                OutlinedTextField(
                    value = thanhPho,
                    onValueChange = onThanhPhoChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Thành phố") },
                    isError = formErrors.containsKey("thanhPho"),
                    supportingText = formErrors["thanhPho"]?.let { { Text(it) } }
                )
                
                OutlinedTextField(
                    value = quocGia,
                    onValueChange = onQuocGiaChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Quốc gia") },
                    isError = formErrors.containsKey("quocGia"),
                    supportingText = formErrors["quocGia"]?.let { { Text(it) } }
                )
                
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
                            Text(if (airport != null) "Cập nhật" else "Thêm")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AirportErrorView(message: String, onRetry: () -> Unit) {
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
fun EmptyView(hasSearch: Boolean, onAddClick: () -> Unit) {
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
                text = if (hasSearch) "Không tìm thấy sân bay" else "Không có sân bay nào",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = if (hasSearch) "Thử tìm kiếm với từ khóa khác" else "Bắt đầu bằng cách thêm sân bay mới",
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
                    Text("Thêm sân bay đầu tiên")
                }
            }
        }
    }
}

