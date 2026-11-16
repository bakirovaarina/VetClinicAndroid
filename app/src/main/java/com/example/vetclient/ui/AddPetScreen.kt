package com.example.vetclient.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.vetclient.network.AddPetRequest
import com.example.vetclient.network.ApiClient
import com.example.vetclient.network.Pet
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream

class AddPetViewModel : ViewModel() {
    var isLoading by mutableStateOf(false)
    var message by mutableStateOf("")
    var isError by mutableStateOf(false)
    var isSuccess by mutableStateOf(false)

    fun savePet(
        userId: Int,
        name: String,
        species: String,
        breed: String,
        weight: String,
        age: String,
        petId: Int? = null,
        onSuccess: (Int) -> Unit
    ) {
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
                    weight_kg = weight.ifBlank { null },
                    age = age.ifBlank { null }
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
                        val returnedId = body.pet_id ?: petId ?: 0
                        onSuccess(returnedId)
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

    fun uploadPhoto(petId: Int, file: File) {
        isLoading = true
        message = ""
        viewModelScope.launch {
            try {
                val requestBody = file.asRequestBody("image/*".toMediaTypeOrNull())
                val photoPart = MultipartBody.Part.createFormData("photo", file.name, requestBody)
                val petIdPart = petId.toString().toRequestBody("text/plain".toMediaTypeOrNull())

                val response = ApiClient.apiService.uploadPhoto(petIdPart, photoPart)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.status == "success") {
                        message = "Фото загружено!"
                        isError = false
                        isSuccess = true
                    } else {
                        message = body?.message ?: "Ошибка загрузки"
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
    var age by remember { mutableStateOf("") }

    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var savedPetId by remember { mutableStateOf<Int?>(petToEdit?.id) }

    var nameError by remember { mutableStateOf<String?>(null) }
    var speciesError by remember { mutableStateOf<String?>(null) }
    var weightError by remember { mutableStateOf<String?>(null) }
    var ageError by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
        }
    }

    LaunchedEffect(viewModel.isSuccess) {
        if (viewModel.isSuccess && !viewModel.isLoading) {
            kotlinx.coroutines.delay(1500)
            onNavigateBack()
        }
    }

    fun validate(): Boolean {
        nameError = null
        speciesError = null
        weightError = null
        ageError = null
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

        if (age.isNotBlank()) {
            val ageValue = age.toIntOrNull()
            if (ageValue == null || ageValue <= 0 || ageValue > 50) {
                ageError = "Введите корректный возраст (от 1 до 50 лет)"
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

        Box(
            modifier = Modifier
                .size(120.dp)
                .background(Color.LightGray.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                .clickable { launcher.launch("image/*") },
            contentAlignment = Alignment.Center
        ) {
            if (selectedImageUri != null) {
                Image(
                    painter = rememberAsyncImagePainter(selectedImageUri),
                    contentDescription = "Фото питомца",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else if (petToEdit?.image_url != null) {
                Image(
                    painter = rememberAsyncImagePainter(petToEdit.image_url),
                    contentDescription = "Фото питомца",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(Icons.Default.PhotoCamera, contentDescription = "Добавить фото", tint = Color.Gray, modifier = Modifier.size(40.dp))
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it; nameError = null },
            label = { Text("Кличка") },
            isError = nameError != null,
            supportingText = { nameError?.let { Text(it) } },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = species,
            onValueChange = { species = it; speciesError = null },
            label = { Text("Вид животного") },
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
            onValueChange = { if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) { weight = it; weightError = null } },
            label = { Text("Вес (кг, необязательно)") },
            isError = weightError != null,
            supportingText = { weightError?.let { Text(it) } },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = age,
            onValueChange = { if (it.isEmpty() || it.matches(Regex("^\\d*$"))) { age = it; ageError = null } },
            label = { Text("Возраст (лет, необязательно)") },
            isError = ageError != null,
            supportingText = { ageError?.let { Text(it) } },
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
                        viewModel.savePet(
                            userId = userId,
                            name = name,
                            species = species,
                            breed = breed,
                            weight = weight,
                            age = age,
                            petId = petToEdit?.id,
                            onSuccess = { id ->
                                savedPetId = id
                                if (selectedImageUri != null) {
                                    val file = uriToFile(context, selectedImageUri!!)
                                    if (file != null) {
                                        viewModel.uploadPhoto(id, file)
                                    } else {
                                    }
                                }
                            }
                        )
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
                color = if (viewModel.isError) Color.Red else Color.Green
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = onNavigateBack) {
            Text("Назад", color = VetGreen)
        }
    }
}

fun uriToFile(context: android.content.Context, uri: Uri): File? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null
        val file = File(context.cacheDir, "upload_${System.currentTimeMillis()}.jpg")
        val outputStream = FileOutputStream(file)
        inputStream.copyTo(outputStream)
        inputStream.close()
        outputStream.close()
        file
    } catch (e: Exception) {
        null
    }
}