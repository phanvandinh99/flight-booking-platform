package com.example.flybook.ui.screens.auth

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.flybook.data.models.RegisterRequest
import com.example.flybook.data.repository.AuthRepository
import com.example.flybook.navigation.Screen
import com.example.flybook.ui.theme.*
import com.example.flybook.util.AuthManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(navController: NavController) {
    var tenDayDu by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var soDienThoai by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordConfirmation by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    val scope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current
    val authRepository = remember { AuthRepository() }
    
    // Animation for floating shapes
    val infiniteTransition = rememberInfiniteTransition(label = "floating")
    val floatAnimation1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float1"
    )
    val floatAnimation2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(15000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float2"
    )
    val floatAnimation3 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(18000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float3"
    )
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(PrimaryBlue, PrimaryPurple)
                )
            )
    ) {
        // Floating background shapes
        FloatingShape(
            modifier = Modifier
                .offset(x = (-100 + floatAnimation1 * 100).dp, y = (-100 + floatAnimation1 * 50).dp)
                .size(300.dp),
            color = Color.White.copy(alpha = 0.1f)
        )
        FloatingShape(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = (50 - floatAnimation2 * 50).dp, y = (50 - floatAnimation2 * 30).dp)
                .size(200.dp),
            color = Color.White.copy(alpha = 0.1f)
        )
        FloatingShape(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = (-30 + floatAnimation3 * 30).dp, y = (100 + floatAnimation3 * 20).dp)
                .size(150.dp),
            color = Color.White.copy(alpha = 0.1f)
        )
        
        // Main content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Register Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 20.dp,
                        shape = RoundedCornerShape(24.dp),
                        spotColor = Color.Black.copy(alpha = 0.3f)
                    ),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.95f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Logo
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(PrimaryBlue, PrimaryPurple)
                                ),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .shadow(
                                elevation = 8.dp,
                                shape = RoundedCornerShape(16.dp),
                                spotColor = PrimaryBlue.copy(alpha = 0.4f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "✈️",
                            fontSize = 28.sp,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    
                    // Title
                    Text(
                        text = "Đăng ký",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 28.sp,
                            letterSpacing = (-0.5).sp
                        ),
                        color = TextPrimary
                    )
                    
                    // Subtitle
                    Text(
                        text = "Tạo tài khoản mới",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 14.sp
                        ),
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )
                    
                    // Error message
                    errorMessage?.let { error ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = ErrorBackground
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = error,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontSize = 14.sp
                                    ),
                                    color = ErrorRed,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                    
                    // Full name field
                    OutlinedTextField(
                        value = tenDayDu,
                        onValueChange = { tenDayDu = it },
                        label = {
                            Text(
                                "Họ và tên",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Họ và tên",
                                tint = if (tenDayDu.isNotBlank()) PrimaryBlue else TextSecondary
                            )
                        },
                        placeholder = {
                            Text(
                                "Nguyễn Văn A",
                                color = PlaceholderGrey
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = !isLoading,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryBlue,
                            unfocusedBorderColor = BorderLight,
                            focusedLabelColor = PrimaryBlue,
                            unfocusedLabelColor = TextTertiary
                        )
                    )
                    
                    // Email field
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = {
                            Text(
                                "Email",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = "Email",
                                tint = if (email.isNotBlank()) PrimaryBlue else TextSecondary
                            )
                        },
                        placeholder = {
                            Text(
                                "you@example.com",
                                color = PlaceholderGrey
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = !isLoading,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryBlue,
                            unfocusedBorderColor = BorderLight,
                            focusedLabelColor = PrimaryBlue,
                            unfocusedLabelColor = TextTertiary
                        )
                    )
                    
                    // Phone field
                    OutlinedTextField(
                        value = soDienThoai,
                        onValueChange = { soDienThoai = it },
                        label = {
                            Text(
                                "Số điện thoại",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Phone,
                                contentDescription = "Số điện thoại",
                                tint = if (soDienThoai.isNotBlank()) PrimaryBlue else TextSecondary
                            )
                        },
                        placeholder = {
                            Text(
                                "0123456789",
                                color = PlaceholderGrey
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = !isLoading,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryBlue,
                            unfocusedBorderColor = BorderLight,
                            focusedLabelColor = PrimaryBlue,
                            unfocusedLabelColor = TextTertiary
                        )
                    )
                    
                    // Password field
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = {
                            Text(
                                "Mật khẩu",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Mật khẩu",
                                tint = if (password.isNotBlank()) PrimaryBlue else TextSecondary
                            )
                        },
                        placeholder = {
                            Text(
                                "Nhập mật khẩu",
                                color = PlaceholderGrey
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        enabled = !isLoading,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryBlue,
                            unfocusedBorderColor = BorderLight,
                            focusedLabelColor = PrimaryBlue,
                            unfocusedLabelColor = TextTertiary
                        )
                    )
                    
                    // Password confirmation field
                    OutlinedTextField(
                        value = passwordConfirmation,
                        onValueChange = { passwordConfirmation = it },
                        label = {
                            Text(
                                "Xác nhận mật khẩu",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Xác nhận mật khẩu",
                                tint = if (passwordConfirmation.isNotBlank()) PrimaryBlue else TextSecondary
                            )
                        },
                        placeholder = {
                            Text(
                                "Nhập lại mật khẩu",
                                color = PlaceholderGrey
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        enabled = !isLoading,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryBlue,
                            unfocusedBorderColor = BorderLight,
                            focusedLabelColor = PrimaryBlue,
                            unfocusedLabelColor = TextTertiary
                        )
                    )
                    
                    // Register button
                    Button(
                        onClick = {
                            if (tenDayDu.isBlank() || email.isBlank() || soDienThoai.isBlank() || password.isBlank()) {
                                errorMessage = "Vui lòng nhập đầy đủ thông tin"
                                return@Button
                            }
                            
                            if (password != passwordConfirmation) {
                                errorMessage = "Mật khẩu xác nhận không khớp"
                                return@Button
                            }
                            
                            if (password.length < 8) {
                                errorMessage = "Mật khẩu phải có ít nhất 8 ký tự"
                                return@Button
                            }
                            
                            isLoading = true
                            errorMessage = null
                            
                            scope.launch {
                                val request = RegisterRequest(
                                    ten_day_du = tenDayDu,
                                    email = email,
                                    so_dien_thoai = soDienThoai,
                                    password = password,
                                    password_confirmation = passwordConfirmation
                                )
                                
                                authRepository.register(request)
                                    .onSuccess { authResponse ->
                                        AuthManager.saveToken(context, authResponse.token)
                                        AuthManager.saveUserEmail(context, authResponse.user.email)
                                        navController.navigate(Screen.Home.route) {
                                            popUpTo(Screen.Register.route) { inclusive = true }
                                        }
                                    }
                                    .onFailure { e ->
                                        errorMessage = e.message ?: "Đăng ký thất bại"
                                        isLoading = false
                                    }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        enabled = !isLoading,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent
                        )
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    brush = Brush.linearGradient(
                                        colors = listOf(PrimaryBlue, PrimaryPurple)
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    text = "Đăng ký",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 16.sp
                                    ),
                                    color = Color.White
                                )
                            }
                        }
                    }
                    
                    // Footer
                    Divider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = BorderLight
                    )
                    
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Đã có tài khoản? ",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 14.sp
                            ),
                            color = TextSecondary
                        )
                        TextButton(
                            onClick = { navController.navigate(Screen.Login.route) },
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "Đăng nhập ngay",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp
                                ),
                                color = PrimaryBlue
                            )
                        }
                    }
                }
            }
        }
    }
}


