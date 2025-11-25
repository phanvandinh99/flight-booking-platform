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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.example.flybook.data.models.AirlineAircraft
import com.example.flybook.data.models.AirlineFlight
import com.example.flybook.data.models.ApprovedRoute
import com.example.flybook.data.repository.AircraftRepository
import com.example.flybook.data.repository.FlightRepository
import com.example.flybook.ui.components.BottomNavigationBar
import com.example.flybook.ui.components.airlineBottomNavItems
import com.example.flybook.ui.components.DateTimePicker
import com.example.flybook.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

fun formatDateTime(dateString: String, formatter: SimpleDateFormat): String {
    return try {
        val date = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(dateString)
        if (date != null) {
            formatter.format(date)
        } else {
            dateString
        }
    } catch (e: Exception) {
        dateString
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlightManagementScreen(navController: NavController) {
    val flightRepository = remember { FlightRepository() }
    val aircraftRepository = remember { AircraftRepository() }
    val scope = rememberCoroutineScope()
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var flights by remember { mutableStateOf<List<AirlineFlight>>(emptyList()) }
    var aircrafts by remember { mutableStateOf<List<AirlineAircraft>>(emptyList()) }
    var approvedRoutes by remember { mutableStateOf<List<ApprovedRoute>>(emptyList()) }
    var searchTerm by remember { mutableStateOf("") }
    var filterNgayKhoiHanh by remember { mutableStateOf("") }
    var filterTrangThai by remember { mutableStateOf("") }
    var filterTuyenBay by remember { mutableStateOf<Int?>(null) }
    var showDialog by remember { mutableStateOf(false) }
    var editingFlight by remember { mutableStateOf<AirlineFlight?>(null) }
    var maMayBay by remember { mutableStateOf<Int?>(null) }
    var maChuyenBay by remember { mutableStateOf("") }
    var maTuyenBay by remember { mutableStateOf<Int?>(null) }
    var gioKhoiHanh by remember { mutableStateOf("") }
    var gioHaCanh by remember { mutableStateOf("") }
    var tanSuat by remember { mutableStateOf("hang_ngay") }
    var trangThai by remember { mutableStateOf("du_kien") }
    var formErrors by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var submitting by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var deletingFlightId by remember { mutableStateOf<Int?>(null) }
    
    val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.getDefault())
    val displayDateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    
    fun loadFlights() {
        scope.launch {
            loading = true
            error = null
            val result = flightRepository.getFlights(
                ngayKhoiHanh = if (filterNgayKhoiHanh.isNotEmpty()) filterNgayKhoiHanh else null,
                trangThai = if (filterTrangThai.isNotEmpty()) filterTrangThai else null,
                maTuyenBay = filterTuyenBay
            )
            result.onSuccess { 
                flights = it
                error = null
            }.onFailure { 
                error = it.message
            }
            loading = false
        }
    }
    
    fun loadAircrafts() {
        scope.launch {
            aircraftRepository.getAircrafts().onSuccess {
                aircrafts = it
            }
        }
    }
    
    fun loadApprovedRoutes() {
        scope.launch {
            flightRepository.getApprovedRoutes()
                .onSuccess {
                    approvedRoutes = it
                }
                .onFailure { e ->
                    error = "Không thể tải danh sách tuyến bay: ${e.message}"
                }
        }
    }
    
    LaunchedEffect(Unit) {
        loadFlights()
        loadAircrafts()
        loadApprovedRoutes()
    }
    
    LaunchedEffect(filterNgayKhoiHanh, filterTrangThai, filterTuyenBay) {
        loadFlights()
    }
    
    fun parseDateTime(dateTimeString: String): String {
        // Try multiple formats
        val formats = listOf(
            "yyyy-MM-dd'T'HH:mm:ss",
            "yyyy-MM-dd'T'HH:mm",
            "yyyy-MM-dd HH:mm:ss",
            "yyyy-MM-dd HH:mm",
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            "yyyy-MM-dd'T'HH:mm:ss'Z'"
        )
        
        for (format in formats) {
            try {
                val parsed = SimpleDateFormat(format, Locale.getDefault()).parse(dateTimeString)
                if (parsed != null) {
                    return dateFormat.format(parsed)
                }
            } catch (e: Exception) {
                // Try next format
            }
        }
        return ""
    }
    
    fun openDialog(flight: AirlineFlight? = null) {
        editingFlight = flight
        if (flight != null) {
            maMayBay = flight.ma_may_bay
            maChuyenBay = flight.ma_chuyen_bay
            maTuyenBay = flight.ma_tuyen_bay
            gioKhoiHanh = parseDateTime(flight.gio_khoi_hanh)
            gioHaCanh = parseDateTime(flight.gio_ha_canh)
            tanSuat = flight.tan_suat
            trangThai = flight.trang_thai
        } else {
            maMayBay = null
            maChuyenBay = ""
            maTuyenBay = null
            gioKhoiHanh = ""
            gioHaCanh = ""
            tanSuat = "hang_ngay"
            trangThai = "du_kien"
        }
        formErrors = emptyMap()
        showDialog = true
    }
    
    fun closeDialog() {
        showDialog = false
        editingFlight = null
        maMayBay = null
        maChuyenBay = ""
        maTuyenBay = null
        gioKhoiHanh = ""
        gioHaCanh = ""
        tanSuat = "hang_ngay"
        trangThai = "du_kien"
        formErrors = emptyMap()
    }
    
    fun validateForm(): Boolean {
        val errors = mutableMapOf<String, String>()
        if (maMayBay == null) {
            errors["ma_may_bay"] = "Máy bay là bắt buộc"
        }
        if (maChuyenBay.trim().isEmpty()) {
            errors["ma_chuyen_bay"] = "Mã chuyến bay là bắt buộc"
        }
        if (maTuyenBay == null) {
            errors["ma_tuyen_bay"] = "Tuyến bay là bắt buộc"
        }
        if (gioKhoiHanh.trim().isEmpty()) {
            errors["gio_khoi_hanh"] = "Giờ khởi hành là bắt buộc"
        }
        if (gioHaCanh.trim().isEmpty()) {
            errors["gio_ha_canh"] = "Giờ hạ cánh là bắt buộc"
        }
        formErrors = errors
        return errors.isEmpty()
    }
    
    fun handleSubmit() {
        if (!validateForm()) return
        
        scope.launch {
            submitting = true
            val result = if (editingFlight != null && maMayBay != null && maTuyenBay != null) {
                flightRepository.updateFlight(
                    editingFlight!!.id,
                    maMayBay,
                    maChuyenBay.takeIf { it.isNotEmpty() },
                    maTuyenBay,
                    gioKhoiHanh.takeIf { it.isNotEmpty() },
                    gioHaCanh.takeIf { it.isNotEmpty() },
                    tanSuat.takeIf { it.isNotEmpty() },
                    trangThai.takeIf { it.isNotEmpty() }
                )
            } else if (maMayBay != null && maTuyenBay != null) {
                flightRepository.createFlight(
                    maMayBay!!,
                    maChuyenBay,
                    maTuyenBay!!,
                    gioKhoiHanh,
                    gioHaCanh,
                    tanSuat,
                    trangThai
                )
            } else {
                return@launch
            }
            
            result.onSuccess {
                loadFlights()
                closeDialog()
            }.onFailure {
                error = it.message
            }
            submitting = false
        }
    }
    
    fun handleDelete(id: Int) {
        deletingFlightId = id
        showDeleteDialog = true
    }
    
    fun confirmDelete() {
        deletingFlightId?.let { id ->
            scope.launch {
                val result = flightRepository.deleteFlight(id)
                result.onSuccess {
                    loadFlights()
                }.onFailure {
                    error = it.message
                }
            }
        }
        showDeleteDialog = false
        deletingFlightId = null
    }
    
    val filteredFlights = flights.filter {
        it.ma_chuyen_bay.contains(searchTerm, ignoreCase = true) ||
        it.may_bay?.loai_may_bay?.contains(searchTerm, ignoreCase = true) == true ||
        it.tuyen_bay?.san_bay_di?.ten_san_bay?.contains(searchTerm, ignoreCase = true) == true ||
        it.tuyen_bay?.san_bay_den?.ten_san_bay?.contains(searchTerm, ignoreCase = true) == true
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Quản Lý Chuyến Bay") },
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
                    IconButton(onClick = { loadFlights() }) {
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
                Icon(Icons.Default.Add, contentDescription = "Add Flight")
            }
        }
    ) { paddingValues ->
        if (loading && flights.isEmpty()) {
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
                    Text("Đang tải danh sách chuyến bay...", color = TextSecondary)
                }
            }
        } else if (error != null && flights.isEmpty()) {
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
                        onClick = { loadFlights() },
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
                    placeholder = { Text("Tìm kiếm chuyến bay...") },
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
                
                // Filters
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = filterNgayKhoiHanh,
                        onValueChange = { filterNgayKhoiHanh = it },
                        modifier = Modifier.weight(1f),
                        label = { Text("Ngày khởi hành") },
                        placeholder = { Text("YYYY-MM-DD") },
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp)
                    )
                    DropdownMenu(
                        expanded = false,
                        onDismissRequest = { }
                    ) {}
                    // Simplified - using TextField for status filter
                    OutlinedTextField(
                        value = filterTrangThai,
                        onValueChange = { filterTrangThai = it },
                        modifier = Modifier.weight(1f),
                        label = { Text("Trạng thái") },
                        placeholder = { Text("du_kien, bi_huy, da_hoan_thanh") },
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp)
                    )
                }
                
                // Flights list
                if (filteredFlights.isEmpty()) {
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
                                if (searchTerm.isNotEmpty()) "Không tìm thấy chuyến bay" else "Không có chuyến bay nào",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                            Text(
                                if (searchTerm.isNotEmpty()) "Thử tìm kiếm với từ khóa khác" else "Bắt đầu bằng cách thêm chuyến bay mới",
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
                                Text("Mã Chuyến Bay", fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.weight(1f))
                                Text("Tuyến Bay", fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.weight(1.5f))
                                Text("Giờ KH", fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.weight(1f))
                                Text("Trạng Thái", fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.weight(1f))
                                Text("Thao Tác", fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.width(80.dp))
                            }
                            
                            // Rows
                            filteredFlights.forEach { flight ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(BackgroundLight, RoundedCornerShape(8.dp))
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        flight.ma_chuyen_bay,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Column(
                                        modifier = Modifier.weight(1.5f),
                                        verticalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        Text(
                                            "${flight.tuyen_bay?.san_bay_di?.ten_san_bay ?: "N/A"} → ${flight.tuyen_bay?.san_bay_den?.ten_san_bay ?: "N/A"}",
                                            fontSize = 10.sp,
                                            color = TextPrimary
                                        )
                                        Text(
                                            flight.may_bay?.loai_may_bay ?: "N/A",
                                            fontSize = 9.sp,
                                            color = TextSecondary
                                        )
                                    }
                                    Column(
                                        modifier = Modifier.weight(1f),
                                        verticalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        Text(
                                            formatDateTime(flight.gio_khoi_hanh, displayDateFormat),
                                            fontSize = 10.sp,
                                            color = TextPrimary
                                        )
                                    }
                                    Text(
                                        when (flight.trang_thai) {
                                            "du_kien" -> "Dự kiến"
                                            "bi_huy" -> "Bị hủy"
                                            "da_hoan_thanh" -> "Hoàn thành"
                                            else -> flight.trang_thai
                                        },
                                        fontSize = 10.sp,
                                        color = when (flight.trang_thai) {
                                            "du_kien" -> PrimaryBlue
                                            "bi_huy" -> ErrorRed
                                            "da_hoan_thanh" -> Color(0xFF4CAF50)
                                            else -> TextSecondary
                                        },
                                        modifier = Modifier.weight(1f)
                                    )
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.width(80.dp)
                                    ) {
                                        IconButton(
                                            onClick = { openDialog(flight) },
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
                                            onClick = { handleDelete(flight.id) },
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
    
    // Dialog for add/edit - sẽ được implement tiếp
    if (showDialog) {
        FlightDialog(
            editingFlight = editingFlight,
            aircrafts = aircrafts,
            approvedRoutes = approvedRoutes,
            maMayBay = maMayBay,
            maChuyenBay = maChuyenBay,
            maTuyenBay = maTuyenBay,
            gioKhoiHanh = gioKhoiHanh,
            gioHaCanh = gioHaCanh,
            tanSuat = tanSuat,
            trangThai = trangThai,
            formErrors = formErrors,
            submitting = submitting,
            onMaMayBayChange = { maMayBay = it },
            onMaChuyenBayChange = { maChuyenBay = it },
            onMaTuyenBayChange = { maTuyenBay = it },
            onGioKhoiHanhChange = { gioKhoiHanh = it },
            onGioHaCanhChange = { gioHaCanh = it },
            onTanSuatChange = { tanSuat = it },
            onTrangThaiChange = { trangThai = it },
            onClose = { closeDialog() },
            onSubmit = { handleSubmit() }
        )
    }
    
    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Xác nhận xóa") },
            text = { Text("Bạn có chắc chắn muốn xóa chuyến bay này?") },
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlightDialog(
    editingFlight: AirlineFlight?,
    aircrafts: List<AirlineAircraft>,
    approvedRoutes: List<ApprovedRoute>,
    maMayBay: Int?,
    maChuyenBay: String,
    maTuyenBay: Int?,
    gioKhoiHanh: String,
    gioHaCanh: String,
    tanSuat: String,
    trangThai: String,
    formErrors: Map<String, String>,
    submitting: Boolean,
    onMaMayBayChange: (Int?) -> Unit,
    onMaChuyenBayChange: (String) -> Unit,
    onMaTuyenBayChange: (Int?) -> Unit,
    onGioKhoiHanhChange: (String) -> Unit,
    onGioHaCanhChange: (String) -> Unit,
    onTanSuatChange: (String) -> Unit,
    onTrangThaiChange: (String) -> Unit,
    onClose: () -> Unit,
    onSubmit: () -> Unit
) {
    Dialog(onDismissRequest = onClose) {
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
                        if (editingFlight != null) "Sửa chuyến bay" else "Thêm chuyến bay mới",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }
                
                // Aircraft dropdown
                var aircraftExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = aircraftExpanded,
                    onExpandedChange = { aircraftExpanded = !aircraftExpanded }
                ) {
                    OutlinedTextField(
                        value = aircrafts.find { it.id == maMayBay }?.loai_may_bay ?: "",
                        onValueChange = { },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        label = { Text("Máy Bay *") },
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = aircraftExpanded) },
                        isError = formErrors.containsKey("ma_may_bay"),
                        supportingText = {
                            if (formErrors.containsKey("ma_may_bay")) {
                                Text(formErrors["ma_may_bay"] ?: "", color = ErrorRed)
                            }
                        },
                        shape = RoundedCornerShape(8.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = aircraftExpanded,
                        onDismissRequest = { aircraftExpanded = false }
                    ) {
                        aircrafts.forEach { aircraft ->
                            DropdownMenuItem(
                                text = { Text("${aircraft.loai_may_bay} (${aircraft.tong_so_ghe} ghế)") },
                                onClick = {
                                    onMaMayBayChange(aircraft.id)
                                    aircraftExpanded = false
                                }
                            )
                        }
                    }
                }
                
                OutlinedTextField(
                    value = maChuyenBay,
                    onValueChange = onMaChuyenBayChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Mã Chuyến Bay *") },
                    singleLine = true,
                    isError = formErrors.containsKey("ma_chuyen_bay"),
                    supportingText = {
                        if (formErrors.containsKey("ma_chuyen_bay")) {
                            Text(formErrors["ma_chuyen_bay"] ?: "", color = ErrorRed)
                        }
                    },
                    shape = RoundedCornerShape(8.dp)
                )
                
                // Route dropdown
                var routeExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = routeExpanded,
                    onExpandedChange = { routeExpanded = !routeExpanded }
                ) {
                    OutlinedTextField(
                        value = approvedRoutes.find { it.id == maTuyenBay }?.let { 
                            "${it.san_bay_di?.ten_san_bay ?: "N/A"} → ${it.san_bay_den?.ten_san_bay ?: "N/A"}"
                        } ?: "",
                        onValueChange = { },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        label = { Text("Tuyến Bay *") },
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = routeExpanded) },
                        isError = formErrors.containsKey("ma_tuyen_bay"),
                        supportingText = {
                            if (formErrors.containsKey("ma_tuyen_bay")) {
                                Text(formErrors["ma_tuyen_bay"] ?: "", color = ErrorRed)
                            }
                        },
                        shape = RoundedCornerShape(8.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = routeExpanded,
                        onDismissRequest = { routeExpanded = false }
                    ) {
                        approvedRoutes.forEach { route ->
                            DropdownMenuItem(
                                text = { Text("${route.san_bay_di?.ten_san_bay ?: "N/A"} → ${route.san_bay_den?.ten_san_bay ?: "N/A"}") },
                                onClick = {
                                    onMaTuyenBayChange(route.id)
                                    routeExpanded = false
                                }
                            )
                        }
                    }
                }
                
                DateTimePicker(
                    label = "Giờ Khởi Hành *",
                    value = gioKhoiHanh,
                    onValueChange = onGioKhoiHanhChange,
                    modifier = Modifier.fillMaxWidth(),
                    isError = formErrors.containsKey("gio_khoi_hanh"),
                    errorMessage = formErrors["gio_khoi_hanh"],
                    supportingText = if (!formErrors.containsKey("gio_khoi_hanh")) "Chọn ngày và giờ khởi hành" else null
                )
                
                DateTimePicker(
                    label = "Giờ Hạ Cánh *",
                    value = gioHaCanh,
                    onValueChange = onGioHaCanhChange,
                    modifier = Modifier.fillMaxWidth(),
                    isError = formErrors.containsKey("gio_ha_canh"),
                    errorMessage = formErrors["gio_ha_canh"],
                    supportingText = if (!formErrors.containsKey("gio_ha_canh")) "Chọn ngày và giờ hạ cánh" else null
                )
                
                // Frequency dropdown
                var tanSuatExpanded by remember { mutableStateOf(false) }
                val tanSuatOptions = listOf(
                    "hang_ngay" to "Hàng ngày",
                    "thu_2_thu_4" to "Thứ 2 - Thứ 4",
                    "thu_3_thu_5" to "Thứ 3 - Thứ 5",
                    "thu_4_thu_6" to "Thứ 4 - Thứ 6",
                    "thu_5_thu_7" to "Thứ 5 - Thứ 7",
                    "thu_6_cn" to "Thứ 6 - CN",
                    "thu_7_cn" to "Thứ 7 - CN",
                    "cn_thu_2" to "CN - Thứ 2"
                )
                ExposedDropdownMenuBox(
                    expanded = tanSuatExpanded,
                    onExpandedChange = { tanSuatExpanded = !tanSuatExpanded }
                ) {
                    OutlinedTextField(
                        value = tanSuatOptions.find { it.first == tanSuat }?.second ?: tanSuat,
                        onValueChange = { },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        label = { Text("Tần Suất") },
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = tanSuatExpanded) },
                        shape = RoundedCornerShape(8.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = tanSuatExpanded,
                        onDismissRequest = { tanSuatExpanded = false }
                    ) {
                        tanSuatOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option.second) },
                                onClick = {
                                    onTanSuatChange(option.first)
                                    tanSuatExpanded = false
                                }
                            )
                        }
                    }
                }
                
                // Status dropdown
                var trangThaiExpanded by remember { mutableStateOf(false) }
                val trangThaiOptions = listOf(
                    "du_kien" to "Dự kiến",
                    "bi_huy" to "Bị hủy",
                    "da_hoan_thanh" to "Đã hoàn thành"
                )
                ExposedDropdownMenuBox(
                    expanded = trangThaiExpanded,
                    onExpandedChange = { trangThaiExpanded = !trangThaiExpanded }
                ) {
                    OutlinedTextField(
                        value = trangThaiOptions.find { it.first == trangThai }?.second ?: trangThai,
                        onValueChange = { },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        label = { Text("Trạng Thái") },
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = trangThaiExpanded) },
                        shape = RoundedCornerShape(8.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = trangThaiExpanded,
                        onDismissRequest = { trangThaiExpanded = false }
                    ) {
                        trangThaiOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option.second) },
                                onClick = {
                                    onTrangThaiChange(option.first)
                                    trangThaiExpanded = false
                                }
                            )
                        }
                    }
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onClose,
                        modifier = Modifier.weight(1f),
                        enabled = !submitting
                    ) {
                        Text("Hủy")
                    }
                    Button(
                        onClick = onSubmit,
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
                            Text(if (editingFlight != null) "Cập nhật" else "Tạo mới")
                        }
                    }
                }
            }
        }
    }
}

