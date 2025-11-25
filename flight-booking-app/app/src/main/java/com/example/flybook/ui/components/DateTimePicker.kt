package com.example.flybook.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateTimePicker(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    errorMessage: String? = null,
    supportingText: String? = null
) {
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    
    val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")
    val displayFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
    
    val currentDateTime = try {
        if (value.isNotEmpty()) {
            LocalDateTime.parse(value, dateFormatter)
        } else {
            LocalDateTime.now()
        }
    } catch (e: Exception) {
        LocalDateTime.now()
    }
    
    var selectedDate by remember { mutableStateOf(currentDateTime.toLocalDate()) }
    var selectedTime by remember { mutableStateOf(currentDateTime.toLocalTime()) }
    
    OutlinedTextField(
        value = try {
            if (value.isNotEmpty()) {
                LocalDateTime.parse(value, dateFormatter).format(displayFormatter)
            } else {
                ""
            }
        } catch (e: Exception) {
            value
        },
        onValueChange = { },
        modifier = modifier
            .fillMaxWidth()
            .clickable { showDatePicker = true },
        label = { Text(label) },
        readOnly = true,
        trailingIcon = {
            IconButton(onClick = { showDatePicker = true }) {
                Icon(Icons.Default.ArrowDropDown, contentDescription = "Chọn ngày giờ")
            }
        },
        isError = isError,
        supportingText = {
            if (errorMessage != null) {
                Text(errorMessage, color = MaterialTheme.colorScheme.error)
            } else if (supportingText != null) {
                Text(supportingText, fontSize = 10.sp)
            }
        },
        shape = MaterialTheme.shapes.medium
    )
    
    if (showDatePicker) {
        val calendar = Calendar.getInstance()
        calendar.set(selectedDate.year, selectedDate.monthValue - 1, selectedDate.dayOfMonth)
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = calendar.timeInMillis
        )
        
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            onConfirm = {
                datePickerState.selectedDateMillis?.let { millis ->
                    val instant = java.time.Instant.ofEpochMilli(millis)
                    selectedDate = instant.atZone(java.time.ZoneId.systemDefault()).toLocalDate()
                    showDatePicker = false
                    showTimePicker = true
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Hủy")
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val instant = java.time.Instant.ofEpochMilli(millis)
                        selectedDate = instant.atZone(java.time.ZoneId.systemDefault()).toLocalDate()
                        showDatePicker = false
                        showTimePicker = true
                    }
                }) {
                    Text("Chọn")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
    
    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = selectedTime.hour,
            initialMinute = selectedTime.minute
        )
        
        TimePickerDialog(
            onDismissRequest = { showTimePicker = false },
            onConfirm = {
                selectedTime = java.time.LocalTime.of(
                    timePickerState.hour,
                    timePickerState.minute
                )
                val dateTime = LocalDateTime.of(selectedDate, selectedTime)
                onValueChange(dateTime.format(dateFormatter))
                showTimePicker = false
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("Hủy")
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    selectedTime = java.time.LocalTime.of(
                        timePickerState.hour,
                        timePickerState.minute
                    )
                    val dateTime = LocalDateTime.of(selectedDate, selectedTime)
                    onValueChange(dateTime.format(dateFormatter))
                    showTimePicker = false
                }) {
                    Text("Xác nhận")
                }
            }
        ) {
            TimePicker(state = timePickerState)
        }
    }
}

@Composable
fun DatePickerDialog(
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit,
    dismissButton: @Composable () -> Unit,
    confirmButton: @Composable () -> Unit,
    content: @Composable () -> Unit
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Card(
            modifier = Modifier
                .wrapContentWidth()
                .wrapContentHeight(),
            shape = MaterialTheme.shapes.large
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                content()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    dismissButton()
                    Spacer(modifier = Modifier.width(8.dp))
                    confirmButton()
                }
            }
        }
    }
}

@Composable
fun TimePickerDialog(
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit,
    dismissButton: @Composable () -> Unit,
    confirmButton: @Composable () -> Unit,
    content: @Composable () -> Unit
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Card(
            modifier = Modifier
                .wrapContentWidth()
                .wrapContentHeight(),
            shape = MaterialTheme.shapes.large
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                content()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    dismissButton()
                    Spacer(modifier = Modifier.width(8.dp))
                    confirmButton()
                }
            }
        }
    }
}

