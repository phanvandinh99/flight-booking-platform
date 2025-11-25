package com.example.flybook.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.example.flybook.data.models.SystemConfig
import com.example.flybook.data.repository.ConfigRepository
import com.example.flybook.ui.components.BottomNavigationBar
import com.example.flybook.ui.components.adminBottomNavItems
import com.example.flybook.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SystemConfigScreen(navController: NavController) {
    val repository = remember { ConfigRepository() }
    val scope = rememberCoroutineScope()
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var configs by remember { mutableStateOf<List<SystemConfig>>(emptyList()) }
    var searchTerm by remember { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) }
    var editingConfig by remember { mutableStateOf<SystemConfig?>(null) }
    var tenCauHinh by remember { mutableStateOf("") }
    var giaTri by remember { mutableStateOf("") }
    var formErrors by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var submitting by remember { mutableStateOf(false) }
    
    fun loadConfigs() {
        scope.launch {
            loading = true
            error = null
            val result = repository.getConfigs()
            result.onSuccess { 
                configs = it
                error = null
            }.onFailure { 
                error = it.message
            }
            loading = false
        }
    }
    
    LaunchedEffect(Unit) {
        loadConfigs()
    }
    
    fun openDialog(config: SystemConfig? = null) {
        editingConfig = config
        if (config != null) {
            tenCauHinh = config.ten_cau_hinh
            giaTri = config.gia_tri
        } else {
            tenCauHinh = ""
            giaTri = ""
        }
        formErrors = emptyMap()
        showDialog = true
    }
    
    fun closeDialog() {
        showDialog = false
        editingConfig = null
        tenCauHinh = ""
        giaTri = ""
        formErrors = emptyMap()
    }
    
    fun validateForm(): Boolean {
        val errors = mutableMapOf<String, String>()
        if (tenCauHinh.trim().isEmpty()) {
            errors["ten_cau_hinh"] = "Tên cấu hình là bắt buộc"
        } else if (tenCauHinh.length > 255) {
            errors["ten_cau_hinh"] = "Tên cấu hình không được quá 255 ký tự"
        }
        if (giaTri.trim().isEmpty()) {
            errors["gia_tri"] = "Giá trị là bắt buộc"
        }
        formErrors = errors
        return errors.isEmpty()
    }
    
    fun handleSubmit() {
        if (!validateForm()) return
        
        scope.launch {
            submitting = true
            val result = if (editingConfig != null) {
                repository.updateConfig(editingConfig!!.ten_cau_hinh, giaTri)
            } else {
                repository.createConfig(tenCauHinh, giaTri)
            }
            
            result.onSuccess {
                loadConfigs()
                closeDialog()
            }.onFailure {
                error = it.message
            }
            submitting = false
        }
    }
    
    fun handleDelete(key: String) {
        scope.launch {
            val result = repository.deleteConfig(key)
            result.onSuccess {
                loadConfigs()
            }.onFailure {
                error = it.message
            }
        }
    }
    
    val filteredConfigs = configs.filter {
        it.ten_cau_hinh.contains(searchTerm, ignoreCase = true) ||
        it.gia_tri.contains(searchTerm, ignoreCase = true)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cấu Hình Hệ Thống") },
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
                    IconButton(onClick = { loadConfigs() }) {
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
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { openDialog() },
                containerColor = PrimaryBlue,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Config")
            }
        }
    ) { paddingValues ->
        if (loading && configs.isEmpty()) {
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
                    Text("Đang tải danh sách cấu hình...", color = TextSecondary)
                }
            }
        } else if (error != null && configs.isEmpty()) {
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
                        onClick = { loadConfigs() },
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
                    placeholder = { Text("Tìm kiếm cấu hình...") },
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
                
                // Configs list
                if (filteredConfigs.isEmpty()) {
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
                                Icons.Default.Settings,
                                contentDescription = "Empty",
                                modifier = Modifier.size(64.dp),
                                tint = TextSecondary
                            )
                            Text(
                                if (searchTerm.isNotEmpty()) "Không tìm thấy cấu hình" else "Không có cấu hình nào",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                            Text(
                                if (searchTerm.isNotEmpty()) "Thử tìm kiếm với từ khóa khác" else "Bắt đầu bằng cách thêm cấu hình mới",
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
                                Text("Tên Cấu Hình", fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.weight(1f))
                                Text("Giá Trị", fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.weight(1f))
                                Text("Thao Tác", fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.width(100.dp))
                            }
                            
                            // Rows
                            filteredConfigs.forEach { config ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(BackgroundLight, RoundedCornerShape(8.dp))
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        config.ten_cau_hinh,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        config.gia_tri,
                                        fontSize = 12.sp,
                                        modifier = Modifier.weight(1f),
                                        color = TextSecondary
                                    )
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.width(100.dp)
                                    ) {
                                        IconButton(
                                            onClick = { openDialog(config) },
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
                                            onClick = { handleDelete(config.ten_cau_hinh) },
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
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            if (editingConfig != null) "Sửa cấu hình" else "Thêm cấu hình mới",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        IconButton(onClick = { closeDialog() }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }
                    
                    OutlinedTextField(
                        value = tenCauHinh,
                        onValueChange = { tenCauHinh = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Tên Cấu Hình *") },
                        placeholder = { Text("VD: thue, phi_dich_vu, ...") },
                        enabled = editingConfig == null,
                        singleLine = true,
                        isError = formErrors.containsKey("ten_cau_hinh"),
                        supportingText = {
                            if (formErrors.containsKey("ten_cau_hinh")) {
                                Text(formErrors["ten_cau_hinh"] ?: "", color = ErrorRed)
                            } else if (editingConfig != null) {
                                Text("Không thể thay đổi tên cấu hình khi chỉnh sửa", fontSize = 10.sp, color = TextSecondary)
                            }
                        },
                        shape = RoundedCornerShape(8.dp)
                    )
                    
                    OutlinedTextField(
                        value = giaTri,
                        onValueChange = { giaTri = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Giá Trị *") },
                        placeholder = { Text("VD: 0.1, 100000, true, ...") },
                        singleLine = true,
                        isError = formErrors.containsKey("gia_tri"),
                        supportingText = {
                            if (formErrors.containsKey("gia_tri")) {
                                Text(formErrors["gia_tri"] ?: "", color = ErrorRed)
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
                                Text(if (editingConfig != null) "Cập nhật" else "Tạo mới")
                            }
                        }
                    }
                }
            }
        }
    }
}

