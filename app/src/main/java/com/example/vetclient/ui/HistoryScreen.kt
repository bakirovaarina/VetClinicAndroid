package com.example.vetclient.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.vetclient.network.ApiClient
import com.example.vetclient.network.CancelAppointmentRequest
import com.example.vetclient.network.HistoryItem
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class HistoryViewModel : ViewModel() {
    var history by mutableStateOf<List<HistoryItem>>(emptyList())
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf("")
    var selectedCategoryFilter by mutableStateOf("Все")
    var selectedDateFilter by mutableStateOf("Все время")
    var message by mutableStateOf("")

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

    fun cancelAppointment(appointmentId: Int, petId: Int) {
        viewModelScope.launch {
            try {
                val response = ApiClient.apiService.cancelAppointment(CancelAppointmentRequest(appointmentId))
                if (response.isSuccessful) {
                    val body = response.body()
                    message = body?.message ?: "Запись отменена"
                    loadHistory(petId)
                } else {
                    message = "Ошибка отмены"
                }
            } catch (e: Exception) {
                message = "Нет связи с сервером"
            }
        }
    }

    fun canCancel(item: HistoryItem): Boolean {
        if (item.status_name != "Ожидает") return false
        return try {
            val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val appointmentDate = format.parse(item.date) ?: return false
            val now = Date()
            val diff = appointmentDate.time - now.time
            val hoursLeft = diff / (1000 * 60 * 60)
            hoursLeft > 24
        } catch (e: Exception) {
            false
        }
    }

    fun getFilteredHistory(): List<HistoryItem> {
        var filtered = history
        filtered = when (selectedCategoryFilter) {
            "Приёмы" -> filtered.filter { it.category_id == 1 }
            "Груминг" -> filtered.filter { it.category_id == 2 }
            "Выгул" -> filtered.filter { it.category_id == 3 }
            else -> filtered
        }
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val now = Calendar.getInstance()
        filtered = when (selectedDateFilter) {
            "Сегодня" -> {
                val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(now.time)
                filtered.filter { it.date.startsWith(todayStr) }
            }
            "На этой неделе" -> {
                val startOfWeek = now.clone() as Calendar
                startOfWeek.set(Calendar.DAY_OF_WEEK, startOfWeek.firstDayOfWeek)
                startOfWeek.set(Calendar.HOUR_OF_DAY, 0)
                startOfWeek.set(Calendar.MINUTE, 0)
                startOfWeek.set(Calendar.SECOND, 0)
                filtered.filter { item ->
                    try {
                        val itemDate = dateFormat.parse(item.date)
                        itemDate != null && itemDate.after(startOfWeek.time)
                    } catch (e: Exception) { false }
                }
            }
            "В этом месяце" -> {
                val startOfMonth = now.clone() as Calendar
                startOfMonth.set(Calendar.DAY_OF_MONTH, 1)
                startOfMonth.set(Calendar.HOUR_OF_DAY, 0)
                startOfMonth.set(Calendar.MINUTE, 0)
                startOfMonth.set(Calendar.SECOND, 0)
                filtered.filter { item ->
                    try {
                        val itemDate = dateFormat.parse(item.date)
                        itemDate != null && itemDate.after(startOfMonth.time)
                    } catch (e: Exception) { false }
                }
            }
            else -> filtered
        }
        return filtered
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    petId: Int,
    onNavigateBack: () -> Unit,
    viewModel: HistoryViewModel = viewModel()
) {
    val historyNavController = rememberNavController()

    NavHost(navController = historyNavController, startDestination = "list") {
        composable("list") {
            LaunchedEffect(petId) {
                viewModel.loadHistory(petId)
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF5F9F9))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(VetGreen)
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад", tint = Color.White)
                    }
                    Text("История лечения", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Все", "Приёмы", "Груминг", "Выгул").forEach { filter ->
                        FilterChip(
                            selected = viewModel.selectedCategoryFilter == filter,
                            onClick = { viewModel.selectedCategoryFilter = filter },
                            label = { Text(filter, fontSize = 12.sp) }
                        )
                    }
                }

                var expanded by remember { mutableStateOf(false) }
                Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)) {
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }
                    ) {
                        OutlinedTextField(
                            value = viewModel.selectedDateFilter,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Период") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor(),
                            shape = MaterialTheme.shapes.medium
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            listOf("Все время", "Сегодня", "На этой неделе", "В этом месяце").forEach { period ->
                                DropdownMenuItem(
                                    text = { Text(period) },
                                    onClick = {
                                        viewModel.selectedDateFilter = period
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                if (viewModel.message.isNotEmpty()) {
                    Text(
                        viewModel.message,
                        color = if (viewModel.message.contains("успешно")) Color.Green else Color.Red,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }

                val filteredHistory = viewModel.getFilteredHistory()

                if (viewModel.isLoading) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = VetGreen)
                    }
                } else if (viewModel.errorMessage.isNotEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(viewModel.errorMessage, color = Color.Red)
                    }
                } else if (filteredHistory.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Записей не найдено", color = Color.Gray)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(filteredHistory) { item ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { historyNavController.navigate("details/${item.appointment_id}") },
                                colors = CardDefaults.cardColors(containerColor = Color.White)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(item.date, fontWeight = FontWeight.Bold, color = VetGreen)
                                        StatusBadge(statusName = item.status_name ?: "Ожидает")
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Услуга: ${item.service_name}", fontSize = 14.sp)
                                    Text("Врач: ${item.doctor_name}", fontSize = 14.sp)

                                    if (item.category_id == 3 && item.status_name == "Завершена" && item.walk_comment != null) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Card(
                                            colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                                            shape = MaterialTheme.shapes.small
                                        ) {
                                            Column(modifier = Modifier.padding(8.dp)) {
                                                Text("Отчет о прогулке:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF2E7D32))
                                                Text(item.walk_comment, fontSize = 12.sp, color = Color.DarkGray)
                                            }
                                        }
                                    }

                                    if (item.vaccine_name != null) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Card(
                                            colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                                            shape = MaterialTheme.shapes.small
                                        ) {
                                            Column(modifier = Modifier.padding(8.dp)) {
                                                Text("Вакцинация: ${item.vaccine_name}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF2E7D32))
                                                if (item.next_vaccination_date != null) {
                                                    Text("Повторная вакцинация: ${item.next_vaccination_date}", fontSize = 12.sp, color = Color.DarkGray)
                                                }
                                            }
                                        }
                                    }

                                    if (viewModel.canCancel(item)) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Button(
                                            onClick = { viewModel.cancelAppointment(item.appointment_id, petId) },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text("Отменить запись")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        composable("details/{appointmentId}") { backStackEntry ->
            val appointmentId = backStackEntry.arguments?.getString("appointmentId")?.toIntOrNull() ?: 0
            val item = viewModel.history.find { it.appointment_id == appointmentId }
            if (item != null) {
                HistoryDetailsScreen(item = item, onNavigateBack = { historyNavController.popBackStack() })
            } else {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Запись не найдена")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryDetailsScreen(item: HistoryItem, onNavigateBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F9F9))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(VetGreen)
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад", tint = Color.White)
            }
            Text("Подробности", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }

        Column(modifier = Modifier.padding(16.dp)) {
            Card(colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Дата: ${item.date}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("Услуга: ${item.service_name}", fontSize = 14.sp)
                    Text("Врач: ${item.doctor_name}", fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(12.dp))

                    if (item.category_id == 3 && item.status_name == "Завершена" && item.walk_comment != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("Отчет о прогулке:", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF2E7D32))
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(item.walk_comment, fontSize = 13.sp, color = Color.DarkGray)
                            }
                        }
                    }

                    if (item.category_id == 1 && item.status_name == "Завершена" && item.diagnosis != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Диагноз: ${item.diagnosis}", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        Text("Назначения: ${item.prescriptions}", fontSize = 14.sp)
                        Text("Рекомендации: ${item.recommendations}", fontSize = 14.sp)
                    } else if (item.category_id == 1 && item.status_name == "Завершена") {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Диагноз не указан", color = Color.Gray, fontSize = 12.sp)
                    }

                    if (item.vaccine_name != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("Вакцинация: ${item.vaccine_name}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF2E7D32))
                                if (item.next_vaccination_date != null) {
                                    Text("Повторная вакцинация: ${item.next_vaccination_date}", fontSize = 13.sp, color = Color.DarkGray)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatusBadge(statusName: String) {
    val color = when (statusName) {
        "Ожидает" -> Color(0xFFFFA000)
        "Подтверждена" -> Color(0xFF1976D2)
        "Завершена" -> Color(0xFF388E3C)
        "Отменена" -> Color(0xFFD32F2F)
        else -> Color.Gray
    }
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = statusName,
            color = color,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}