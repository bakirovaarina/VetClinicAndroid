package com.example.vetclient.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.vetclient.network.ApiClient
import com.example.vetclient.network.DeletePetRequest
import com.example.vetclient.network.Pet
import kotlinx.coroutines.launch

class PetsViewModel : ViewModel() {
    var pets by mutableStateOf<List<Pet>>(emptyList())
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf("")
    var editingPet by mutableStateOf<Pet?>(null)

    fun loadPets(userId: Int) {
        isLoading = true
        errorMessage = ""
        viewModelScope.launch {
            try {
                val response = ApiClient.apiService.getPets(userId)
                if (response.isSuccessful) {
                    pets = response.body() ?: emptyList()
                } else {
                    errorMessage = "Ошибка загрузки: ${response.code()}"
                }
            } catch (e: Exception) {
                errorMessage = "Нет связи с сервером."
            } finally {
                isLoading = false
            }
        }
    }

    fun deletePet(petId: Int, userId: Int) {
        viewModelScope.launch {
            try {
                val response = ApiClient.apiService.deletePet(DeletePetRequest(petId))
                if (response.isSuccessful) {
                    loadPets(userId)
                }
            } catch (e: Exception) {
                errorMessage = "Ошибка удаления"
            }
        }
    }
}

@Composable
fun PetsScreen(
    userId: Int,
    onNavigateToAdd: () -> Unit,
    onNavigateToEdit: (Pet) -> Unit,
    viewModel: PetsViewModel = viewModel()
) {
    LaunchedEffect(userId) {
        viewModel.loadPets(userId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Мои питомцы", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
            Button(onClick = onNavigateToAdd, colors = ButtonDefaults.buttonColors(containerColor = VetGreen)) {
                Icon(Icons.Default.Add, contentDescription = "Добавить", modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Добавить")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (viewModel.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = VetGreen)
            }
        } else if (viewModel.errorMessage.isNotEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = viewModel.errorMessage, color = Color.Red)
            }
        } else if (viewModel.pets.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "У вас пока нет питомцев.\nНажмите «Добавить», чтобы создать карточку.",
                    color = Color.Gray,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(viewModel.pets) { pet ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(pet.name, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = VetGreen)
                                Text("${pet.species} • ${pet.breed ?: "Порода не указана"}", fontSize = 14.sp, color = Color.Gray)
                                if (pet.weight_kg != null) {
                                    Text("Вес: ${pet.weight_kg} кг", fontSize = 14.sp, color = Color.Gray)
                                }
                            }
                            IconButton(onClick = { onNavigateToEdit(pet) }) {
                                Icon(Icons.Default.Edit, contentDescription = "Редактировать", tint = VetGreen)
                            }
                            IconButton(onClick = { viewModel.deletePet(pet.id, userId) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Удалить", tint = Color.Red)
                            }
                        }
                    }
                }
            }
        }
    }
}