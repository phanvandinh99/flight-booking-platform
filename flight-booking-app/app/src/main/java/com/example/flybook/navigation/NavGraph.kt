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
    }
}

