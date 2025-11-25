package com.example.flybook.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.flybook.data.models.Flight
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TodayFlightsList(
    flights: List<Flight>,
    onFlightClick: (Flight) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        flights.forEach { flight ->
            FlightCard(
                flight = flight,
                onClick = { onFlightClick(flight) }
            )
        }
    }
}

@Composable
fun FlightCard(
    flight: Flight,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = flight.tuyen_bay?.san_bay_di?.ma_san_bay ?: "",
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = "→",
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = flight.tuyen_bay?.san_bay_den?.ma_san_bay ?: "",
                    style = MaterialTheme.typography.titleLarge
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = formatTime(flight.gio_khoi_hanh),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = flight.tuyen_bay?.san_bay_di?.ten_san_bay ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Column(horizontalAlignment = androidx.compose.ui.Alignment.End) {
                    Text(
                        text = formatTime(flight.gio_ha_canh),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = flight.tuyen_bay?.san_bay_den?.ten_san_bay ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Divider()
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Text(
                    text = flight.hang_hang_khong?.ten_hang ?: "",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                flight.gia_ve?.firstOrNull()?.let { price ->
                    Text(
                        text = formatCurrency(price.gia),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

fun formatTime(dateString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val outputFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val date = inputFormat.parse(dateString)
        outputFormat.format(date ?: Date())
    } catch (e: Exception) {
        dateString
    }
}

fun formatCurrency(amount: Double): String {
    return String.format(Locale("vi", "VN"), "%,.0f VND", amount)
}

