package com.example.vetclient.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.vetclient.network.AddPetRequest
import com.example.vetclient.network.ApiClient
import com.example.vetclient.network.Pet
import kotlinx.coroutines.launch

class AddPetViewModel : ViewModel() {
    var isLoading by mutableStateOf(false)
    var message by mutableStateOf("")
    var isError by mutableStateOf(false)
    var isSuccess by mutableStateOf(false)

    fun savePet(userId: Int, name: String, species: String, breed: String, weight: String, petId: Int? = null) {
        isLoading = true
        message = ""
        viewModelScope.launch {
            try {
                val request = AddPetRequest(
                    id = petId,
                    user_id = userId,
                    name = name,
                    breed = breed.ifBlank { null },
                    species = species,
                    weight_kg = weight.ifBlank { null }
                )

                val response = if (petId == null) {
                    ApiClient.apiService.addPet(request)
                } else {
                    ApiClient.apiService.updatePet(request)
                }

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.status == "success") {
                        message = if (petId == null) "Питомец добавлен!" else "Данные обновлены!"
                        isError = false
                        isSuccess = true
                    } else {
                        message = body?.message ?: "Ошибка"
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
fun AddPetScreen(
    userId: Int,
    petToEdit: Pet? = null,
    onNavigateBack: () -> Unit,
    viewModel: AddPetViewModel = viewModel()
) {
    var name by remember { mutableStateOf(petToEdit?.name ?: "") }
    var breed by remember { mutableStateOf(petToEdit?.breed ?: "") }
    var weight by remember { mutableStateOf(petToEdit?.weight_kg ?: "") }
    var species by remember { mutableStateOf(petToEdit?.species ?: "") }

    var nameError by remember { mutableStateOf<String?>(null) }
    var speciesError by remember { mutableStateOf<String?>(null) }
    var weightError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(viewModel.isSuccess) {
        if (viewModel.isSuccess) {
            kotlinx.coroutines.delay(1500)
            onNavigateBack()
        }
    }

    fun validate(): Boolean {
        nameError = null
        speciesError = null
        weightError = null
        var isValid = true

        if (name.isBlank()) {
            nameError = "Поле не может быть пустым"
            isValid = false
        } else if (name.trim().length < 2) {
            nameError = "Кличка должна быть не менее 2 символов"
            isValid = false
        } else if (!name.matches(Regex("^[А-Яа-яЁёA-Za-z ]+$"))) {
            nameError = "Кличка может содержать только буквы"
            isValid = false
        }

        if (species.isBlank()) {
            speciesError = "Поле не может быть пустым"
            isValid = false
        } else if (species.trim().length < 2) {
            speciesError = "Введите вид животного (минимум 2 символа)"
            isValid = false
        } else if (!species.matches(Regex("^[А-Яа-яЁёA-Za-z ]+$"))) {
            speciesError = "Вид должен содержать только буквы"
            isValid = false
        }

        if (weight.isNotBlank()) {
            val weightValue = weight.toDoubleOrNull()
            if (weightValue == null || weightValue <= 0 || weightValue > 100) {
                weightError = "Введите корректный вес (от 0.1 до 100 кг)"
                isValid = false
            }
        }
        return isValid
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(if (petToEdit == null) "Добавить питомца" else "Редактировать питомца", fontSize = 24.sp)
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = name,
            onValueChange = {
                name = it
                nameError = null
            },
            label = { Text("Кличка") },
            isError = nameError != null,
            supportingText = { nameError?.let { Text(it) } },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = species,
            onValueChange = {
                species = it
                speciesError = null
            },
            label = { Text("Вид животного (например, Собака)") },
            isError = speciesError != null,
            supportingText = { speciesError?.let { Text(it) } },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = breed,
            onValueChange = { breed = it },
            label = { Text("Порода (необязательно)") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = weight,
            onValueChange = {
                if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) {
                    weight = it
                    weightError = null
                }
            },
            label = { Text("Вес (кг, необязательно)") },
            isError = weightError != null,
            supportingText = { weightError?.let { Text(it) } },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (viewModel.isLoading) {
            CircularProgressIndicator(color = VetGreen)
        } else {
            Button(
                onClick = {
                    if (validate()) {
                        viewModel.savePet(userId, name, species, breed, weight, petToEdit?.id)
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = VetGreen)
            ) {
                Text(if (petToEdit == null) "Сохранить" else "Обновить", fontSize = 18.sp)
            }
        }

        if (viewModel.message.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = viewModel.message,
                color = if (viewModel.isError) androidx.compose.ui.graphics.Color.Red else androidx.compose.ui.graphics.Color.Green
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = onNavigateBack) {
            Text("Назад", color = VetGreen)
        }
    }
}