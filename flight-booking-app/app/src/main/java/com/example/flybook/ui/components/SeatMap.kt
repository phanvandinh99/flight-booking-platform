package com.example.flybook.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.flybook.data.models.FareClassPrice
import com.example.flybook.data.models.SeatLayout
import com.example.flybook.ui.theme.*

data class SeatInfo(
    val number: String,
    val row: Int,
    val letter: String,
    val status: SeatStatus,
    val fareClass: String,
    val price: Double,
    val fareClassLabel: String
)

enum class SeatStatus {
    AVAILABLE,
    SELECTED,
    BOOKED,
    RESERVED
}

@Composable
fun SeatMap(
    seatLayout: List<SeatLayout>?,
    totalSeats: Int,
    bookedSeats: List<String>,
    reservedSeats: List<String>,
    selectedSeats: List<String>,
    allFareClasses: List<FareClassPrice>,
    onSeatSelect: (SeatInfo) -> Unit,
    modifier: Modifier = Modifier
) {
    val seats = remember(seatLayout, totalSeats, bookedSeats, reservedSeats, selectedSeats, allFareClasses) {
        generateSeatMap(seatLayout, totalSeats, bookedSeats, reservedSeats, selectedSeats, allFareClasses)
    }
    
    val seatsByRow = remember(seats) {
        seats.groupBy { it.row }
    }
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Legend
        SeatMapLegend()
        
        // Seat map
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Header: A B C | D E F
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Text("A", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        Text("B", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        Text("C", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(40.dp))
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Text("D", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        Text("E", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        Text("F", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    }
                }
                
                Divider()
                
                // Seat rows - Use Column instead of LazyColumn to avoid nested scroll issues
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.heightIn(max = 400.dp)
                ) {
                    seatsByRow.keys.sorted().forEach { rowNum ->
                        SeatRow(
                            rowNumber = rowNum,
                            seats = seatsByRow[rowNum] ?: emptyList(),
                            onSeatClick = onSeatSelect
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SeatRow(
    rowNumber: Int,
    seats: List<SeatInfo>,
    onSeatClick: (SeatInfo) -> Unit
) {
    val leftSeats = seats.filter { it.letter in listOf("A", "B", "C") }
    val rightSeats = seats.filter { it.letter in listOf("D", "E", "F") }
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Row number
        Text(
            text = rowNumber.toString(),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(24.dp),
            textAlign = TextAlign.Center
        )
        
        // Left side: A, B, C
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            leftSeats.forEach { seat ->
                SeatButton(seat = seat, onClick = { onSeatClick(seat) })
            }
            // Fill empty seats
            repeat(3 - leftSeats.size) {
                Spacer(modifier = Modifier.width(40.dp))
            }
        }
        
        // Aisle
        Spacer(modifier = Modifier.width(40.dp))
        
        // Right side: D, E, F
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            rightSeats.forEach { seat ->
                SeatButton(seat = seat, onClick = { onSeatClick(seat) })
            }
            // Fill empty seats
            repeat(3 - rightSeats.size) {
                Spacer(modifier = Modifier.width(40.dp))
            }
        }
    }
}

@Composable
fun SeatButton(
    seat: SeatInfo,
    onClick: () -> Unit
) {
    val (backgroundColor, borderColor, textColor) = when (seat.status) {
        SeatStatus.AVAILABLE -> {
            val fareClassColor = when (seat.fareClass) {
                "hang_nhat" -> Color(0xFFFFD700) // Gold
                "thuong_gia" -> Color(0xFF4169E1) // Royal Blue
                "pho_thong_cao_cap" -> Color(0xFF32CD32) // Lime Green
                else -> PrimaryBlue // Economy
            }
            Triple(fareClassColor.copy(alpha = 0.3f), fareClassColor, Color.Black)
        }
        SeatStatus.SELECTED -> Triple(SuccessGreen, SuccessGreen, Color.White)
        SeatStatus.BOOKED -> Triple(Color.Gray.copy(alpha = 0.5f), Color.Gray, Color.White)
        SeatStatus.RESERVED -> Triple(WarningYellow.copy(alpha = 0.5f), WarningYellow, Color.Black)
    }
    
    // Disable if booked or reserved
    val isEnabled = seat.status != SeatStatus.BOOKED && seat.status != SeatStatus.RESERVED
    
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(backgroundColor)
            .border(2.dp, borderColor, RoundedCornerShape(4.dp))
            .clickable(
                enabled = isEnabled,
                onClick = onClick
            )
            .alpha(if (isEnabled) 1f else 0.5f),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = seat.letter,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = textColor,
                fontSize = 12.sp
            )
            if (seat.status == SeatStatus.AVAILABLE && seat.price > 0) {
                Text(
                    text = "${(seat.price / 1000).toInt()}k",
                    style = MaterialTheme.typography.bodySmall,
                    color = textColor,
                    fontSize = 8.sp
                )
            }
        }
    }
}

@Composable
fun SeatMapLegend() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Chú thích:",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            LegendItem("Trống", PrimaryBlue.copy(alpha = 0.3f), PrimaryBlue)
            LegendItem("Đã chọn", SuccessGreen, SuccessGreen)
            LegendItem("Đã đặt", Color.Gray.copy(alpha = 0.5f), Color.Gray)
            LegendItem("Giữ chỗ", WarningYellow.copy(alpha = 0.5f), WarningYellow)
        }
    }
}

@Composable
fun LegendItem(
    label: String,
    backgroundColor: Color,
    borderColor: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(backgroundColor)
                .border(1.dp, borderColor, RoundedCornerShape(2.dp))
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            fontSize = 10.sp
        )
    }
}

