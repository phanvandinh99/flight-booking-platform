package com.example.flybook.ui.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.flybook.data.models.RegisterRequest
import com.example.flybook.data.repository.AuthRepository
import com.example.flybook.navigation.Screen
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
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Đăng ký",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Text(
            text = "Tạo tài khoản mới",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 32.dp)
        )
        
        errorMessage?.let {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = it,
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
        
        OutlinedTextField(
            value = tenDayDu,
            onValueChange = { tenDayDu = it },
            label = { Text("Họ và tên") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            singleLine = true,
            enabled = !isLoading
        )
        
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            singleLine = true,
            enabled = !isLoading
        )
        
        OutlinedTextField(
            value = soDienThoai,
            onValueChange = { soDienThoai = it },
            label = { Text("Số điện thoại") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            singleLine = true,
            enabled = !isLoading
        )
        
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Mật khẩu") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            enabled = !isLoading
        )
        
        OutlinedTextField(
            value = passwordConfirmation,
            onValueChange = { passwordConfirmation = it },
            label = { Text("Xác nhận mật khẩu") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            enabled = !isLoading
        )
        
        Button(
            onClick = {
                if (tenDayDu.isBlank() || email.isBlank() || password.isBlank()) {
                    errorMessage = "Vui lòng nhập đầy đủ thông tin"
                    return@Button
                }
                
                if (password != passwordConfirmation) {
                    errorMessage = "Mật khẩu xác nhận không khớp"
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
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Đăng ký")
            }
        }
        
        TextButton(
            onClick = { navController.navigate(Screen.Login.route) },
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text("Đã có tài khoản? Đăng nhập")
        }
    }
}

