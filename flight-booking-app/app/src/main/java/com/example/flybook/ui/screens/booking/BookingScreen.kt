package com.example.flybook.ui.screens.booking

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
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import android.content.Context
import androidx.compose.ui.platform.LocalContext
import com.example.flybook.data.models.*
import com.example.flybook.data.repository.AuthRepository
import com.example.flybook.data.repository.BookingRepository
import com.example.flybook.data.repository.CustomerFlightRepository
import com.example.flybook.navigation.Screen
import com.example.flybook.ui.components.BottomNavigationBar
import com.example.flybook.ui.components.customerBottomNavItems
import com.example.flybook.ui.components.formatCurrency
import com.example.flybook.ui.components.formatTime
import com.example.flybook.ui.theme.*
import com.example.flybook.util.AuthManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingScreen(
    navController: NavController,
    flightId: Int? = null
) {
    val context = LocalContext.current
    val flightRepository = remember { CustomerFlightRepository() }
    val bookingRepository = remember { BookingRepository() }
    val authRepository = remember { AuthRepository() }
    val scope = rememberCoroutineScope()
    
    var isLoading by remember { mutableStateOf(true) }
    var isSubmitting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var flight by remember { mutableStateOf<Flight?>(null) }
    var selectedFareClass by remember { mutableStateOf<Price?>(null) }
    var currentUser by remember { mutableStateOf<User?>(null) }
    
    // Passenger form state
    var passengers by remember { 
        mutableStateOf(listOf(
            BookingPassengerData(
                ho_ten = "",
                so_ho_chieu = "",
                loai_giay_to = "ho_chieu",
                so_giay_to = "",
                so_ghe = "",
                loai_hanh_khach = "nguoi_lon"
            )
        ))
    }
    
    // Contact info state
    var contactInfo by remember { 
        mutableStateOf(ContactInfoData(
            email = "",
            so_dien_thoai = "",
            ten_day_du = ""
        ))
    }
    
    // Check authentication
    LaunchedEffect(Unit) {
        scope.launch {
            // Load token first
            AuthManager.loadToken(context)
            
            // Get current user
            authRepository.getMe()
                .onSuccess { user ->
                    currentUser = user
                    if (user.vai_tro != "khach_hang") {
                        errorMessage = "Bạn không có quyền đặt vé. Vui lòng đăng nhập bằng tài khoản khách hàng."
                        kotlinx.coroutines.delay(1500)
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Home.route)
                        }
                        return@onSuccess
                    }
                    
                    // Set contact info from user
                    contactInfo = ContactInfoData(
                        email = user.email ?: "",
                        so_dien_thoai = "",
                        ten_day_du = user.ten_day_du ?: ""
                    )
                }
                .onFailure {
                    errorMessage = "Vui lòng đăng nhập để đặt vé"
                    kotlinx.coroutines.delay(1500)
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Home.route)
                    }
                    return@onFailure
                }
        
            // Load flight detail
            val id = flightId ?: run {
                errorMessage = "Thông tin chuyến bay không hợp lệ"
                isLoading = false
                return@launch
            }
            
            isLoading = true
            flightRepository.getFlightDetail(id)
                .onSuccess { 
                    flight = it
                    // Auto-select first fare class
                    if (it.gia_ve != null && it.gia_ve.isNotEmpty()) {
                        selectedFareClass = it.gia_ve.first()
                    }
                }
                .onFailure { 
                    errorMessage = it.message ?: "Không thể tải thông tin chuyến bay"
                }
            isLoading = false
        }
    }
    
    fun calculatePassengerPrice(passenger: BookingPassengerData): Double {
        val basePrice = selectedFareClass?.gia ?: 0.0
        return when (passenger.loai_hanh_khach) {
            "nguoi_lon" -> basePrice
            "tre_em" -> basePrice * 0.75
            "em_be" -> basePrice * 0.1
            else -> basePrice
        }
    }
    
    fun calculateTotalPrice(): Double {
        return passengers.sumOf { calculatePassengerPrice(it) }
    }
    
    fun validateForm(): String? {
        if (selectedFareClass == null) {
            return "Vui lòng chọn hạng vé"
        }
        
        if (contactInfo.email.isEmpty() || contactInfo.so_dien_thoai.isEmpty() || contactInfo.ten_day_du.isEmpty()) {
            return "Vui lòng điền đầy đủ thông tin liên hệ"
        }
        
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(contactInfo.email).matches()) {
            return "Email không hợp lệ"
        }
        
        if (contactInfo.so_dien_thoai.length < 10) {
            return "Số điện thoại phải có ít nhất 10 số"
        }
        
        passengers.forEachIndexed { index, passenger ->
            if (passenger.ho_ten.trim().isEmpty()) {
                return "Vui lòng nhập họ tên hành khách ${index + 1}"
            }
            if (passenger.loai_hanh_khach == "nguoi_lon" && passenger.so_giay_to.trim().isEmpty()) {
                return "Vui lòng nhập số giấy tờ cho hành khách ${index + 1}"
            }
        }
        
        return null
    }
    
    fun handleSubmit() {
        val validationError = validateForm()
        if (validationError != null) {
            errorMessage = validationError
            return
        }
        
        if (flight == null || selectedFareClass == null) {
            errorMessage = "Thông tin chuyến bay không hợp lệ"
            return
        }
        
        scope.launch {
            isSubmitting = true
            errorMessage = null
            
            val bookingRequest = CreateBookingRequest(
                ma_chuyen_bay_di = flight!!.id,
                ma_chuyen_bay_ve = null,
                hang_ve = selectedFareClass!!.hang_ve,
                hanh_khach = passengers.map { 
                    BookingPassenger(
                        ho_ten = it.ho_ten,
                        so_ho_chieu = it.so_ho_chieu.takeIf { s -> s.isNotEmpty() },
                        loai_giay_to = it.loai_giay_to,
                        so_giay_to = it.so_giay_to.takeIf { s -> s.isNotEmpty() },
                        so_ghe = it.so_ghe.takeIf { s -> s.isNotEmpty() },
                        loai_hanh_khach = it.loai_hanh_khach
                    )
                },
                thong_tin_lien_he = ContactInfo(
                    email = contactInfo.email,
                    so_dien_thoai = contactInfo.so_dien_thoai,
                    ten_day_du = contactInfo.ten_day_du
                )
            )
            
            bookingRepository.createBooking(bookingRequest)
                .onSuccess {
                    navController.navigate(Screen.MyBookings.route) {
                        popUpTo(Screen.Home.route) { inclusive = false }
                    }
                }
                .onFailure { e ->
                    errorMessage = e.message ?: "Có lỗi xảy ra khi đặt vé. Vui lòng thử lại."
                }
            
            isSubmitting = false
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Đặt vé") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryBlue,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        bottomBar = {
            BottomNavigationBar(
                navController = navController,
                items = customerBottomNavItems
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (errorMessage != null && flight == null) {
            Card(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Lỗi",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = errorMessage ?: "",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Button(onClick = { navController.popBackStack() }) {
                        Text("Quay lại")
                    }
                }
            }
        } else if (flight != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Flight Summary
                FlightSummaryCard(flight = flight!!)
                
                // Fare Class Selection
                if (flight!!.gia_ve != null && flight!!.gia_ve!!.size > 1) {
                    FareClassSelection(
                        fareClasses = flight!!.gia_ve!!,
                        selectedFareClass = selectedFareClass,
                        onFareClassSelected = { selectedFareClass = it }
                    )
                }
                
                // Passengers Section
                PassengersSection(
                    passengers = passengers,
                    onPassengersChange = { passengers = it },
                    selectedFareClass = selectedFareClass,
                    calculatePrice = { calculatePassengerPrice(it) }
                )
                
                // Contact Info Section
                ContactInfoSection(
                    contactInfo = contactInfo,
                    onContactInfoChange = { contactInfo = it }
                )
                
                // Error Message
                if (errorMessage != null) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = ErrorRed.copy(alpha = 0.1f)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = null,
                                tint = ErrorRed
                            )
                            Text(
                                text = errorMessage ?: "",
                                color = ErrorRed,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
                
                // Submit Button
                Button(
                    onClick = { handleSubmit() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = !isSubmitting,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryBlue
                    )
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White
                        )
                    } else {
                        Text(
                            text = "Đặt vé - ${formatCurrency(calculateTotalPrice())}",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }
        }
    }
}

