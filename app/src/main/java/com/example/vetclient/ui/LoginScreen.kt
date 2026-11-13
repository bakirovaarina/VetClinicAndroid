package com.example.vetclient.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.vetclient.network.ApiClient
import com.example.vetclient.network.LoginRequest
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {
    var isLoading by mutableStateOf(false)
    var message by mutableStateOf("")
    var isError by mutableStateOf(false)
    var loggedInUserName by mutableStateOf<String?>(null)
    var loggedInUserId by mutableStateOf<Int?>(null)

    fun login(email: String, password: String) {
        isLoading = true
        message = ""
        viewModelScope.launch {
            try {
                val response = ApiClient.apiService.loginUser(LoginRequest(email, password))
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.status == "success") {
                        message = body.message
                        isError = false
                        loggedInUserName = body.full_name
                        loggedInUserId = body.user_id
                    } else {
                        message = body?.message ?: "Ошибка входа"
                        isError = true
                    }
                } else {
                    message = "Ошибка сервера: ${response.code()}"
                    isError = true
                }
            } catch (e: Exception) {
                message = "Нет связи с сервером. Проверьте Laragon."
                isError = true
            } finally {
                isLoading = false
            }
        }
    }
}

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = viewModel(),
    onLoginSuccess: (String, Int) -> Unit,
    onNavigateToRegister: () -> Unit,
    onNavigateToForgot: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    LaunchedEffect(viewModel.loggedInUserName, viewModel.loggedInUserId) {
        if (viewModel.loggedInUserName != null && viewModel.loggedInUserId != null) {
            onLoginSuccess(viewModel.loggedInUserName!!, viewModel.loggedInUserId!!)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F9F9)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = "Логотип",
                tint = Color(0xFF00897B),
                modifier = Modifier.size(80.dp)
            )
            Text(
                text = "ВетПолис",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF00897B),
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = "Забота о ваших питомцах",
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 40.dp)
            )

            OutlinedTextField(
                value = email,
                onValueChange = { newValue: String -> email = newValue },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { newValue: String -> password = newValue },
                label = { Text("Пароль") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Забыли пароль?",
                color = Color(0xFF00897B),
                fontSize = 14.sp,
                modifier = Modifier
                    .align(Alignment.End)
                    .clickable { onNavigateToForgot() }
            )

            Spacer(modifier = Modifier.height(32.dp))

            if (viewModel.isLoading) {
                CircularProgressIndicator(color = Color(0xFF00897B))
            } else {
                Button(
                    onClick = { viewModel.login(email, password) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00897B))
                ) {
                    Text("Войти", fontSize = 18.sp)
                }
            }

            if (viewModel.message.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = viewModel.message,
                    color = if (viewModel.isError) Color.Red else Color.Green
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            TextButton(onClick = onNavigateToRegister) {
                Text("Нет аккаунта? Зарегистрироваться", color = Color(0xFF00897B))
            }
        }
    }
}