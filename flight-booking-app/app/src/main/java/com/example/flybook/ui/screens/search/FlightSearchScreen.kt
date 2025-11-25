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
fun FlightSearchScreen(navController: NavController) {
    val flightRepository = remember { CustomerFlightRepository() }
    val scope = rememberCoroutineScope()
    
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var searchResults by remember { mutableStateOf<com.example.flybook.data.models.FlightSearchResponse?>(null) }
    
    // TODO: Get search params from navigation arguments
    // For now, using default values
    LaunchedEffect(Unit) {
        isLoading = true
        scope.launch {
            val request = FlightSearchRequest(
                san_bay_di = "SGN",
                san_bay_den = "HAN",
                ngay_khoi_hanh = "2025-01-01",
                loai_chuyen = "mot_chieu",
                nguoi_lon = 1
            )
            
            flightRepository.searchFlights(request)
                .onSuccess { results ->
                    searchResults = results
                    errorMessage = null
                }
                .onFailure { e ->
                    errorMessage = e.message ?: "Không thể tìm kiếm chuyến bay"
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

