package com.example.flybook.ui.screens.airline

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.example.flybook.data.models.AirlineFarePrice
import com.example.flybook.data.models.AirlineFlight
import com.example.flybook.data.repository.FarePriceRepository
import com.example.flybook.ui.components.BottomNavigationBar
import com.example.flybook.ui.components.airlineBottomNavItems
import com.example.flybook.ui.components.DateTimePicker
import com.example.flybook.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FareManagementScreen(navController: NavController) {
    val repository = remember { FarePriceRepository() }
    val scope = rememberCoroutineScope()
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var farePrices by remember { mutableStateOf<List<AirlineFarePrice>>(emptyList()) }
    var flights by remember { mutableStateOf<List<AirlineFlight>>(emptyList()) }
    var searchTerm by remember { mutableStateOf("") }
    var filterHangVe by remember { mutableStateOf("") }
    var filterMaChuyenBay by remember { mutableStateOf<Int?>(null) }
    var showDialog by remember { mutableStateOf(false) }
    var editingFarePrice by remember { mutableStateOf<AirlineFarePrice?>(null) }
    var maChuyenBay by remember { mutableStateOf<Int?>(null) }
    var hangVe by remember { mutableStateOf("pho_thong") }
    var gia by remember { mutableStateOf("") }
    var hanhLyKyGui by remember { mutableStateOf("") }
    var chinhSachHuyVe by remember { mutableStateOf("") }
    var chinhSachDoiVe by remember { mutableStateOf("") }
    var ngayBatDau by remember { mutableStateOf("") }
    var ngayKetThuc by remember { mutableStateOf("") }
    var formErrors by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var submitting by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var deletingFarePriceId by remember { mutableStateOf<Int?>(null) }
    
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val displayDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    
    fun loadFarePrices() {
        scope.launch {
            loading = true
            error = null
            val result = repository.getFarePrices(
                maChuyenBay = filterMaChuyenBay,
                hangVe = if (filterHangVe.isNotEmpty()) filterHangVe else null,
                ngayBatDau = null
            )
            result.onSuccess {
                farePrices = it
                error = null
            }.onFailure {
                error = it.message
            }
            loading = false
        }
    }
    
    fun loadFlights() {
        scope.launch {
            repository.getFlightsForPricing()
                .onSuccess { flights = it }
                .onFailure { }
        }
    }
    
    LaunchedEffect(Unit) {
        loadFarePrices()
        loadFlights()
    }
    
    LaunchedEffect(filterHangVe, filterMaChuyenBay) {
        loadFarePrices()
    }
    
    fun openDialog(farePrice: AirlineFarePrice? = null) {
        editingFarePrice = farePrice
        if (farePrice != null) {
            maChuyenBay = farePrice.ma_chuyen_bay
            hangVe = farePrice.hang_ve
            gia = farePrice.gia.toString()
            hanhLyKyGui = farePrice.hanh_ly_ky_gui
            chinhSachHuyVe = farePrice.chinh_sach_huy_ve ?: ""
            chinhSachDoiVe = farePrice.chinh_sach_doi_ve ?: ""
            try {
                val ngayBD = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(farePrice.ngay_bat_dau)
                val ngayKT = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(farePrice.ngay_ket_thuc)
                ngayBatDau = if (ngayBD != null) dateFormat.format(ngayBD) else farePrice.ngay_bat_dau
                ngayKetThuc = if (ngayKT != null) dateFormat.format(ngayKT) else farePrice.ngay_ket_thuc
            } catch (e: Exception) {
                ngayBatDau = farePrice.ngay_bat_dau
                ngayKetThuc = farePrice.ngay_ket_thuc
            }
        } else {
            maChuyenBay = null
            hangVe = "pho_thong"
            gia = ""
            hanhLyKyGui = ""
            chinhSachHuyVe = ""
            chinhSachDoiVe = ""
            ngayBatDau = ""
            ngayKetThuc = ""
        }
        formErrors = emptyMap()
        showDialog = true
    }
    
    fun closeDialog() {
        showDialog = false
        editingFarePrice = null
        maChuyenBay = null
        hangVe = "pho_thong"
        gia = ""
        hanhLyKyGui = ""
        chinhSachHuyVe = ""
        chinhSachDoiVe = ""
        ngayBatDau = ""
        ngayKetThuc = ""
        formErrors = emptyMap()
    }
    
    fun validateForm(): Boolean {
        val errors = mutableMapOf<String, String>()
        if (maChuyenBay == null) errors["ma_chuyen_bay"] = "Chuyến bay là bắt buộc"
        if (gia.trim().isEmpty()) errors["gia"] = "Giá vé là bắt buộc"
        else {
            try {
                val price = gia.toDouble()
                if (price < 0) errors["gia"] = "Giá vé phải lớn hơn hoặc bằng 0"
            } catch (e: Exception) {
                errors["gia"] = "Giá vé không hợp lệ"
            }
        }
        if (hanhLyKyGui.trim().isEmpty()) errors["hanh_ly_ky_gui"] = "Hành lý ký gửi là bắt buộc"
        if (ngayBatDau.trim().isEmpty()) errors["ngay_bat_dau"] = "Ngày bắt đầu là bắt buộc"
        if (ngayKetThuc.trim().isEmpty()) errors["ngay_ket_thuc"] = "Ngày kết thúc là bắt buộc"
        
        if (ngayBatDau.isNotEmpty() && ngayKetThuc.isNotEmpty()) {
            try {
                val start = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(ngayBatDau)
                val end = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(ngayKetThuc)
                if (start != null && end != null && start.after(end)) {
                    errors["ngay_ket_thuc"] = "Ngày kết thúc phải sau ngày bắt đầu"
                }
            } catch (e: Exception) {
                // Ignore parse errors, they will be caught by required validation
            }
        }
        
        formErrors = errors
        return errors.isEmpty()
    }
    
    fun handleSubmit() {
        if (!validateForm()) return
        
        scope.launch {
            submitting = true
            val result = if (editingFarePrice != null && maChuyenBay != null) {
                repository.updateFarePrice(
                    editingFarePrice!!.id,
                    com.example.flybook.data.api.UpdateFarePriceRequest(
                        ma_chuyen_bay = maChuyenBay,
                        hang_ve = hangVe,
                        gia = gia.toDoubleOrNull(),
                        hanh_ly_ky_gui = hanhLyKyGui.takeIf { it.isNotEmpty() },
                        chinh_sach_huy_ve = chinhSachHuyVe.takeIf { it.isNotEmpty() },
                        chinh_sach_doi_ve = chinhSachDoiVe.takeIf { it.isNotEmpty() },
                        ngay_bat_dau = ngayBatDau.takeIf { it.isNotEmpty() },
                        ngay_ket_thuc = ngayKetThuc.takeIf { it.isNotEmpty() }
                    )
                )
            } else if (maChuyenBay != null) {
                repository.createFarePrice(
                    com.example.flybook.data.api.CreateFarePriceRequest(
                        ma_chuyen_bay = maChuyenBay!!,
                        hang_ve = hangVe,
                        gia = gia.toDouble(),
                        hanh_ly_ky_gui = hanhLyKyGui,
                        chinh_sach_huy_ve = chinhSachHuyVe.takeIf { it.isNotEmpty() },
                        chinh_sach_doi_ve = chinhSachDoiVe.takeIf { it.isNotEmpty() },
                        ngay_bat_dau = ngayBatDau,
                        ngay_ket_thuc = ngayKetThuc
                    )
                )
            } else {
                return@launch
            }
            
            result.onSuccess {
                loadFarePrices()
                closeDialog()
            }.onFailure {
                error = it.message
            }
            submitting = false
        }
    }
    
    fun handleDelete(id: Int) {
        deletingFarePriceId = id
        showDeleteDialog = true
    }
    
    fun confirmDelete() {
        deletingFarePriceId?.let { id ->
            scope.launch {
                submitting = true
                repository.deleteFarePrice(id)
                    .onSuccess {
                        loadFarePrices()
                        showDeleteDialog = false
                        deletingFarePriceId = null
                    }
                    .onFailure {
                        error = it.message
                    }
                submitting = false
            }
        }
    }
    
    val filteredFarePrices = farePrices.filter {
        val matchesSearch = searchTerm.isBlank() ||
                it.chuyen_bay?.ma_chuyen_bay?.contains(searchTerm, ignoreCase = true) == true ||
                it.chuyen_bay?.tuyen_bay?.san_bay_di?.ten_san_bay?.contains(searchTerm, ignoreCase = true) == true ||
                it.chuyen_bay?.tuyen_bay?.san_bay_den?.ten_san_bay?.contains(searchTerm, ignoreCase = true) == true
        
        val matchesHangVe = filterHangVe.isBlank() || it.hang_ve == filterHangVe
        val matchesFlight = filterMaChuyenBay == null || it.ma_chuyen_bay == filterMaChuyenBay
        
        matchesSearch && matchesHangVe && matchesFlight
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Quản Lý Giá Vé") },
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
                    IconButton(onClick = { loadFarePrices() }) {
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
                Icon(Icons.Default.Add, contentDescription = "Add Fare Price")
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
                    placeholder = { Text("Tìm kiếm giá vé...") },
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
                    
                    // Fare class filter
                    var hangVeExpanded by remember { mutableStateOf(false) }
                    val hangVeOptions = mapOf(
                        "" to "Tất cả hạng vé",
                        "pho_thong" to "Phổ thông",
                        "thuong_gia" to "Thương gia",
                        "hang_nhat" to "Hạng nhất"
                    )
                    ExposedDropdownMenuBox(
                        expanded = hangVeExpanded,
                        onExpandedChange = { hangVeExpanded = !hangVeExpanded },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = hangVeOptions[filterHangVe] ?: "Tất cả hạng vé",
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = hangVeExpanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = hangVeExpanded,
                            onDismissRequest = { hangVeExpanded = false }
                        ) {
                            hangVeOptions.forEach { (key, value) ->
                                DropdownMenuItem(
                                    text = { Text(value) },
                                    onClick = {
                                        filterHangVe = key
                                        hangVeExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
                
                // Content
                when {
                    loading && farePrices.isEmpty() -> {
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
                                    text = "Đang tải danh sách giá vé...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                    
                    error != null -> {
                        FarePriceErrorView(
                            message = error!!,
                            onRetry = { loadFarePrices() }
                        )
                    }
                    
                    filteredFarePrices.isEmpty() -> {
                        EmptyFarePriceView(
                            hasSearch = searchTerm.isNotEmpty() || filterHangVe.isNotEmpty() || filterMaChuyenBay != null,
                            onAddClick = { openDialog() }
                        )
                    }
                    
                    else -> {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(filteredFarePrices) { farePrice ->
                                FarePriceCard(
                                    farePrice = farePrice,
                                    displayDateFormat = displayDateFormat,
                                    onEdit = { openDialog(farePrice) },
                                    onDelete = { handleDelete(farePrice.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
        
        // Dialog for Add/Edit
        if (showDialog) {
            FarePriceDialog(
                farePrice = editingFarePrice,
                flights = flights,
                maChuyenBay = maChuyenBay,
                hangVe = hangVe,
                gia = gia,
                hanhLyKyGui = hanhLyKyGui,
                chinhSachHuyVe = chinhSachHuyVe,
                chinhSachDoiVe = chinhSachDoiVe,
                ngayBatDau = ngayBatDau,
                ngayKetThuc = ngayKetThuc,
                formErrors = formErrors,
                submitting = submitting,
                onMaChuyenBayChange = { maChuyenBay = it },
                onHangVeChange = { hangVe = it },
                onGiaChange = { gia = it },
                onHanhLyKyGuiChange = { hanhLyKyGui = it },
                onChinhSachHuyVeChange = { chinhSachHuyVe = it },
                onChinhSachDoiVeChange = { chinhSachDoiVe = it },
                onNgayBatDauChange = { ngayBatDau = it },
                onNgayKetThucChange = { ngayKetThuc = it },
                onSubmit = { handleSubmit() },
                onDismiss = { closeDialog() }
            )
        }
        
        // Delete Confirmation Dialog
        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Xác nhận xóa") },
                text = { Text("Bạn có chắc chắn muốn xóa giá vé này không?") },
                confirmButton = {
                    Button(
                        onClick = { confirmDelete() },
                        colors = ButtonDefaults.buttonColors(containerColor = ErrorRed),
                        enabled = !submitting
                    ) {
                        Text("Xóa", color = Color.White)
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = { showDeleteDialog = false },
                        enabled = !submitting
                    ) {
                        Text("Hủy")
                    }
                }
            )
        }
    }
}

@Composable
fun FarePriceCard(
    farePrice: AirlineFarePrice,
    displayDateFormat: SimpleDateFormat,
    onEdit: () -> Unit,
    onDelete: () -> Unit
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
                        text = farePrice.chuyen_bay?.ma_chuyen_bay ?: "N/A",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = PrimaryBlue
                    )
                    Text(
                        text = "${farePrice.chuyen_bay?.tuyen_bay?.san_bay_di?.ten_san_bay ?: "N/A"} → ${farePrice.chuyen_bay?.tuyen_bay?.san_bay_den?.ten_san_bay ?: "N/A"}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
                Badge(
                    containerColor = when (farePrice.hang_ve) {
                        "pho_thong" -> PrimaryBlue
                        "thuong_gia" -> Color(0xFFFF9800)
                        "hang_nhat" -> Color(0xFF9C27B0)
                        else -> TextSecondary
                    },
                    contentColor = Color.White
                ) {
                    Text(
                        text = when (farePrice.hang_ve) {
                            "pho_thong" -> "Phổ thông"
                            "thuong_gia" -> "Thương gia"
                            "hang_nhat" -> "Hạng nhất"
                            else -> farePrice.hang_ve
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
                    Text("Giá vé:", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    Text(
                        text = "${String.format("%,.0f", farePrice.gia)} VNĐ",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = PrimaryBlue
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Hành lý:", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    Text(
                        text = farePrice.hanh_ly_ky_gui,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary
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
                    Text("Ngày bắt đầu:", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    Text(
                        text = try {
                            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(farePrice.ngay_bat_dau)?.let {
                                displayDateFormat.format(it)
                            } ?: farePrice.ngay_bat_dau
                        } catch (e: Exception) {
                            farePrice.ngay_bat_dau
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Ngày kết thúc:", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    Text(
                        text = try {
                            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(farePrice.ngay_ket_thuc)?.let {
                                displayDateFormat.format(it)
                            } ?: farePrice.ngay_ket_thuc
                        } catch (e: Exception) {
                            farePrice.ngay_ket_thuc
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary
                    )
                }
            }
            if (farePrice.chinh_sach_huy_ve != null || farePrice.chinh_sach_doi_ve != null) {
                Spacer(modifier = Modifier.height(8.dp))
                if (farePrice.chinh_sach_huy_ve != null) {
                    Text(
                        text = "Chính sách hủy: ${farePrice.chinh_sach_huy_ve}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
                if (farePrice.chinh_sach_doi_ve != null) {
                    Text(
                        text = "Chính sách đổi: ${farePrice.chinh_sach_doi_ve}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(PrimaryBlue.copy(alpha = 0.1f))
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = PrimaryBlue)
                }
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(ErrorRed.copy(alpha = 0.1f))
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = ErrorRed)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FarePriceDialog(
    farePrice: AirlineFarePrice?,
    flights: List<AirlineFlight>,
    maChuyenBay: Int?,
    hangVe: String,
    gia: String,
    hanhLyKyGui: String,
    chinhSachHuyVe: String,
    chinhSachDoiVe: String,
    ngayBatDau: String,
    ngayKetThuc: String,
    formErrors: Map<String, String>,
    submitting: Boolean,
    onMaChuyenBayChange: (Int?) -> Unit,
    onHangVeChange: (String) -> Unit,
    onGiaChange: (String) -> Unit,
    onHanhLyKyGuiChange: (String) -> Unit,
    onChinhSachHuyVeChange: (String) -> Unit,
    onChinhSachDoiVeChange: (String) -> Unit,
    onNgayBatDauChange: (String) -> Unit,
    onNgayKetThucChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
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
                        if (farePrice != null) "Sửa giá vé" else "Thêm giá vé mới",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }
                
                // Flight dropdown
                var flightExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = flightExpanded,
                    onExpandedChange = { flightExpanded = !flightExpanded }
                ) {
                    OutlinedTextField(
                        value = flights.find { it.id == maChuyenBay }?.let {
                            "${it.ma_chuyen_bay} - ${it.tuyen_bay?.san_bay_di?.ma_san_bay} → ${it.tuyen_bay?.san_bay_den?.ma_san_bay}"
                        } ?: "",
                        onValueChange = { },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        label = { Text("Chuyến Bay *") },
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = flightExpanded) },
                        isError = formErrors.containsKey("ma_chuyen_bay"),
                        supportingText = {
                            if (formErrors.containsKey("ma_chuyen_bay")) {
                                Text(formErrors["ma_chuyen_bay"] ?: "", color = ErrorRed)
                            }
                        },
                        shape = RoundedCornerShape(8.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = flightExpanded,
                        onDismissRequest = { flightExpanded = false }
                    ) {
                        flights.forEach { flight ->
                            DropdownMenuItem(
                                text = { Text("${flight.ma_chuyen_bay} - ${flight.tuyen_bay?.san_bay_di?.ma_san_bay} → ${flight.tuyen_bay?.san_bay_den?.ma_san_bay}") },
                                onClick = {
                                    onMaChuyenBayChange(flight.id)
                                    flightExpanded = false
                                }
                            )
                        }
                    }
                }
                
                // Fare class dropdown
                var hangVeExpanded by remember { mutableStateOf(false) }
                val hangVeOptions = mapOf(
                    "pho_thong" to "Phổ thông",
                    "thuong_gia" to "Thương gia",
                    "hang_nhat" to "Hạng nhất"
                )
                ExposedDropdownMenuBox(
                    expanded = hangVeExpanded,
                    onExpandedChange = { hangVeExpanded = !hangVeExpanded }
                ) {
                    OutlinedTextField(
                        value = hangVeOptions[hangVe] ?: "Phổ thông",
                        onValueChange = { },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        label = { Text("Hạng Vé *") },
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = hangVeExpanded) },
                        shape = RoundedCornerShape(8.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = hangVeExpanded,
                        onDismissRequest = { hangVeExpanded = false }
                    ) {
                        hangVeOptions.forEach { (key, value) ->
                            DropdownMenuItem(
                                text = { Text(value) },
                                onClick = {
                                    onHangVeChange(key)
                                    hangVeExpanded = false
                                }
                            )
                        }
                    }
                }
                
                // Price
                OutlinedTextField(
                    value = gia,
                    onValueChange = onGiaChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Giá Vé (VNĐ) *") },
                    singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = formErrors.containsKey("gia"),
                    supportingText = {
                        if (formErrors.containsKey("gia")) {
                            Text(formErrors["gia"] ?: "", color = ErrorRed)
                        }
                    },
                    shape = RoundedCornerShape(8.dp)
                )
                
                // Baggage
                OutlinedTextField(
                    value = hanhLyKyGui,
                    onValueChange = onHanhLyKyGuiChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Hành Lý Ký Gửi *") },
                    singleLine = true,
                    isError = formErrors.containsKey("hanh_ly_ky_gui"),
                    supportingText = {
                        if (formErrors.containsKey("hanh_ly_ky_gui")) {
                            Text(formErrors["hanh_ly_ky_gui"] ?: "", color = ErrorRed)
                        }
                    },
                    shape = RoundedCornerShape(8.dp)
                )
                
                // Cancel policy
                OutlinedTextField(
                    value = chinhSachHuyVe,
                    onValueChange = onChinhSachHuyVeChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Chính Sách Hủy Vé") },
                    maxLines = 3,
                    shape = RoundedCornerShape(8.dp)
                )
                
                // Change policy
                OutlinedTextField(
                    value = chinhSachDoiVe,
                    onValueChange = onChinhSachDoiVeChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Chính Sách Đổi Vé") },
                    maxLines = 3,
                    shape = RoundedCornerShape(8.dp)
                )
                
                // Start date - using simple text field for date (YYYY-MM-DD format)
                OutlinedTextField(
                    value = ngayBatDau,
                    onValueChange = onNgayBatDauChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Ngày Bắt Đầu (YYYY-MM-DD) *") },
                    placeholder = { Text("2025-01-01") },
                    singleLine = true,
                    isError = formErrors.containsKey("ngay_bat_dau"),
                    supportingText = {
                        if (formErrors.containsKey("ngay_bat_dau")) {
                            Text(formErrors["ngay_bat_dau"] ?: "", color = ErrorRed)
                        } else {
                            Text("Format: YYYY-MM-DD", fontSize = 10.sp, color = TextSecondary)
                        }
                    },
                    shape = RoundedCornerShape(8.dp)
                )
                
                // End date
                OutlinedTextField(
                    value = ngayKetThuc,
                    onValueChange = onNgayKetThucChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Ngày Kết Thúc (YYYY-MM-DD) *") },
                    placeholder = { Text("2025-12-31") },
                    singleLine = true,
                    isError = formErrors.containsKey("ngay_ket_thuc"),
                    supportingText = {
                        if (formErrors.containsKey("ngay_ket_thuc")) {
                            Text(formErrors["ngay_ket_thuc"] ?: "", color = ErrorRed)
                        } else {
                            Text("Format: YYYY-MM-DD", fontSize = 10.sp, color = TextSecondary)
                        }
                    },
                    shape = RoundedCornerShape(8.dp)
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
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
                            Text(if (farePrice != null) "Cập nhật" else "Tạo mới")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FarePriceErrorView(message: String, onRetry: () -> Unit) {
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
fun EmptyFarePriceView(hasSearch: Boolean, onAddClick: () -> Unit) {
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
            text = if (hasSearch) "Không tìm thấy giá vé nào" else "Chưa có giá vé nào",
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary
        )
        Text(
            text = if (hasSearch) "Thử tìm kiếm với từ khóa hoặc bộ lọc khác" else "Bắt đầu bằng cách thêm giá vé mới",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            modifier = Modifier.padding(top = 4.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        if (!hasSearch) {
            Button(onClick = onAddClick, colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)) {
                Text("Thêm giá vé đầu tiên", color = Color.White)
            }
        }
    }
}

