package com.example.vetclient.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.example.vetclient.network.HistoryItem
import kotlinx.coroutines.launch

class HistoryViewModel : ViewModel() {
    var history by mutableStateOf<List<HistoryItem>>(emptyList())
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf("")

    fun loadHistory(petId: Int) {
        isLoading = true
        viewModelScope.launch {
            try {
                val response = ApiClient.apiService.getHistory(petId)
                if (response.isSuccessful) {
                    history = response.body() ?: emptyList()
                } else {
                    errorMessage = "Ошибка загрузки"
                }
            } catch (e: Exception) {
                errorMessage = "Нет связи с сервером"
            } finally {
                isLoading = false
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    petId: Int,
    onNavigateBack: () -> Unit,
    viewModel: HistoryViewModel = viewModel()
) {
    LaunchedEffect(petId) {
        viewModel.loadHistory(petId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("История лечения") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Назад",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = VetGreen,
                    titleContentColor = Color.White
                )
            )
        },
        containerColor = Color(0xFFF5F9F9)
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            if (viewModel.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = VetGreen)
                }
            } else if (viewModel.errorMessage.isNotEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(viewModel.errorMessage, color = Color.Red)
                }
            } else if (viewModel.history.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("История лечения пуста", color = Color.Gray)
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                    items(viewModel.history) { item ->
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(item.date, fontWeight = FontWeight.Bold, color = VetGreen)
                                Text("Услуга: ${item.service_name}", fontSize = 14.sp)
                                Text("Врач: ${item.doctor_name}", fontSize = 14.sp)

                                if (item.diagnosis != null) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Диагноз: ${item.diagnosis}", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                    Text("Назначения: ${item.prescriptions}", fontSize = 14.sp)
                                    Text("Рекомендации: ${item.recommendations}", fontSize = 14.sp)
                                } else {
                                    Text("Диагноз не указан", color = Color.Gray, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}