package com.example.flybook.ui.screens.search

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavController
import com.example.flybook.data.models.FlightSearchRequest
import com.example.flybook.data.repository.CustomerFlightRepository
import com.example.flybook.navigation.Screen
import com.example.flybook.ui.components.FlightCard
import com.example.flybook.ui.components.BottomNavigationBar
import com.example.flybook.ui.components.customerBottomNavItems
import com.example.flybook.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlightSearchScreen(
    navController: NavController,
    savedStateHandle: SavedStateHandle? = null
) {
    val flightRepository = remember { CustomerFlightRepository() }
    val scope = rememberCoroutineScope()
    
    // Get navigation arguments
    val navBackStackEntry = navController.currentBackStackEntry
    val stateHandle = savedStateHandle ?: navBackStackEntry?.savedStateHandle
    val route = navBackStackEntry?.destination?.route
    
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var searchResults by remember { mutableStateOf<com.example.flybook.data.models.FlightSearchResponse?>(null) }
    
    // Parse search parameters from query string in route or SavedStateHandle
    LaunchedEffect(navBackStackEntry?.id) {
        isLoading = true
        errorMessage = null
        
        android.util.Log.d("FlightSearchScreen", "Route: $route")
        android.util.Log.d("FlightSearchScreen", "NavBackStackEntry ID: ${navBackStackEntry?.id}")
        android.util.Log.d("FlightSearchScreen", "SavedStateHandle keys: ${stateHandle?.keys()}")
        
        // Wait a bit for SavedStateHandle to be populated (if set after navigation)
        kotlinx.coroutines.delay(100)
        
        // Parse query string from route or SavedStateHandle
        fun getQueryParam(key: String): String? {
            // First, try to get from savedStateHandle (if saved during navigation)
            // Handle both String and Int types
            try {
                stateHandle?.get<String>(key)?.let { 
                    android.util.Log.d("FlightSearchScreen", "Got $key from savedStateHandle (String): $it")
                    return it 
                }
            } catch (e: ClassCastException) {
                // If it's an Int, convert to String
                try {
                    stateHandle?.get<Int>(key)?.toString()?.let { 
                        android.util.Log.d("FlightSearchScreen", "Got $key from savedStateHandle (Int): $it")
                        return it 
                    }
                } catch (e2: Exception) {
                    android.util.Log.w("FlightSearchScreen", "Error getting $key from savedStateHandle: ${e2.message}")
                }
            }
            
            // Try to get from savedStateHandle with "search_" prefix (from HomeScreen)
            try {
                stateHandle?.get<String>("search_$key")?.let { 
                    android.util.Log.d("FlightSearchScreen", "Got search_$key from savedStateHandle (String): $it")
                    return it 
                }
            } catch (e: ClassCastException) {
                // If it's an Int, convert to String
                try {
                    stateHandle?.get<Int>("search_$key")?.toString()?.let { 
                        android.util.Log.d("FlightSearchScreen", "Got search_$key from savedStateHandle (Int): $it")
                        return it 
                    }
                } catch (e2: Exception) {
                    android.util.Log.w("FlightSearchScreen", "Error getting search_$key from savedStateHandle: ${e2.message}")
                }
            }
            
            // Try to parse from route if it contains query string
            val routeToCheck = route ?: ""
            android.util.Log.d("FlightSearchScreen", "Checking route for query string: $routeToCheck")
            
            if (routeToCheck.contains("?")) {
                val queryString = routeToCheck.substringAfter("?")
                android.util.Log.d("FlightSearchScreen", "Query string from route: $queryString")
                val params = queryString.split("&")
                val param = params.find { it.startsWith("$key=") }
                val value = param?.substringAfter("=")?.let { 
                    try {
                        Uri.decode(it)
                    } catch (e: Exception) {
                        it
                    }
                }
                android.util.Log.d("FlightSearchScreen", "Found $key in route: $value")
                return value
            }
            
            android.util.Log.w("FlightSearchScreen", "Could not find $key in route or savedStateHandle")
            return null
        }
        
        val sanBayDi = getQueryParam("san_bay_di") ?: ""
        val sanBayDen = getQueryParam("san_bay_den") ?: ""
        val ngayKhoiHanh = getQueryParam("ngay_khoi_hanh") ?: ""
        val loaiChuyen = getQueryParam("loai_chuyen") ?: "mot_chieu"
        val nguoiLon = getQueryParam("nguoi_lon")?.toIntOrNull() ?: 1
        val treEm = getQueryParam("tre_em")?.toIntOrNull() ?: 0
        val emBe = getQueryParam("em_be")?.toIntOrNull() ?: 0
        val ngayVe = getQueryParam("ngay_ve")
        val hangVe = getQueryParam("hang_ve")
        
        android.util.Log.d("FlightSearchScreen", "Parsed params: san_bay_di=$sanBayDi, san_bay_den=$sanBayDen, ngay_khoi_hanh=$ngayKhoiHanh, loai_chuyen=$loaiChuyen")
        
        // Validate required parameters
        if (sanBayDi.isEmpty() || sanBayDen.isEmpty() || ngayKhoiHanh.isEmpty()) {
            android.util.Log.e("FlightSearchScreen", "Missing required params: san_bay_di=$sanBayDi, san_bay_den=$sanBayDen, ngay_khoi_hanh=$ngayKhoiHanh")
            errorMessage = "Thiếu thông tin tìm kiếm. Vui lòng quay lại và nhập đầy đủ thông tin."
            isLoading = false
            return@LaunchedEffect
        }
        
        scope.launch {
            val request = FlightSearchRequest(
                san_bay_di = sanBayDi,
                san_bay_den = sanBayDen,
                ngay_khoi_hanh = ngayKhoiHanh,
                loai_chuyen = loaiChuyen,
                nguoi_lon = nguoiLon,
                tre_em = treEm,
                em_be = emBe,
                ngay_ve = ngayVe,
                hang_ve = hangVe?.takeIf { it.isNotEmpty() }
            )
            
            android.util.Log.d("FlightSearchScreen", "Search request: $request")
            
            flightRepository.searchFlights(request)
                .onSuccess { results ->
                    searchResults = results
                    errorMessage = null
                    android.util.Log.d("FlightSearchScreen", "Search successful: ${results.chuyen_bay_di.size} flights found")
                }
                .onFailure { e ->
                    errorMessage = e.message ?: "Không thể tìm kiếm chuyến bay"
                    android.util.Log.e("FlightSearchScreen", "Search failed: ${e.message}", e)
                }
            isLoading = false
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tìm kiếm chuyến bay") },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (errorMessage != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
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
            } else if (searchResults != null) {
                Text(
                    text = "${searchResults!!.chuyen_bay_di.size} chuyến bay đi",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(searchResults!!.chuyen_bay_di) { flight ->
                        FlightCard(
                            flight = flight,
                            onClick = {
                                navController.navigate(
                                    Screen.FlightDetail.createRoute(flight.id)
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

