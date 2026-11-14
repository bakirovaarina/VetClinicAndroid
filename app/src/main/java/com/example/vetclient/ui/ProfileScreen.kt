package com.example.vetclient.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Person
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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.vetclient.network.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ProfileViewModel : ViewModel() {
    var profile by mutableStateOf<UserProfile?>(null)
    var isLoading by mutableStateOf(false)
    var message by mutableStateOf("")
    var isError by mutableStateOf(false)
    var isSuccess by mutableStateOf(false)

    var isPasswordChanged by mutableStateOf(false)

    fun loadProfile(userId: Int) {
        isLoading = true
        viewModelScope.launch {
            try {
                val response = ApiClient.apiService.getProfile(userId)
                if (response.isSuccessful) {
                    profile = response.body()
                }
            } catch (e: Exception) {
            } finally {
                isLoading = false
            }
        }
    }

    fun updateProfile(userId: Int, fullName: String, phone: String) {
        isLoading = true
        message = ""
        viewModelScope.launch {
            try {
                val response = ApiClient.apiService.updateProfile(
                    UpdateProfileRequest(userId, fullName, phone)
                )
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.status == "success") {
                        message = "Профиль обновлен"
                        isError = false
                        isSuccess = true
                        loadProfile(userId)
                    } else {
                        message = body?.message ?: "Ошибка"
                        isError = true
                    }
                } else {
                    message = "Ошибка сервера"
                    isError = true
                }
            } catch (e: Exception) {
                message = "Нет связи с сервером"
                isError = true
            } finally {
                isLoading = false
            }
        }
    }

    fun changePassword(userId: Int, oldPass: String, newPass: String) {
        isLoading = true
        message = ""
        isPasswordChanged = false
        viewModelScope.launch {
            try {
                val response = ApiClient.apiService.changePassword(
                    ChangePasswordRequest(userId, oldPass, newPass)
                )
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.status == "success") {
                        message = "Пароль изменен"
                        isError = false
                        isPasswordChanged = true
                    } else {
                        message = body?.message ?: "Ошибка"
                        isError = true
                    }
                } else {
                    message = "Ошибка сервера"
                    isError = true
                }
            } catch (e: Exception) {
                message = "Нет связи с сервером"
                isError = true
            } finally {
                isLoading = false
            }
        }
    }
}

@Composable
fun ProfileScreen(
    userId: Int,
    onLogout: () -> Unit,
    viewModel: ProfileViewModel = viewModel()
) {
    val profileNavController = rememberNavController()

    LaunchedEffect(userId) {
        viewModel.loadProfile(userId)
    }

    NavHost(navController = profileNavController, startDestination = "profile_main") {
        composable("profile_main") {
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp)
            ) {
                Text("Профиль", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                Spacer(modifier = Modifier.height(24.dp))

                if (viewModel.isLoading) {
                    CircularProgressIndicator(color = VetGreen)
                } else {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Person, contentDescription = null, tint = VetGreen, modifier = Modifier.size(40.dp))
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text(viewModel.profile?.full_name ?: "Загрузка...", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                    Text(viewModel.profile?.email ?: "", fontSize = 14.sp, color = Color.Gray)
                                    Text(viewModel.profile?.phone ?: "", fontSize = 14.sp, color = Color.Gray)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { profileNavController.navigate("edit_profile") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = VetGreen)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Редактировать профиль")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = { profileNavController.navigate("change_password") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                    ) {
                        Text("Сменить пароль")
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth().clickable { profileNavController.navigate("help") },
                        colors = CardDefaults.cardColors(containerColor = VetGreen)
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Favorite, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(16.dp))
                            Text("Помощь бездомным животным", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = { profileNavController.navigate("orders") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = VetGreen)
                    ) {
                        Text("Мои заказы")
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    Button(
                        onClick = onLogout,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Выйти из аккаунта")
                    }
                }
            }
        }

        composable("edit_profile") {
            EditProfileScreen(
                userId = userId,
                currentProfile = viewModel.profile,
                onSave = { fullName, phone ->
                    viewModel.updateProfile(userId, fullName, phone)
                    profileNavController.popBackStack()
                },
                onNavigateBack = { profileNavController.popBackStack() }
            )
        }

        composable("change_password") {
            ChangePasswordScreen(
                viewModel = viewModel,
                userId = userId,
                onNavigateBack = { profileNavController.popBackStack() }
            )
        }

        composable("help") {
            HelpScreen(
                userId = userId,
                onNavigateBack = { profileNavController.popBackStack() }
            )
        }

        composable("orders") {
            OrdersScreen(
                userId = userId,
                onNavigateBack = { profileNavController.popBackStack() }
            )
        }
    }
}

