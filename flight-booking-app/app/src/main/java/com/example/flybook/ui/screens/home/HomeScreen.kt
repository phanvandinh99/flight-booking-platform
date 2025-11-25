package com.example.flybook.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.flybook.ui.theme.*
import androidx.navigation.NavController
import com.example.flybook.data.models.Airport
import com.example.flybook.data.models.Flight
import com.example.flybook.data.repository.FlightRepository
import com.example.flybook.navigation.Screen
import com.example.flybook.ui.components.FlightSearchForm
import com.example.flybook.ui.components.TodayFlightsList
import com.example.flybook.ui.components.BottomNavigationBar
import com.example.flybook.ui.components.customerBottomNavItems
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    val flightRepository = remember { FlightRepository() }
    val scope = rememberCoroutineScope()
    
    var airports by remember { mutableStateOf<List<Airport>>(emptyList()) }
    var todayFlights by remember { mutableStateOf<List<Flight>>(emptyList()) }
    var isLoadingAirports by remember { mutableStateOf(false) }
    var isLoadingFlights by remember { mutableStateOf(true) }
    
    LaunchedEffect(Unit) {
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
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Flight Booking") },
                actions = {
                    TextButton(onClick = { navController.navigate(Screen.Login.route) }) {
                        Text("Đăng nhập")
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
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                text = "Tìm kiếm chuyến bay",
                style = MaterialTheme.typography.headlineMedium
            )
            
            FlightSearchForm(
                airports = airports,
                isLoadingAirports = isLoadingAirports,
                onSearchClick = { searchData ->
                    // Navigate to search screen with search data
                    navController.navigate(Screen.FlightSearch.route)
                }
            )
            
            Divider()
            
            Text(
                text = "Chuyến bay sắp tới",
                style = MaterialTheme.typography.titleLarge
            )
            
            if (isLoadingFlights) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (todayFlights.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Không có chuyến bay nào trong ngày hôm nay",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else {
                TodayFlightsList(
                    flights = todayFlights,
                    onFlightClick = { flight ->
                        navController.navigate(Screen.FlightDetail.createRoute(flight.id))
                    }
                )
            }
        }
    }
}

