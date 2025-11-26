package com.example.flybook.ui.screens.home

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.Context
import android.util.Log
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.example.flybook.data.repository.AuthRepository
import com.example.flybook.util.AuthManager
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.flybook.data.models.Airport
import com.example.flybook.data.models.Flight
import com.example.flybook.data.models.User
import com.example.flybook.data.repository.CustomerFlightRepository
import com.example.flybook.navigation.Screen
import com.example.flybook.ui.components.BottomNavigationBar
import com.example.flybook.ui.components.customerBottomNavItems
import com.example.flybook.ui.components.formatCurrency
import com.example.flybook.ui.components.formatTime
import com.example.flybook.ui.components.DatePickerDialog as CustomDatePickerDialog
import com.example.flybook.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    val context = LocalContext.current
    val flightRepository = remember { CustomerFlightRepository() }
    val authRepository = remember { AuthRepository() }
    val scope = rememberCoroutineScope()
    
    var currentUser by remember { mutableStateOf<User?>(null) }
    var isLoadingUser by remember { mutableStateOf(true) }
    var userLoadError by remember { mutableStateOf<String?>(null) }
    
    var airports by remember { mutableStateOf<List<Airport>>(emptyList()) }
    var todayFlights by remember { mutableStateOf<List<Flight>>(emptyList()) }
    var isLoadingAirports by remember { mutableStateOf(false) }
    var isLoadingFlights by remember { mutableStateOf(true) }
    var currentBanner by remember { mutableStateOf(0) }
    
    // Search form state
    var tripType by remember { mutableStateOf("mot_chieu") }
    var departureAirport by remember { mutableStateOf<Airport?>(null) }
    var arrivalAirport by remember { mutableStateOf<Airport?>(null) }
    var departureDate by remember { mutableStateOf("") }
    var returnDate by remember { mutableStateOf("") }
    var adults by remember { mutableStateOf(1) }
    var children by remember { mutableStateOf(0) }
    var infants by remember { mutableStateOf(0) }
    var fareClass by remember { mutableStateOf("") }
    var showPassengerSelector by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showReturnDatePicker by remember { mutableStateOf(false) }
    var errors by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    
    val banners = listOf(
        BannerData("Khám phá thế giới", "Đặt vé ngay hôm nay", "baner1.jpg", listOf(PrimaryBlue, PrimaryPurple)),
        BannerData("Giá vé tốt nhất", "So sánh và tiết kiệm", "baner2.jpg", listOf(Color(0xFF4FACFE), Color(0xFF00F2FE))),
        BannerData("Bay an toàn", "Dịch vụ hàng đầu", "baner1.jpg", listOf(Color(0xFF43E97B), Color(0xFF38F9D7))),
        BannerData("Trải nghiệm tuyệt vời", "Hành trình đáng nhớ", "baner3.jpg", listOf(Color(0xFFF093FB), Color(0xFFF5576C)))
    )
    
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val isHomeScreen = navBackStackEntry?.destination?.route == Screen.Home.route
    
    // Reload user when screen is focused or when coming back from login
    LaunchedEffect(navBackStackEntry?.id, isHomeScreen) {
        // Load token first
        scope.launch {
            try {
                Log.d("HomeScreen", "Loading token...")
                AuthManager.loadToken(context)
                Log.d("HomeScreen", "Token loaded, calling getMe()...")
                
                // Try to get current user
                authRepository.getMe()
                    .onSuccess { user ->
                        currentUser = user
                        userLoadError = null
                        Log.d("HomeScreen", "User loaded successfully: email=${user.email}, role=${user.vai_tro}, ten_day_du=${user.ten_day_du}")
                    }
                    .onFailure { e ->
                        currentUser = null
                        userLoadError = e.message ?: "Không thể tải thông tin người dùng"
                        Log.e("HomeScreen", "Failed to load user: ${e.message}", e)
                        Log.e("HomeScreen", "Error type: ${e.javaClass.simpleName}")
                    }
            } catch (e: Exception) {
                currentUser = null
                userLoadError = "Lỗi: ${e.message}"
                Log.e("HomeScreen", "Exception loading user: ${e.message}", e)
                Log.e("HomeScreen", "Exception type: ${e.javaClass.simpleName}")
            }
            isLoadingUser = false
        }
        
        scope.launch {
            isLoadingAirports = true
            flightRepository.getAirports()
                .onSuccess { airports = it }
                .onFailure { }
            isLoadingAirports = false
            
            isLoadingFlights = true
            flightRepository.getTodayFlights()
                .onSuccess { todayFlights = it }
                .onFailure { }
            isLoadingFlights = false
        }
        
        // Auto rotate banner
        while (true) {
            delay(5000)
            currentBanner = (currentBanner + 1) % banners.size
        }
    }
    
    fun validateForm(): Boolean {
        val newErrors = mutableMapOf<String, String>()
        
        if (departureAirport == null) {
            newErrors["san_bay_di"] = "Vui lòng chọn sân bay đi"
        }
        if (arrivalAirport == null) {
            newErrors["san_bay_den"] = "Vui lòng chọn sân bay đến"
        }
        if (departureAirport?.id == arrivalAirport?.id && departureAirport != null) {
            newErrors["san_bay_den"] = "Sân bay đến phải khác sân bay đi"
        }
        if (departureDate.isEmpty()) {
            newErrors["ngay_khoi_hanh"] = "Vui lòng chọn ngày khởi hành"
        }
        if (tripType == "khu_hoi" && returnDate.isEmpty()) {
            newErrors["ngay_ve"] = "Vui lòng chọn ngày về"
        }
        if (departureDate.isNotEmpty() && returnDate.isNotEmpty()) {
            try {
                val depDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(departureDate)
                val retDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(returnDate)
                if (retDate != null && depDate != null && retDate.before(depDate)) {
                    newErrors["ngay_ve"] = "Ngày về phải sau ngày đi"
                }
            } catch (e: Exception) { }
        }
        if (adults < 1 || adults > 9) {
            newErrors["nguoi_lon"] = "Số người lớn phải từ 1 đến 9"
        }
        val totalPassengers = adults + children + infants
        if (totalPassengers > 9) {
            newErrors["tong_hanh_khach"] = "Tổng số hành khách không được vượt quá 9 người"
        }
        
        errors = newErrors
        return newErrors.isEmpty()
    }
    
    fun handleSearch() {
        if (!validateForm()) return
        
        val params = buildString {
            append("san_bay_di=${departureAirport?.ma_san_bay}&")
            append("san_bay_den=${arrivalAirport?.ma_san_bay}&")
            append("ngay_khoi_hanh=$departureDate&")
            append("loai_chuyen=$tripType&")
            append("nguoi_lon=$adults")
            if (children > 0) append("&tre_em=$children")
            if (infants > 0) append("&em_be=$infants")
            if (fareClass.isNotEmpty()) append("&hang_ve=$fareClass")
            if (tripType == "khu_hoi" && returnDate.isNotEmpty()) {
                append("&ngay_ve=$returnDate")
            }
        }
        
        navController.navigate("${Screen.FlightSearch.route}?$params")
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = Color.White
                        )
                        Text("Flight Booking")
                    }
                },
                actions = {
                    if (isLoadingUser) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White
                        )
                    } else if (currentUser != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text(
                                text = currentUser?.ten_day_du ?: currentUser?.email ?: "Người dùng",
                                color = Color.White,
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 1
                            )
                            TextButton(
                                onClick = {
                                    scope.launch {
                                        try {
                                            authRepository.logout()
                                        } catch (e: Exception) {
                                            Log.e("HomeScreen", "Logout error: ${e.message}", e)
                                        }
                                        AuthManager.clearToken(context)
                                        currentUser = null
                                        userLoadError = null
                                    }
                                }
                            ) {
                                Text("Đăng xuất", color = Color.White)
                            }
                        }
                    } else {
                        Column(
                            horizontalAlignment = Alignment.End
                        ) {
                            if (userLoadError != null) {
                                Text(
                                    text = userLoadError ?: "",
                                    color = Color.Red.copy(alpha = 0.8f),
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                            }
                            TextButton(onClick = { navController.navigate(Screen.Login.route) }) {
                                Text("Đăng nhập", color = Color.White)
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryBlue,
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundLight)
                .verticalScroll(rememberScrollState())
        ) {
            // Banner Carousel
            BannerCarousel(
                banners = banners,
                currentBanner = currentBanner,
                onBannerSelected = { currentBanner = it },
                onPrevClick = { currentBanner = (currentBanner - 1 + banners.size) % banners.size },
                onNextClick = { currentBanner = (currentBanner + 1) % banners.size }
            )
            
            // Search Form
            SearchFormCard(
                tripType = tripType,
                onTripTypeChange = { 
                    tripType = it
                    if (it == "mot_chieu") returnDate = ""
                },
                airports = airports,
                departureAirport = departureAirport,
                onDepartureAirportChange = { departureAirport = it },
                arrivalAirport = arrivalAirport,
                onArrivalAirportChange = { arrivalAirport = it },
                departureDate = departureDate,
                onDepartureDateChange = { departureDate = it },
                returnDate = returnDate,
                onReturnDateChange = { returnDate = it },
                adults = adults,
                onAdultsChange = { adults = it },
                children = children,
                onChildrenChange = { children = it },
                infants = infants,
                onInfantsChange = { infants = it },
                fareClass = fareClass,
                onFareClassChange = { fareClass = it },
                showPassengerSelector = showPassengerSelector,
                onShowPassengerSelectorChange = { showPassengerSelector = it },
                errors = errors,
                onSearchClick = { handleSearch() },
                isLoading = isLoadingAirports
            )
            
            // Today's Flights Section
            TodayFlightsSection(
                flights = todayFlights,
                isLoading = isLoadingFlights,
                onFlightClick = { flight ->
                    navController.navigate(Screen.FlightDetail.createRoute(flight.id))
                }
            )
            
            // Features Section
            FeaturesSection()
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun BannerCarousel(
    banners: List<BannerData>,
    currentBanner: Int,
    onBannerSelected: (Int) -> Unit,
    onPrevClick: () -> Unit,
    onNextClick: () -> Unit
) {
    val context = LocalContext.current
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(400.dp)
    ) {
        // Banner slides
        banners.forEachIndexed { index, banner ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(if (index == currentBanner) 1f else 0f),
                contentAlignment = Alignment.Center
            ) {
                if (banner.imageName != null) {
                    // Show image banner
                    val imageResId = context.resources.getIdentifier(
                        banner.imageName?.substringBefore("."),
                        "drawable",
                        context.packageName
                    )
                    if (imageResId != 0) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(imageResId)
                                .crossfade(true)
                                .build(),
                            contentDescription = banner.title,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                    // Overlay gradient for text readability
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Black.copy(alpha = 0.3f),
                                        Color.Transparent,
                                        Color.Black.copy(alpha = 0.5f)
                                    )
                                )
                            )
                    )
                } else {
                    // Show gradient banner
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.linearGradient(
                                    colors = banner.gradientColors,
                                    start = androidx.compose.ui.geometry.Offset(0f, 0f),
                                    end = androidx.compose.ui.geometry.Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                                )
                            )
                    )
                }
                
                // Text overlay
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier
                        .padding(horizontal = 24.dp)
                        .background(
                            color = Color.White.copy(alpha = 0.9f),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                ) {
                    Text(
                        text = banner.title,
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = PrimaryBlue
                        ),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = banner.subtitle,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            color = PrimaryBlue.copy(alpha = 0.8f)
                        ),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
        
        // Banner controls
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            banners.forEachIndexed { index, _ ->
                Box(
                    modifier = Modifier
                        .size(width = if (index == currentBanner) 32.dp else 12.dp, height = 12.dp)
                        .clip(RoundedCornerShape(if (index == currentBanner) 6.dp else 50.dp))
                        .background(if (index == currentBanner) Color.White else Color.White.copy(alpha = 0.5f))
                        .clickable { onBannerSelected(index) }
                )
            }
        }
        
        // Navigation buttons
        IconButton(
            onClick = onPrevClick,
            modifier = Modifier.align(Alignment.CenterStart)
        ) {
            Text(
                text = "‹",
                color = Color.White,
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.padding(8.dp)
            )
        }
        
        IconButton(
            onClick = onNextClick,
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            Text(
                text = "›",
                color = Color.White,
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}

@Composable
fun BannerImagesSection() {
    val context = LocalContext.current
    val bannerResIds = listOf(
        context.resources.getIdentifier("baner1", "drawable", context.packageName),
        context.resources.getIdentifier("baner2", "drawable", context.packageName),
        context.resources.getIdentifier("baner3", "drawable", context.packageName)
    )
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp, horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Trải nghiệm tuyệt vời - Hành trình đáng nhớ",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 16.dp)
        )
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(horizontal = 8.dp)
        ) {
            items(bannerResIds.size) { index ->
                val resId = bannerResIds[index]
                if (resId != 0) {
                    Card(
                        modifier = Modifier
                            .width(300.dp)
                            .height(200.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(resId)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Banner ${index + 1}",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
        }
    }
}

data class BannerData(
    val title: String,
    val subtitle: String,
    val imageName: String? = null,
    val gradientColors: List<Color>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchFormCard(
    tripType: String,
    onTripTypeChange: (String) -> Unit,
    airports: List<Airport>,
    departureAirport: Airport?,
    onDepartureAirportChange: (Airport?) -> Unit,
    arrivalAirport: Airport?,
    onArrivalAirportChange: (Airport?) -> Unit,
    departureDate: String,
    onDepartureDateChange: (String) -> Unit,
    returnDate: String,
    onReturnDateChange: (String) -> Unit,
    adults: Int,
    onAdultsChange: (Int) -> Unit,
    children: Int,
    onChildrenChange: (Int) -> Unit,
    infants: Int,
    onInfantsChange: (Int) -> Unit,
    fareClass: String,
    onFareClassChange: (String) -> Unit,
    showPassengerSelector: Boolean,
    onShowPassengerSelectorChange: (Boolean) -> Unit,
    errors: Map<String, String>,
    onSearchClick: () -> Unit,
    isLoading: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .offset(y = (-80).dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Trip type selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = tripType == "mot_chieu",
                    onClick = { onTripTypeChange("mot_chieu") },
                    label = { Text("Một chiều") },
                    modifier = Modifier.weight(1f),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PrimaryBlue.copy(alpha = 0.1f),
                        selectedLabelColor = PrimaryBlue
                    )
                )
                FilterChip(
                    selected = tripType == "khu_hoi",
                    onClick = { onTripTypeChange("khu_hoi") },
                    label = { Text("Khứ hồi") },
                    modifier = Modifier.weight(1f),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PrimaryBlue.copy(alpha = 0.1f),
                        selectedLabelColor = PrimaryBlue
                    )
                )
            }
            
            // Airports
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                var departureExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = departureExpanded,
                    onExpandedChange = { departureExpanded = !departureExpanded },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = departureAirport?.let { "${it.ma_san_bay} - ${it.ten_san_bay}" } ?: if (isLoading) "Đang tải..." else "",
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Sân bay đi") },
                        trailingIcon = { 
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = departureExpanded)
                            }
                        },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        enabled = !isLoading && airports.isNotEmpty(),
                        isError = errors.containsKey("san_bay_di"),
                        supportingText = errors["san_bay_di"]?.let { { Text(it) } }
                    )
                    if (airports.isNotEmpty()) {
                        ExposedDropdownMenu(
                            expanded = departureExpanded,
                            onDismissRequest = { departureExpanded = false },
                            modifier = Modifier.heightIn(max = 300.dp)
                        ) {
                            airports.forEach { airport ->
                                DropdownMenuItem(
                                    text = { Text("${airport.ma_san_bay} - ${airport.ten_san_bay}") },
                                    onClick = {
                                        onDepartureAirportChange(airport)
                                        departureExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
                
                var arrivalExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = arrivalExpanded,
                    onExpandedChange = { arrivalExpanded = !arrivalExpanded },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = arrivalAirport?.let { "${it.ma_san_bay} - ${it.ten_san_bay}" } ?: if (isLoading) "Đang tải..." else "",
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Sân bay đến") },
                        trailingIcon = { 
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = arrivalExpanded)
                            }
                        },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        enabled = !isLoading && airports.isNotEmpty(),
                        isError = errors.containsKey("san_bay_den"),
                        supportingText = errors["san_bay_den"]?.let { { Text(it) } }
                    )
                    if (airports.isNotEmpty()) {
                        ExposedDropdownMenu(
                            expanded = arrivalExpanded,
                            onDismissRequest = { arrivalExpanded = false },
                            modifier = Modifier.heightIn(max = 300.dp)
                        ) {
                            airports.filter { it.id != departureAirport?.id }.forEach { airport ->
                                DropdownMenuItem(
                                    text = { Text("${airport.ma_san_bay} - ${airport.ten_san_bay}") },
                                    onClick = {
                                        onArrivalAirportChange(airport)
                                        arrivalExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
            
            // Dates
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                var showDatePicker by remember { mutableStateOf(false) }
                
                OutlinedTextField(
                    value = departureDate,
                    onValueChange = { },
                    label = { Text("Ngày đi") },
                    readOnly = true,
                    modifier = Modifier.weight(1f),
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(Icons.Default.Info, contentDescription = "Chọn ngày", tint = MaterialTheme.colorScheme.primary)
                        }
                    },
                    isError = errors.containsKey("ngay_khoi_hanh"),
                    supportingText = errors["ngay_khoi_hanh"]?.let { { Text(it) } }
                )
                
                if (showDatePicker) {
                    val datePickerState = rememberDatePickerState(
                        initialSelectedDateMillis = if (departureDate.isNotEmpty()) {
                            try {
                                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(departureDate)?.time
                            } catch (e: Exception) {
                                null
                            }
                        } else null
                    )
                    
                    CustomDatePickerDialog(
                        onDismissRequest = { showDatePicker = false },
                        onConfirm = {
                            datePickerState.selectedDateMillis?.let { millis ->
                                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                onDepartureDateChange(dateFormat.format(Date(millis)))
                            }
                            showDatePicker = false
                        },
                        dismissButton = {
                            TextButton(onClick = { showDatePicker = false }) {
                                Text("Hủy")
                            }
                        },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    datePickerState.selectedDateMillis?.let { millis ->
                                        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                        onDepartureDateChange(dateFormat.format(Date(millis)))
                                    }
                                    showDatePicker = false
                                }
                            ) {
                                Text("Chọn")
                            }
                        }
                    ) {
                        DatePicker(state = datePickerState)
                    }
                }
                
                if (tripType == "khu_hoi") {
                    var showReturnDatePicker by remember { mutableStateOf(false) }
                    
                    OutlinedTextField(
                        value = returnDate,
                        onValueChange = { },
                        label = { Text("Ngày về") },
                        readOnly = true,
                        modifier = Modifier.weight(1f),
                        trailingIcon = {
                            IconButton(onClick = { showReturnDatePicker = true }) {
                                Text("📅", style = MaterialTheme.typography.bodyLarge)
                            }
                        },
                        isError = errors.containsKey("ngay_ve"),
                        supportingText = errors["ngay_ve"]?.let { { Text(it) } }
                    )
                    
                    if (showReturnDatePicker) {
                        val minDateMillis = if (departureDate.isNotEmpty()) {
                            try {
                                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(departureDate)?.time
                            } catch (e: Exception) {
                                null
                            }
                        } else null
                        
                        val returnDatePickerState = rememberDatePickerState(
                            initialSelectedDateMillis = if (returnDate.isNotEmpty()) {
                                try {
                                    SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(returnDate)?.time
                                } catch (e: Exception) {
                                    null
                                }
                            } else null
                        )
                        
                        CustomDatePickerDialog(
                            onDismissRequest = { showReturnDatePicker = false },
                            onConfirm = {
                                returnDatePickerState.selectedDateMillis?.let { millis ->
                                    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                    onReturnDateChange(dateFormat.format(Date(millis)))
                                }
                                showReturnDatePicker = false
                            },
                            dismissButton = {
                                TextButton(onClick = { showReturnDatePicker = false }) {
                                    Text("Hủy")
                                }
                            },
                            confirmButton = {
                                TextButton(
                                    onClick = {
                                        returnDatePickerState.selectedDateMillis?.let { millis ->
                                            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                            onReturnDateChange(dateFormat.format(Date(millis)))
                                        }
                                        showReturnDatePicker = false
                                    }
                                ) {
                                    Text("Chọn")
                                }
                            }
                        ) {
                            DatePicker(state = returnDatePickerState)
                        }
                    }
                }
            }
            
            // Passengers and Fare Class
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Passenger selector
                val totalPassengers = adults + children + infants
                OutlinedTextField(
                    value = "$totalPassengers người",
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("Hành khách") },
                    trailingIcon = {
                        IconButton(onClick = { onShowPassengerSelectorChange(true) }) {
                            Icon(
                                Icons.Default.ArrowDropDown,
                                contentDescription = "Chọn hành khách"
                            )
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
                
                // Passenger selector dialog
                if (showPassengerSelector) {
                    Dialog(onDismissRequest = { onShowPassengerSelectorChange(false) }) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                verticalArrangement = Arrangement.spacedBy(20.dp)
                            ) {
                                Text(
                                    text = "Chọn hành khách",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                
                                Divider()
                                
                                PassengerRow(
                                    label = "Người lớn (12+)",
                                    count = adults,
                                    onDecrease = { if (adults > 1) onAdultsChange(adults - 1) },
                                    onIncrease = { if (adults < 9) onAdultsChange(adults + 1) }
                                )
                                Divider()
                                PassengerRow(
                                    label = "Trẻ em (2-11)",
                                    count = children,
                                    onDecrease = { if (children > 0) onChildrenChange(children - 1) },
                                    onIncrease = { if (children < 9) onChildrenChange(children + 1) }
                                )
                                Divider()
                                PassengerRow(
                                    label = "Em bé (<2)",
                                    count = infants,
                                    onDecrease = { if (infants > 0) onInfantsChange(infants - 1) },
                                    onIncrease = { if (infants < 9) onInfantsChange(infants + 1) }
                                )
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                Button(
                                    onClick = { onShowPassengerSelectorChange(false) },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = PrimaryBlue
                                    )
                                ) {
                                    Text("Xác nhận")
                                }
                            }
                        }
                    }
                }
                
                // Fare class
                var fareExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = fareExpanded,
                    onExpandedChange = { fareExpanded = !fareExpanded },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = when (fareClass) {
                            "pho_thong" -> "Phổ thông"
                            "thuong_gia" -> "Thương gia"
                            "hang_nhat" -> "Hạng nhất"
                            else -> "Tất cả"
                        },
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Hạng vé") },
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
                            text = { Text("Tất cả") },
                            onClick = {
                                onFareClassChange("")
                                fareExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Phổ thông") },
                            onClick = {
                                onFareClassChange("pho_thong")
                                fareExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Thương gia") },
                            onClick = {
                                onFareClassChange("thuong_gia")
                                fareExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Hạng nhất") },
                            onClick = {
                                onFareClassChange("hang_nhat")
                                fareExpanded = false
                            }
                        )
                    }
                }
            }
            
            if (errors.containsKey("tong_hanh_khach")) {
                Text(
                    text = errors["tong_hanh_khach"] ?: "",
                    color = ErrorRed,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            
            // Search button
            Button(
                onClick = onSearchClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryBlue
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White
                    )
                } else {
                    Text(
                        "Tìm chuyến bay",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun PassengerRow(
    label: String,
    count: Int,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onDecrease,
                modifier = Modifier.size(36.dp)
            ) {
                Text("-", style = MaterialTheme.typography.titleLarge)
            }
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.width(30.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            IconButton(
                onClick = onIncrease,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tăng")
            }
        }
    }
}


@Composable
fun TodayFlightsSection(
    flights: List<Flight>,
    isLoading: Boolean,
    onFlightClick: (Flight) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp, horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text(
            text = "Chuyến bay sắp tới",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (flights.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Không có chuyến bay nào trong ngày hôm nay",
                    modifier = Modifier.padding(24.dp),
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextSecondary
                )
            }
        } else {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(horizontal = 8.dp)
            ) {
                items(flights) { flight ->
                    FlightCardMini(
                        flight = flight,
                        onClick = { onFlightClick(flight) }
                    )
                }
            }
        }
    }
}