@Composable
fun EditProfileScreen(
    userId: Int,
    currentProfile: UserProfile?,
    onSave: (String, String) -> Unit,
    onNavigateBack: () -> Unit
) {
    val nameParts = currentProfile?.full_name?.split(" ") ?: emptyList()
    var lastName by remember { mutableStateOf(nameParts.getOrNull(0) ?: "") }
    var firstName by remember { mutableStateOf(nameParts.getOrNull(1) ?: "") }
    var middleName by remember { mutableStateOf(nameParts.getOrNull(2) ?: "") }
    var phone by remember { mutableStateOf(currentProfile?.phone ?: "") }

    var lastNameError by remember { mutableStateOf<String?>(null) }
    var firstNameError by remember { mutableStateOf<String?>(null) }
    var middleNameError by remember { mutableStateOf<String?>(null) }
    var phoneError by remember { mutableStateOf<String?>(null) }

    fun formatPhone(input: String): String {
        val digits = input.filter { it.isDigit() }
        val limited = if (digits.length > 11) digits.substring(0, 11) else digits
        val result = StringBuilder()
        if (limited.isNotEmpty()) {
            result.append("+7 ")
            val rest = limited.removePrefix("7")
            if (rest.isNotEmpty()) result.append("(${rest.substring(0, minOf(3, rest.length))}")
            if (rest.length > 3) result.append(") ${rest.substring(3, minOf(6, rest.length))}")
            if (rest.length > 6) result.append("-${rest.substring(6, minOf(8, rest.length))}")
            if (rest.length > 8) result.append("-${rest.substring(8, minOf(10, rest.length))}")
        }
        return result.toString()
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Редактирование профиля", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = lastName,
            onValueChange = { lastName = it; lastNameError = null },
            label = { Text("Фамилия") },
            isError = lastNameError != null,
            supportingText = { lastNameError?.let { Text(it) } },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = firstName,
            onValueChange = { firstName = it; firstNameError = null },
            label = { Text("Имя") },
            isError = firstNameError != null,
            supportingText = { firstNameError?.let { Text(it) } },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = middleName,
            onValueChange = { middleName = it; middleNameError = null },
            label = { Text("Отчество (необязательно)") },
            isError = middleNameError != null,
            supportingText = { middleNameError?.let { Text(it) } },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = phone,
            onValueChange = { phone = formatPhone(it); phoneError = null },
            label = { Text("Телефон") },
            isError = phoneError != null,
            supportingText = { phoneError?.let { Text(it) } },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                var isValid = true
                if (lastName.isBlank()) { lastNameError = "Поле не может быть пустым"; isValid = false }
                else if (!lastName.matches(Regex("^[А-Яа-яЁёA-Za-z-]+$"))) { lastNameError = "Только буквы и дефис"; isValid = false }

                if (firstName.isBlank()) { firstNameError = "Поле не может быть пустым"; isValid = false }
                else if (!firstName.matches(Regex("^[А-Яа-яЁёA-Za-z-]+$"))) { firstNameError = "Только буквы и дефис"; isValid = false }

                if (middleName.isNotBlank() && !middleName.matches(Regex("^[А-Яа-яЁёA-Za-z-]+$"))) {
                    middleNameError = "Только буквы и дефис"; isValid = false
                }

                val digits = phone.filter { it.isDigit() }
                if (digits.length < 11) { phoneError = "Введите номер полностью"; isValid = false }

                if (isValid) {
                    val fullName = "$lastName $firstName $middleName".trim()
                    onSave(fullName, phone)
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = VetGreen)
        ) {
            Text("Сохранить")
        }

        Spacer(modifier = Modifier.height(16.dp))
        TextButton(onClick = onNavigateBack) { Text("Назад", color = VetGreen) }
    }
}

@Composable
fun ChangePasswordScreen(
    viewModel: ProfileViewModel,
    userId: Int,
    onNavigateBack: () -> Unit
) {
    var oldPass by remember { mutableStateOf("") }
    var newPass by remember { mutableStateOf("") }
    var confirmPass by remember { mutableStateOf("") }

    var oldError by remember { mutableStateOf<String?>(null) }
    var newError by remember { mutableStateOf<String?>(null) }
    var confirmError by remember { mutableStateOf<String?>(null) }

    var oldVisible by remember { mutableStateOf(false) }
    var newVisible by remember { mutableStateOf(false) }
    var confirmVisible by remember { mutableStateOf(false) }

    LaunchedEffect(viewModel.isPasswordChanged) {
        if (viewModel.isPasswordChanged) {
            delay(1500)
            viewModel.isPasswordChanged = false
            viewModel.message = ""
            onNavigateBack()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Смена пароля", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = oldPass,
            onValueChange = { oldPass = it; oldError = null; viewModel.message = "" },
            label = { Text("Старый пароль") },
            visualTransformation = if (oldVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { oldVisible = !oldVisible }) {
                    Icon(if (oldVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff, contentDescription = null)
                }
            },
            isError = oldError != null,
            supportingText = { oldError?.let { Text(it) } },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = newPass,
            onValueChange = { newPass = it; newError = null; viewModel.message = "" },
            label = { Text("Новый пароль") },
            visualTransformation = if (newVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { newVisible = !newVisible }) {
                    Icon(if (newVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff, contentDescription = null)
                }
            },
            isError = newError != null,
            supportingText = { newError?.let { Text(it) } },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = confirmPass,
            onValueChange = { confirmPass = it; confirmError = null; viewModel.message = "" },
            label = { Text("Подтвердите пароль") },
            visualTransformation = if (confirmVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { confirmVisible = !confirmVisible }) {
                    Icon(if (confirmVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff, contentDescription = null)
                }
            },
            isError = confirmError != null,
            supportingText = { confirmError?.let { Text(it) } },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (viewModel.message.isNotEmpty()) {
            Text(
                viewModel.message,
                color = if (viewModel.isError) Color.Red else Color.Green,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        Button(
            onClick = {
                var isValid = true
                if (oldPass.isBlank()) { oldError = "Введите старый пароль"; isValid = false }

                if (newPass.length < 6) { newError = "Пароль должен быть не менее 6 символов"; isValid = false }
                else if (newPass.contains(" ")) { newError = "Пароль не должен содержать пробелов"; isValid = false }

                if (newPass != confirmPass) { confirmError = "Пароли не совпадают"; isValid = false }

                if (isValid) viewModel.changePassword(userId, oldPass, newPass)
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = VetGreen)
        ) {
            Text("Сменить пароль")
        }

        Spacer(modifier = Modifier.height(16.dp))
        TextButton(onClick = onNavigateBack) { Text("Назад", color = VetGreen) }
    }
}