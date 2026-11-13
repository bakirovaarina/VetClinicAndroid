package com.example.vetclient.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.vetclient.network.ApiClient
import com.example.vetclient.network.RecoverRequest
import kotlinx.coroutines.launch

class ForgotViewModel : ViewModel() {
    var isLoading by mutableStateOf(false)
    var message by mutableStateOf("")
    var isError by mutableStateOf(false)

    fun recover(email: String) {
        isLoading = true
        message = ""
        viewModelScope.launch {
            try {
                val response = ApiClient.apiService.recoverPassword(RecoverRequest(email))
                if (response.isSuccessful) {
                    val body = response.body()
                    message = body?.message ?: "Ошибка"
                    isError = body?.status != "success"
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
fun ForgotPasswordScreen(
    viewModel: ForgotViewModel = viewModel(),
    onNavigateBack: () -> Unit
) {
    var email by remember { mutableStateOf("") }

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
            Text(
                text = "Восстановление пароля",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF00897B),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Text(
                text = "Введите email, указанный при регистрации. Мы вышлем новый пароль.",
                fontSize = 14.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            OutlinedTextField(
                value = email,
                onValueChange = { newValue: String -> email = newValue },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (viewModel.isLoading) {
                CircularProgressIndicator(color = Color(0xFF00897B))
            } else {
                Button(
                    onClick = { viewModel.recover(email) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00897B))
                ) {
                    Text("Отправить", fontSize = 18.sp)
                }
            }

            if (viewModel.message.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = viewModel.message,
                    color = if (viewModel.isError) Color.Red else Color.Green,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            TextButton(onClick = onNavigateBack) {
                Text("Вернуться ко входу", color = Color(0xFF00897B))
            }
        }
    }
}