fun generateSeatMap(
    seatLayout: List<SeatLayout>?,
    totalSeats: Int,
    bookedSeats: List<String>,
    reservedSeats: List<String>,
    selectedSeats: List<String>,
    allFareClasses: List<FareClassPrice>
): List<SeatInfo> {
    val normalizedBookedSeats = bookedSeats.map { it.trim().uppercase() }
    val normalizedReservedSeats = reservedSeats.map { it.trim().uppercase() }
    val normalizedSelectedSeats = selectedSeats.map { it.trim().uppercase() }
    
    android.util.Log.d("SeatMap", "Generating seat map with:")
    android.util.Log.d("SeatMap", "  Booked seats (normalized): $normalizedBookedSeats")
    android.util.Log.d("SeatMap", "  Reserved seats (normalized): $normalizedReservedSeats")
    android.util.Log.d("SeatMap", "  Seat layout count: ${seatLayout?.size ?: 0}")
    
    return if (seatLayout != null && seatLayout.isNotEmpty()) {
        seatLayout.map { seat ->
            val normalizedSeatNumber = seat.number.trim().uppercase()
            val status = when {
                normalizedBookedSeats.contains(normalizedSeatNumber) -> {
                    android.util.Log.d("SeatMap", "Seat $normalizedSeatNumber is BOOKED")
                    SeatStatus.BOOKED
                }
                normalizedReservedSeats.contains(normalizedSeatNumber) -> {
                    android.util.Log.d("SeatMap", "Seat $normalizedSeatNumber is RESERVED")
                    SeatStatus.RESERVED
                }
                normalizedSelectedSeats.contains(normalizedSeatNumber) -> SeatStatus.SELECTED
                else -> SeatStatus.AVAILABLE
            }
            
            val fareClassInfo = getSeatFareClass(seat.row, seat.letter, allFareClasses)
            
            SeatInfo(
                number = seat.number,
                row = seat.row,
                letter = seat.letter,
                status = status,
                fareClass = fareClassInfo.hang_ve,
                price = fareClassInfo.gia,
                fareClassLabel = fareClassInfo.label
            )
        }
    } else {
        // Generate default seat map
        val rows = (totalSeats / 6).coerceAtLeast(1)
        val seatLetters = listOf("A", "B", "C", "D", "E", "F")
        val generatedSeats = mutableListOf<SeatInfo>()
        
        for (row in 1..rows) {
            seatLetters.forEach { letter ->
                val seatNumber = "$row$letter"
                val normalizedSeatNumber = seatNumber.trim().uppercase()
                val status = when {
                    normalizedBookedSeats.contains(normalizedSeatNumber) -> SeatStatus.BOOKED
                    normalizedReservedSeats.contains(normalizedSeatNumber) -> SeatStatus.RESERVED
                    normalizedSelectedSeats.contains(normalizedSeatNumber) -> SeatStatus.SELECTED
                    else -> SeatStatus.AVAILABLE
                }
                
                val fareClassInfo = getSeatFareClass(row, letter, allFareClasses)
                
                generatedSeats.add(
                    SeatInfo(
                        number = seatNumber,
                        row = row,
                        letter = letter,
                        status = status,
                        fareClass = fareClassInfo.hang_ve,
                        price = fareClassInfo.gia,
                        fareClassLabel = fareClassInfo.label
                    )
                )
            }
        }
        
        generatedSeats
    }
}

data class SeatFareClassInfo(
    val hang_ve: String,
    val gia: Double,
    val label: String
)

fun getSeatFareClass(
    row: Int,
    letter: String,
    allFareClasses: List<FareClassPrice>
): SeatFareClassInfo {
    // Phân loại ghế theo vị trí:
    // - Hàng 1-3: VIP/Hạng nhất (nếu có)
    // - Hàng 4-8: Thương gia (nếu có)
    // - Hàng 9-15: Phổ thông cao cấp (nếu có)
    // - Còn lại: Phổ thông
    
    fun getLabel(hangVe: String): String {
        return when (hangVe) {
            "hang_nhat" -> "VIP"
            "thuong_gia" -> "Thương gia"
            "pho_thong_cao_cap" -> "Phổ thông cao cấp"
            "pho_thong" -> "Phổ thông"
            else -> hangVe
        }
    }
    
    if (row <= 3) {
        val vipClass = allFareClasses.find { it.hang_ve == "hang_nhat" }
        if (vipClass != null) return SeatFareClassInfo(vipClass.hang_ve, vipClass.gia, getLabel(vipClass.hang_ve))
    }
    
    if (row in 4..8) {
        val businessClass = allFareClasses.find { it.hang_ve == "thuong_gia" }
        if (businessClass != null) return SeatFareClassInfo(businessClass.hang_ve, businessClass.gia, getLabel(businessClass.hang_ve))
    }
    
    if (row in 9..15) {
        val premiumClass = allFareClasses.find { it.hang_ve == "pho_thong_cao_cap" }
        if (premiumClass != null) return SeatFareClassInfo(premiumClass.hang_ve, premiumClass.gia, getLabel(premiumClass.hang_ve))
    }
    
    // Default to economy
    val economyClass = allFareClasses.find { it.hang_ve == "pho_thong" }
    val defaultClass = economyClass ?: FareClassPrice("pho_thong", 0.0)
    return SeatFareClassInfo(defaultClass.hang_ve, defaultClass.gia, getLabel(defaultClass.hang_ve))
}

