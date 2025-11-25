package com.example.flybook.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.flybook.ui.screens.home.HomeScreen
import com.example.flybook.ui.screens.auth.LoginScreen
import com.example.flybook.ui.screens.auth.RegisterScreen
import com.example.flybook.ui.screens.search.FlightSearchScreen
import com.example.flybook.ui.screens.detail.FlightDetailScreen
import com.example.flybook.ui.screens.booking.BookingScreen
import com.example.flybook.ui.screens.bookings.MyBookingsScreen
import com.example.flybook.ui.screens.admin.AdminDashboardScreen
import com.example.flybook.ui.screens.admin.AirlineApprovalScreen
import com.example.flybook.ui.screens.admin.AirportManagementScreen
import com.example.flybook.ui.screens.admin.RouteManagementScreen
import com.example.flybook.ui.screens.admin.ReportsScreen
import com.example.flybook.ui.screens.admin.SystemConfigScreen
import com.example.flybook.ui.screens.airline.AirlineDashboardScreen
import com.example.flybook.ui.screens.airline.AircraftManagementScreen

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Login : Screen("login")
    object Register : Screen("register")
    object FlightSearch : Screen("flight_search")
    object FlightDetail : Screen("flight_detail/{flightId}") {
        fun createRoute(flightId: Int) = "flight_detail/$flightId"
    }
    object Booking : Screen("booking")
    object MyBookings : Screen("my_bookings")
    
    // Admin screens
    object AdminDashboard : Screen("admin/dashboard")
    object AdminAirlineApproval : Screen("admin/airline_approval")
    object AdminAirportManagement : Screen("admin/airport_management")
    object AdminRouteManagement : Screen("admin/route_management")
    object AdminReports : Screen("admin/reports")
    object AdminSystemConfig : Screen("admin/system_config")
    
    // Airline screens
    object AirlineDashboard : Screen("airline/dashboard")
    object AirlineAircraftManagement : Screen("airline/aircraft_management")
}

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(navController = navController)
        }
        composable(Screen.Login.route) {
            LoginScreen(navController = navController)
        }
        composable(Screen.Register.route) {
            RegisterScreen(navController = navController)
        }
        composable(Screen.FlightSearch.route) {
            FlightSearchScreen(navController = navController)
        }
        composable(Screen.FlightDetail.route) { backStackEntry ->
            val flightId = backStackEntry.arguments?.getString("flightId")?.toIntOrNull() ?: 0
            FlightDetailScreen(
                navController = navController,
                flightId = flightId
            )
        }
        composable(Screen.Booking.route) {
            BookingScreen(navController = navController)
        }
        composable(Screen.MyBookings.route) {
            MyBookingsScreen(navController = navController)
        }
        
        // Admin routes
        composable(Screen.AdminDashboard.route) {
            AdminDashboardScreen(navController = navController)
        }
        composable(Screen.AdminAirlineApproval.route) {
            AirlineApprovalScreen(navController = navController)
        }
        composable(Screen.AdminAirportManagement.route) {
            AirportManagementScreen(navController = navController)
        }
        composable(Screen.AdminRouteManagement.route) {
            RouteManagementScreen(navController = navController)
        }
        composable(Screen.AdminReports.route) {
            ReportsScreen(navController = navController)
        }
        composable(Screen.AdminSystemConfig.route) {
            SystemConfigScreen(navController = navController)
        }
        
        // Airline routes
        composable(Screen.AirlineDashboard.route) {
            AirlineDashboardScreen(navController = navController)
        }
        composable(Screen.AirlineAircraftManagement.route) {
            AircraftManagementScreen(navController = navController)
        }
    }
}