@Composable
fun FlightCardMini(
    flight: Flight,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(300.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Route
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.Start, modifier = Modifier.weight(1f)) {
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
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
                
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = PrimaryBlue,
                    modifier = Modifier
                        .size(24.dp)
                        .padding(horizontal = 8.dp)
                )
                
                Column(horizontalAlignment = Alignment.End, modifier = Modifier.weight(1f)) {
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
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        textAlign = TextAlign.End
                    )
                }
            }
            
            Divider()
            
            // Airline and price
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = flight.hang_hang_khong?.ten_hang ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
                
                flight.gia_ve?.firstOrNull()?.let { price ->
                    Text(
                        text = formatCurrency(price.gia),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = PrimaryBlue
                        )
                    )
                } ?: Text(
                    text = "Liên hệ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
fun FeaturesSection() {
    val features = listOf(
        FeatureData(
            icon = Icons.Default.Search,
            title = "So sánh giá vé",
            description = "Xem giá vé từ nhiều hãng hàng không cùng một lúc",
            gradientColors = listOf(PrimaryBlue, PrimaryPurple)
        ),
        FeatureData(
            icon = Icons.Default.Lock,
            title = "Đặt vé an toàn",
            description = "Thanh toán bảo mật với nhiều phương thức thanh toán",
            gradientColors = listOf(Color(0xFF4FACFE), Color(0xFF00F2FE))
        ),
        FeatureData(
            icon = Icons.Default.Info,
            title = "Giữ chỗ tạm thời",
            description = "Giữ chỗ 15 phút để bạn có thời gian thanh toán",
            gradientColors = listOf(Color(0xFF43E97B), Color(0xFF38F9D7))
        ),
        FeatureData(
            icon = Icons.Default.List,
            title = "Quản lý đặt vé",
            description = "Xem, hủy hoặc đổi vé dễ dàng trong tài khoản của bạn",
            gradientColors = listOf(Color(0xFFF093FB), Color(0xFFF5576C))
        )
    )
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp, horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text(
            text = "Tại sao chọn chúng tôi?",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        
        // Hiển thị 2 cột trên 1 hàng
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            features.chunked(2).forEach { rowFeatures ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    rowFeatures.forEach { feature ->
                        FeatureCard(
                            icon = feature.icon,
                            title = feature.title,
                            description = feature.description,
                            gradientColors = feature.gradientColors,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    // Nếu chỉ có 1 feature trong hàng cuối, thêm Spacer để căn chỉnh
                    if (rowFeatures.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

data class FeatureData(
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val title: String,
    val description: String,
    val gradientColors: List<Color>
)

@Composable
fun FeatureCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    gradientColors: List<Color>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(
                        brush = Brush.linearGradient(colors = gradientColors),
                        shape = RoundedCornerShape(16.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
            
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                )
            )
            
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )
        }
    }
}
