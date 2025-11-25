package com.example.flybook.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.flybook.navigation.Screen
import com.example.flybook.ui.theme.*

sealed class BottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector = icon
) {
    object Home : BottomNavItem(
        route = Screen.Home.route,
        title = "Trang chủ",
        icon = Icons.Default.Home,
        selectedIcon = Icons.Default.Home
    )
    
    object MyBookings : BottomNavItem(
        route = Screen.MyBookings.route,
        title = "Đặt vé",
        icon = Icons.Default.ShoppingCart,
        selectedIcon = Icons.Default.ShoppingCart
    )
    
    object Admin : BottomNavItem(
        route = Screen.AdminDashboard.route,
        title = "Quản trị",
        icon = Icons.Default.Person,
        selectedIcon = Icons.Default.Person
    )
    
    object AirlineDashboard : BottomNavItem(
        route = Screen.AirlineDashboard.route,
        title = "Trang chủ",
        icon = Icons.Default.Home,
        selectedIcon = Icons.Default.Home
    )
    
    object AirlineAircraft : BottomNavItem(
        route = Screen.AirlineAircraftManagement.route,
        title = "Máy bay",
        icon = Icons.Default.LocationOn,
        selectedIcon = Icons.Default.LocationOn
    )
}

val customerBottomNavItems = listOf(
    BottomNavItem.Home,
    BottomNavItem.MyBookings
)

val adminBottomNavItems = listOf(
    BottomNavItem.Home,
    BottomNavItem.MyBookings,
    BottomNavItem.Admin
)

val airlineBottomNavItems = listOf(
    BottomNavItem.AirlineDashboard,
    BottomNavItem.AirlineAircraft
)

@Composable
fun BottomNavigationBar(
    navController: NavController,
    items: List<BottomNavItem>
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    NavigationBar(
        containerColor = Color(0xFFFAFAFA),
        tonalElevation = 16.dp
    ) {
        items.forEach { item ->
            val isSelected = currentRoute == item.route
            
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = if (isSelected) item.selectedIcon else item.icon,
                        contentDescription = item.title,
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.labelSmall
                    )
                },
                selected = isSelected,
                onClick = {
                    if (currentRoute != item.route) {
                        navController.navigate(item.route) {
                            // Pop up to the start destination, but save state
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            // Avoid multiple copies of the same destination
                            launchSingleTop = true
                            // Restore state when reselecting
                            restoreState = true
                        }
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = PrimaryBlue,
                    selectedTextColor = PrimaryBlue,
                    indicatorColor = PrimaryBlue.copy(alpha = 0.15f),
                    unselectedIconColor = TextSecondary.copy(alpha = 0.7f),
                    unselectedTextColor = TextSecondary.copy(alpha = 0.7f)
                )
            )
        }
    }
}