data class BookingPassengerData(
    var ho_ten: String,
    var so_ho_chieu: String,
    var loai_giay_to: String,
    var so_giay_to: String,
    var so_ghe: String,
    var loai_hanh_khach: String
)

data class ContactInfoData(
    var email: String,
    var so_dien_thoai: String,
    var ten_day_du: String
)

@Composable
fun FlightSummaryCard(flight: Flight) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Thông tin chuyến bay",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold
                )
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    val departureAirport = flight.tuyen_bay?.san_bay_di
                    Text(
                        text = if (departureAirport != null) {
                            "${departureAirport.ma_san_bay} - ${departureAirport.ten_san_bay}"
                        } else {
                            "N/A"
                        },
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = formatTime(flight.gio_khoi_hanh),
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextSecondary
                    )
                }
                
                Text(
                    text = "→",
                    style = MaterialTheme.typography.headlineLarge,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                
                Column(
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier.weight(1f)
                ) {
                    val arrivalAirport = flight.tuyen_bay?.san_bay_den
                    Text(
                        text = if (arrivalAirport != null) {
                            "${arrivalAirport.ma_san_bay} - ${arrivalAirport.ten_san_bay}"
                        } else {
                            "N/A"
                        },
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        textAlign = TextAlign.End
                    )
                    Text(
                        text = formatTime(flight.gio_ha_canh),
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextSecondary,
                        textAlign = TextAlign.End
                    )
                }
            }
            
            Divider()
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Hãng hàng không:",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = flight.hang_hang_khong?.ten_hang ?: "N/A",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FareClassSelection(
    fareClasses: List<Price>,
    selectedFareClass: Price?,
    onFareClassSelected: (Price) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Chọn hạng vé",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold
            )
        )
        
        fareClasses.forEach { fareClass ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                onClick = { onFareClassSelected(fareClass) },
                colors = CardDefaults.cardColors(
                    containerColor = if (selectedFareClass?.id == fareClass.id) {
                        PrimaryBlue.copy(alpha = 0.1f)
                    } else {
                        MaterialTheme.colorScheme.surface
                    }
                ),
                border = if (selectedFareClass?.id == fareClass.id) {
                    BorderStroke(2.dp, PrimaryBlue)
                } else null
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
                            text = when (fareClass.hang_ve) {
                                "pho_thong" -> "Phổ thông"
                                "thuong_gia" -> "Thương gia"
                                "hang_nhat" -> "Hạng nhất"
                                else -> fareClass.hang_ve
                            },
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                        if (fareClass.so_ghe_trong != null) {
                            Text(
                                text = "${fareClass.so_ghe_trong} ghế trống",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }
                    Text(
                        text = formatCurrency(fareClass.gia),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = PrimaryBlue
                        )
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PassengersSection(
    passengers: List<BookingPassengerData>,
    onPassengersChange: (List<BookingPassengerData>) -> Unit,
    selectedFareClass: Price?,
    calculatePrice: (BookingPassengerData) -> Double
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Thông tin hành khách",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold
                )
            )
            IconButton(onClick = {
                onPassengersChange(passengers + BookingPassengerData(
                    ho_ten = "",
                    so_ho_chieu = "",
                    loai_giay_to = "ho_chieu",
                    so_giay_to = "",
                    so_ghe = "",
                    loai_hanh_khach = "nguoi_lon"
                ))
            }) {
                Icon(Icons.Default.Add, contentDescription = "Thêm hành khách")
            }
        }
        
        passengers.forEachIndexed { index, passenger ->
            PassengerCard(
                passenger = passenger,
                index = index,
                onPassengerChange = { updated ->
                    val updatedList = passengers.toMutableList()
                    updatedList[index] = updated
                    onPassengersChange(updatedList)
                },
                onRemove = if (passengers.size > 1) {
                    { onPassengersChange(passengers.filterIndexed { i, _ -> i != index }) }
                } else null,
                selectedFareClass = selectedFareClass,
                calculatePrice = calculatePrice
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PassengerCard(
    passenger: BookingPassengerData,
    index: Int,
    onPassengerChange: (BookingPassengerData) -> Unit,
    onRemove: (() -> Unit)?,
    selectedFareClass: Price?,
    calculatePrice: (BookingPassengerData) -> Double
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Hành khách ${index + 1}",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
                if (onRemove != null) {
                    IconButton(onClick = onRemove) {
                        Icon(Icons.Default.Close, contentDescription = "Xóa")
                    }
                }
            }
            
            OutlinedTextField(
                value = passenger.ho_ten,
                onValueChange = { onPassengerChange(passenger.copy(ho_ten = it)) },
                label = { Text("Họ và tên *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                var expanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = when (passenger.loai_hanh_khach) {
                            "nguoi_lon" -> "Người lớn (12+)"
                            "tre_em" -> "Trẻ em (2-11)"
                            "em_be" -> "Em bé (<2)"
                            else -> passenger.loai_hanh_khach
                        },
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Loại hành khách") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Người lớn (12+)") },
                            onClick = {
                                onPassengerChange(passenger.copy(loai_hanh_khach = "nguoi_lon"))
                                expanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Trẻ em (2-11)") },
                            onClick = {
                                onPassengerChange(passenger.copy(loai_hanh_khach = "tre_em"))
                                expanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Em bé (<2)") },
                            onClick = {
                                onPassengerChange(passenger.copy(loai_hanh_khach = "em_be"))
                                expanded = false
                            }
                        )
                    }
                }
                
                var fareExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = fareExpanded,
                    onExpandedChange = { fareExpanded = !fareExpanded },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = when (passenger.loai_giay_to) {
                            "ho_chieu" -> "Hộ chiếu"
                            "can_cuoc" -> "Căn cước"
                            else -> passenger.loai_giay_to
                        },
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Loại giấy tờ") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = fareExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = fareExpanded,
                        onDismissRequest = { fareExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Hộ chiếu") },
                            onClick = {
                                onPassengerChange(passenger.copy(loai_giay_to = "ho_chieu"))
                                fareExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Căn cước") },
                            onClick = {
                                onPassengerChange(passenger.copy(loai_giay_to = "can_cuoc"))
                                fareExpanded = false
                            }
                        )
                    }
                }
            }
            
            OutlinedTextField(
                value = passenger.so_giay_to,
                onValueChange = { onPassengerChange(passenger.copy(so_giay_to = it)) },
                label = { 
                    Text(if (passenger.loai_hanh_khach == "nguoi_lon") "Số giấy tờ *" else "Số giấy tờ")
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Text)
            )
            
            OutlinedTextField(
                value = passenger.so_ghe,
                onValueChange = { onPassengerChange(passenger.copy(so_ghe = it)) },
                label = { Text("Số ghế (tùy chọn)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("Ví dụ: 12A") }
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Giá vé:",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = formatCurrency(calculatePrice(passenger)),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = PrimaryBlue
                    )
                )
            }
        }
    }
}

@Composable
fun ContactInfoSection(
    contactInfo: ContactInfoData,
    onContactInfoChange: (ContactInfoData) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Thông tin liên hệ",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold
            )
        )
        
        OutlinedTextField(
            value = contactInfo.email,
            onValueChange = { onContactInfoChange(contactInfo.copy(email = it)) },
            label = { Text("Email *") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Email)
        )
        
        OutlinedTextField(
            value = contactInfo.so_dien_thoai,
            onValueChange = { onContactInfoChange(contactInfo.copy(so_dien_thoai = it)) },
            label = { Text("Số điện thoại *") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Phone)
        )
        
        OutlinedTextField(
            value = contactInfo.ten_day_du,
            onValueChange = { onContactInfoChange(contactInfo.copy(ten_day_du = it)) },
            label = { Text("Tên đầy đủ *") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
    }
}
