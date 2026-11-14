package com.example.vetclient.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.vetclient.network.ApiClient
import com.example.vetclient.network.RegisterRequest
import kotlinx.coroutines.launch

class RegisterViewModel : ViewModel() {
    var isLoading by mutableStateOf(false)
    var message by mutableStateOf("")
    var isError by mutableStateOf(false)
    var registrationSuccess by mutableStateOf(false)

    fun register(lastName: String, firstName: String, middleName: String, email: String, password: String, phone: String) {
        isLoading = true
        message = ""
        val fullName = "$lastName $firstName $middleName".trim()
        viewModelScope.launch {
            try {
                val response = ApiClient.apiService.registerUser(
                    RegisterRequest(fullName, email, password, phone)
                )
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.status == "success") {
                        message = "Успешно! Теперь вы можете войти."
                        isError = false
                        registrationSuccess = true
                    } else {
                        message = body?.message ?: "Ошибка"
                        isError = true
                    }
                } else {
                    message = "Ошибка сервера: ${response.code()}"
                    isError = true
                }
            } catch (e: Exception) {
                message = "Нет связи с сервером."
                isError = true
            } finally {
                isLoading = false
            }
        }
    }
}

@Composable
fun RegisterScreen(
    viewModel: RegisterViewModel = viewModel(),
    onNavigateToLogin: () -> Unit
) {
    var lastName by remember { mutableStateOf("") }
    var firstName by remember { mutableStateOf("") }
    var middleName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    var lastNameError by remember { mutableStateOf<String?>(null) }
    var firstNameError by remember { mutableStateOf<String?>(null) }
    var middleNameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var phoneError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(viewModel.registrationSuccess) {
        if (viewModel.registrationSuccess) {
            kotlinx.coroutines.delay(1500)
            onNavigateToLogin()
        }
    }

    fun formatPhone(input: String): String {
        val digits = input.filter { it.isDigit() }
        val limited = if (digits.length > 11) digits.substring(0, 11) else digits
        val result = StringBuilder()
        if (limited.isNotEmpty()) {
            result.append("+7 ")
            val rest = limited.removePrefix("7")
            if (rest.isNotEmpty()) {
                result.append("(${rest.substring(0, minOf(3, rest.length))}")
            }
            if (rest.length > 3) {
                result.append(") ${rest.substring(3, minOf(6, rest.length))}")
            }
            if (rest.length > 6) {
                result.append("-${rest.substring(6, minOf(8, rest.length))}")
            }
            if (rest.length > 8) {
                result.append("-${rest.substring(8, minOf(10, rest.length))}")
            }
        }
        return result.toString()
    }

    fun validate(): Boolean {
        lastNameError = null
        firstNameError = null
        middleNameError = null
        emailError = null
        phoneError = null
        passwordError = null
        var isValid = true

        if (lastName.isBlank()) {
            lastNameError = "Поле не может быть пустым"
            isValid = false
        } else if (!lastName.matches(Regex("^[А-Яа-яЁёA-Za-z- ]+$"))) {
            lastNameError = "Фамилия должна содержать только буквы и дефис"
            isValid = false
        }
        if (firstName.isBlank()) {
            firstNameError = "Поле не может быть пустым"
            isValid = false
        } else if (!firstName.matches(Regex("^[А-Яа-яЁёA-Za-z-]+$"))) {
            firstNameError = "Имя должно содержать только буквы и дефис"
            isValid = false
        }
        if (middleName.isNotBlank() && !middleName.matches(Regex("^[А-Яа-яЁёA-Za-z-]+$"))) {
            middleNameError = "Отчество должно содержать только буквы и дефис"
            isValid = false
        }

        if (email.isBlank()) {
            emailError = "Поле не может быть пустым"
            isValid = false
        } else {
            val emailRegex = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$")
            if (!emailRegex.matches(email)) {
                emailError = "Некорректный email. Пример: name@mail.ru"
                isValid = false
            }
        }

        val digits = phone.filter { it.isDigit() }
        if (phone.isBlank()) {
            phoneError = "Поле не может быть пустым"
            isValid = false
        } else if (digits.length < 11) {
            phoneError = "Введите номер телефона полностью"
            isValid = false
        }

        if (password.isBlank()) {
            passwordError = "Поле не может быть пустым"
            isValid = false
        } else if (password.length < 6) {
            passwordError = "Пароль должен быть не менее 6 символов"
            isValid = false
        } else if (password.contains(" ")) {
            passwordError = "Пароль не должен содержать пробелов"
            isValid = false
        }

        return isValid
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
            Text(
                text = "Регистрация",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF00897B),
                modifier = Modifier.padding(bottom = 24.dp)
            )

            OutlinedTextField(
                value = lastName,
                onValueChange = {
                    lastName = it
                    lastNameError = null
                },
                label = { Text("Фамилия") },
                isError = lastNameError != null,
                supportingText = { lastNameError?.let { Text(it) } },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = firstName,
                onValueChange = {
                    firstName = it
                    firstNameError = null
                },
                label = { Text("Имя") },
                isError = firstNameError != null,
                supportingText = { firstNameError?.let { Text(it) } },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = middleName,
                onValueChange = {
                    middleName = it
                    middleNameError = null
                },
                label = { Text("Отчество (необязательно)") },
                isError = middleNameError != null,
                supportingText = { middleNameError?.let { Text(it) } },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    emailError = null
                },
                label = { Text("Email") },
                isError = emailError != null,
                supportingText = { emailError?.let { Text(it) } },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = phone,
                onValueChange = {
                    phone = formatPhone(it)
                    phoneError = null
                },
                label = { Text("Телефон") },
                isError = phoneError != null,
                supportingText = { phoneError?.let { Text(it) } },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    passwordError = null
                },
                label = { Text("Пароль") },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = if (passwordVisible) "Скрыть пароль" else "Показать пароль",
                            tint = Color.Gray
                        )
                    }
                },
                isError = passwordError != null,
                supportingText = { passwordError?.let { Text(it) } },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (viewModel.isLoading) {
                CircularProgressIndicator(color = Color(0xFF00897B))
            } else {
                Button(
                    onClick = {
                        if (validate()) {
                            viewModel.register(lastName, firstName, middleName, email, password, phone)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00897B))
                ) {
                    Text("Зарегистрироваться", fontSize = 18.sp)
                }
            }

            if (viewModel.message.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = viewModel.message,
                    color = if (viewModel.isError) Color.Red else Color.Green
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = onNavigateToLogin) {
                Text("У меня уже есть аккаунт", color = Color(0xFF00897B))
            }
        }
    }
}