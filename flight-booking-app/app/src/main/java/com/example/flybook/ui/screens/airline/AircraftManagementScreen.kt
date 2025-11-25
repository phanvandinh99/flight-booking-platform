package com.example.flybook.ui.screens.airline

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.example.flybook.data.models.AirlineAircraft
import com.example.flybook.data.repository.AircraftRepository
import com.example.flybook.ui.components.BottomNavigationBar
import com.example.flybook.ui.components.airlineBottomNavItems
import com.example.flybook.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AircraftManagementScreen(navController: NavController) {
    val repository = remember { AircraftRepository() }
    val scope = rememberCoroutineScope()
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var aircrafts by remember { mutableStateOf<List<AirlineAircraft>>(emptyList()) }
    var searchTerm by remember { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) }
    var editingAircraft by remember { mutableStateOf<AirlineAircraft?>(null) }
    var loaiMayBay by remember { mutableStateOf("") }
    var tongSoGhe by remember { mutableStateOf("") }
    var formErrors by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var submitting by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var deletingAircraftId by remember { mutableStateOf<Int?>(null) }
    
    fun loadAircrafts() {
        scope.launch {
            loading = true
            error = null
            val result = repository.getAircrafts()
            result.onSuccess { 
                aircrafts = it
                error = null
            }.onFailure { 
                error = it.message
            }
            loading = false
        }
    }
    
    LaunchedEffect(Unit) {
        loadAircrafts()
    }
    
    fun openDialog(aircraft: AirlineAircraft? = null) {
        editingAircraft = aircraft
        if (aircraft != null) {
            loaiMayBay = aircraft.loai_may_bay
            tongSoGhe = aircraft.tong_so_ghe.toString()
        } else {
            loaiMayBay = ""
            tongSoGhe = ""
        }
        formErrors = emptyMap()
        showDialog = true
    }
    
    fun closeDialog() {
        showDialog = false
        editingAircraft = null
        loaiMayBay = ""
        tongSoGhe = ""
        formErrors = emptyMap()
    }
    
    fun validateForm(): Boolean {
        val errors = mutableMapOf<String, String>()
        if (loaiMayBay.trim().isEmpty()) {
            errors["loai_may_bay"] = "Loại máy bay không được để trống"
        }
        if (tongSoGhe.trim().isEmpty()) {
            errors["tong_so_ghe"] = "Tổng số ghế không được để trống"
        } else {
            val seats = tongSoGhe.toIntOrNull()
            if (seats == null || seats < 1 || seats > 1000) {
                errors["tong_so_ghe"] = "Tổng số ghế phải là số từ 1 đến 1000"
            }
        }
        formErrors = errors
        return errors.isEmpty()
    }
    
    fun handleSubmit() {
        if (!validateForm()) return
        
        scope.launch {
            submitting = true
            val seats = tongSoGhe.toInt()
            val result = if (editingAircraft != null) {
                repository.updateAircraft(editingAircraft!!.id, loaiMayBay, seats, null)
            } else {
                repository.createAircraft(loaiMayBay, seats, null)
            }
            
            result.onSuccess {
                loadAircrafts()
                closeDialog()
            }.onFailure {
                error = it.message
            }
            submitting = false
        }
    }
    
    fun handleDelete(id: Int) {
        deletingAircraftId = id
        showDeleteDialog = true
    }
    
    fun confirmDelete() {
        deletingAircraftId?.let { id ->
            scope.launch {
                val result = repository.deleteAircraft(id)
                result.onSuccess {
                    loadAircrafts()
                }.onFailure {
                    error = it.message
                }
            }
        }
        showDeleteDialog = false
        deletingAircraftId = null
    }
    
    val filteredAircrafts = aircrafts.filter {
        it.loai_may_bay.contains(searchTerm, ignoreCase = true) ||
        it.tong_so_ghe.toString().contains(searchTerm, ignoreCase = true) ||
        it.hang_hang_khong?.ten_hang?.contains(searchTerm, ignoreCase = true) == true
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Quản Lý Máy Bay") },
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
                    IconButton(onClick = { loadAircrafts() }) {
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
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { openDialog() },
                containerColor = PrimaryBlue,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Aircraft")
            }
        }
    ) { paddingValues ->
        if (loading && aircrafts.isEmpty()) {
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
                    Text("Đang tải danh sách máy bay...", color = TextSecondary)
                }
            }
        } else if (error != null && aircrafts.isEmpty()) {
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
                        onClick = { loadAircrafts() },
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
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Search bar
                OutlinedTextField(
                    value = searchTerm,
                    onValueChange = { searchTerm = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Tìm kiếm máy bay...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                    trailingIcon = {
                        if (searchTerm.isNotEmpty()) {
                            IconButton(onClick = { searchTerm = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { })
                )
                
                // Aircrafts list
                if (filteredAircrafts.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.padding(32.dp)
                        ) {
                            Icon(
                                Icons.Default.LocationOn,
                                contentDescription = "Empty",
                                modifier = Modifier.size(64.dp),
                                tint = TextSecondary
                            )
                            Text(
                                if (searchTerm.isNotEmpty()) "Không tìm thấy máy bay" else "Không có máy bay nào",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                            Text(
                                if (searchTerm.isNotEmpty()) "Thử tìm kiếm với từ khóa khác" else "Bắt đầu bằng cách thêm máy bay mới",
                                color = TextSecondary,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                } else {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = BackgroundCard)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
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
                                Text("Loại Máy Bay", fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.weight(2f))
                                Text("Tổng Số Ghế", fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.weight(1f))
                                Text("Hãng", fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.weight(1.5f))
                                Text("Thao Tác", fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.width(100.dp))
                            }
                            
                            // Rows
                            filteredAircrafts.forEach { aircraft ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(BackgroundLight, RoundedCornerShape(8.dp))
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        aircraft.loai_may_bay,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.weight(2f)
                                    )
                                    Text(
                                        "${aircraft.tong_so_ghe} ghế",
                                        fontSize = 12.sp,
                                        modifier = Modifier.weight(1f),
                                        color = TextSecondary
                                    )
                                    Text(
                                        aircraft.hang_hang_khong?.ten_hang ?: "N/A",
                                        fontSize = 12.sp,
                                        modifier = Modifier.weight(1.5f),
                                        color = TextSecondary
                                    )
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.width(100.dp)
                                    ) {
                                        IconButton(
                                            onClick = { openDialog(aircraft) },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Edit,
                                                contentDescription = "Edit",
                                                modifier = Modifier.size(16.dp),
                                                tint = PrimaryBlue
                                            )
                                        }
                                        IconButton(
                                            onClick = { handleDelete(aircraft.id) },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Delete,
                                                contentDescription = "Delete",
                                                modifier = Modifier.size(16.dp),
                                                tint = ErrorRed
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
    }
    
    // Dialog for add/edit
    if (showDialog) {
        Dialog(onDismissRequest = { closeDialog() }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = BackgroundCard)
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
                            if (editingAircraft != null) "Sửa máy bay" else "Thêm máy bay mới",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        IconButton(onClick = { closeDialog() }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }
                    
                    OutlinedTextField(
                        value = loaiMayBay,
                        onValueChange = { loaiMayBay = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Loại Máy Bay *") },
                        placeholder = { Text("VD: Boeing 737, Airbus A320, ...") },
                        singleLine = true,
                        isError = formErrors.containsKey("loai_may_bay"),
                        supportingText = {
                            if (formErrors.containsKey("loai_may_bay")) {
                                Text(formErrors["loai_may_bay"] ?: "", color = ErrorRed)
                            }
                        },
                        shape = RoundedCornerShape(8.dp)
                    )
                    
                    OutlinedTextField(
                        value = tongSoGhe,
                        onValueChange = { tongSoGhe = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Tổng Số Ghế *") },
                        placeholder = { Text("VD: 150, 200, ...") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        isError = formErrors.containsKey("tong_so_ghe"),
                        supportingText = {
                            if (formErrors.containsKey("tong_so_ghe")) {
                                Text(formErrors["tong_so_ghe"] ?: "", color = ErrorRed)
                            } else {
                                Text("Số ghế từ 1 đến 1000", fontSize = 10.sp, color = TextSecondary)
                            }
                        },
                        shape = RoundedCornerShape(8.dp)
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { closeDialog() },
                            modifier = Modifier.weight(1f),
                            enabled = !submitting
                        ) {
                            Text("Hủy")
                        }
                        Button(
                            onClick = { handleSubmit() },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                            enabled = !submitting
                        ) {
                            if (submitting) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(if (editingAircraft != null) "Cập nhật" else "Tạo mới")
                            }
                        }
                    }
                }
            }
        }
    }
    
    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Xác nhận xóa") },
            text = { Text("Bạn có chắc chắn muốn xóa máy bay này?") },
            confirmButton = {
                Button(
                    onClick = { confirmDelete() },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
                ) {
                    Text("Xóa")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteDialog = false }) {
                    Text("Hủy")
                }
            }
        )
    }
}

