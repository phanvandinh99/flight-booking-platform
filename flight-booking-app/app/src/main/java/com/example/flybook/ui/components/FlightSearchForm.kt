package com.example.flybook.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.flybook.data.models.Airport
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlightSearchForm(
    airports: List<Airport>,
    isLoadingAirports: Boolean,
    onSearchClick: (SearchData) -> Unit
) {
    var tripType by remember { mutableStateOf("mot_chieu") }
    var departureAirport by remember { mutableStateOf<Airport?>(null) }
    var arrivalAirport by remember { mutableStateOf<Airport?>(null) }
    var departureDate by remember { mutableStateOf(LocalDate.now().toString()) }
    var returnDate by remember { mutableStateOf<LocalDate?>(null) }
    var adults by remember { mutableStateOf(1) }
    var children by remember { mutableStateOf(0) }
    var infants by remember { mutableStateOf(0) }
    
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
            // Trip type selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = tripType == "mot_chieu",
                    onClick = { tripType = "mot_chieu" },
                    label = { Text("Một chiều") },
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = tripType == "khu_hoi",
                    onClick = { tripType = "khu_hoi" },
                    label = { Text("Khứ hồi") },
                    modifier = Modifier.weight(1f)
                )
            }
            
            // Airports
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                var departureExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = departureExpanded,
                    onExpandedChange = { departureExpanded = !departureExpanded },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = departureAirport?.let { "${it.ma_san_bay} - ${it.ten_san_bay}" } ?: "",
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Sân bay đi") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = departureExpanded) },
                        modifier = Modifier.menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = departureExpanded,
                        onDismissRequest = { departureExpanded = false }
                    ) {
                        airports.forEach { airport ->
                            DropdownMenuItem(
                                text = { Text("${airport.ma_san_bay} - ${airport.ten_san_bay}") },
                                onClick = {
                                    departureAirport = airport
                                    departureExpanded = false
                                }
                            )
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
                        value = arrivalAirport?.let { "${it.ma_san_bay} - ${it.ten_san_bay}" } ?: "",
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Sân bay đến") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = arrivalExpanded) },
                        modifier = Modifier.menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = arrivalExpanded,
                        onDismissRequest = { arrivalExpanded = false }
                    ) {
                        airports.filter { it.id != departureAirport?.id }.forEach { airport ->
                            DropdownMenuItem(
                                text = { Text("${airport.ma_san_bay} - ${airport.ten_san_bay}") },
                                onClick = {
                                    arrivalAirport = airport
                                    arrivalExpanded = false
                                }
                            )
                        }
                    }
                }
            }
            
            // Dates
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = departureDate,
                    onValueChange = { departureDate = it },
                    label = { Text("Ngày đi") },
                    modifier = Modifier.weight(1f),
                    readOnly = true
                )
                
                if (tripType == "khu_hoi") {
                    OutlinedTextField(
                        value = returnDate?.toString() ?: "",
                        onValueChange = { },
                        label = { Text("Ngày về") },
                        modifier = Modifier.weight(1f),
                        readOnly = true
                    )
                }
            }
            
            // Passengers
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PassengerSelector(
                    label = "Người lớn",
                    count = adults,
                    onDecrease = { if (adults > 1) adults-- },
                    onIncrease = { if (adults < 9) adults++ },
                    modifier = Modifier.weight(1f)
                )
                PassengerSelector(
                    label = "Trẻ em",
                    count = children,
                    onDecrease = { if (children > 0) children-- },
                    onIncrease = { if (children < 9) children++ },
                    modifier = Modifier.weight(1f)
                )
                PassengerSelector(
                    label = "Em bé",
                    count = infants,
                    onDecrease = { if (infants > 0) infants-- },
                    onIncrease = { if (infants < 9) infants++ },
                    modifier = Modifier.weight(1f)
                )
            }
            
            Button(
                onClick = {
                    if (departureAirport != null && arrivalAirport != null) {
                        onSearchClick(
                            SearchData(
                                san_bay_di = departureAirport!!.ma_san_bay,
                                san_bay_den = arrivalAirport!!.ma_san_bay,
                                ngay_khoi_hanh = departureDate,
                                loai_chuyen = tripType,
                                ngay_ve = returnDate?.toString(),
                                nguoi_lon = adults,
                                tre_em = children,
                                em_be = infants
                            )
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = departureAirport != null && arrivalAirport != null && !isLoadingAirports
            ) {
                Text("Tìm chuyến bay")
            }
        }
    }
}

@Composable
fun PassengerSelector(
    label: String,
    count: Int,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            IconButton(onClick = onDecrease) {
                Text("-")
            }
            Text(text = count.toString())
            IconButton(onClick = onIncrease) {
                Text("+")
            }
        }
    }
}

data class SearchData(
    val san_bay_di: String,
    val san_bay_den: String,
    val ngay_khoi_hanh: String,
    val loai_chuyen: String,
    val ngay_ve: String?,
    val nguoi_lon: Int,
    val tre_em: Int,
    val em_be: Int
)

