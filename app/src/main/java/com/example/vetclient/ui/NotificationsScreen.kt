package com.example.vetclient.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
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
import com.example.vetclient.network.*
import kotlinx.coroutines.launch

class NotificationsViewModel : ViewModel() {
    var notifications by mutableStateOf<List<UserNotification>>(emptyList())
    var isLoading by mutableStateOf(false)
    var message by mutableStateOf("")

    fun loadNotifications(userId: Int) {
        isLoading = true
        viewModelScope.launch {
            try {
                // 1. Загружаем уведомления из БД
                val response = ApiClient.apiService.getNotifications(userId)
                if (response.isSuccessful) {
                    notifications = response.body() ?: emptyList()
                }

                // 2. Добавляем напоминания о вакцинации (если есть)
                val homeResponse = ApiClient.apiService.getHomeData(userId)
                if (homeResponse.isSuccessful) {
                    val reminder = homeResponse.body()?.next_reminder
                    if (reminder != null) {
                        val note = UserNotification(
                            id = -1, // временный ID
                            title = "Напоминание о вакцинации",
                            text = "Питомцу ${reminder.pet_name} нужно сделать повторную вакцинацию (${reminder.vaccine_name}) до ${reminder.date}.",
                            type = "vaccination",
                            date = reminder.date
                        )
                        notifications = notifications + note
                    }
                }
            } catch (e: Exception) {
                message = "Нет связи с сервером"
            } finally {
                isLoading = false
            }
        }
    }
}

@Composable
fun NotificationsScreen(
    userId: Int,
    onNavigateBack: () -> Unit,
    viewModel: NotificationsViewModel = viewModel()
) {
    LaunchedEffect(userId) {
        viewModel.loadNotifications(userId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F9F9))
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Назад",
                    tint = VetGreen
                )
            }
            Text("Уведомления", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (viewModel.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = VetGreen)
            }
        } else if (viewModel.message.isNotEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(viewModel.message, color = Color.Red)
            }
        } else if (viewModel.notifications.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("У вас пока нет уведомлений", color = Color.Gray)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(viewModel.notifications) { note ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.Top) {
                            Icon(
                                imageVector = when (note.type) {
                                    "appointment" -> Icons.Default.DateRange
                                    "vaccination" -> Icons.Default.Favorite
                                    "order" -> Icons.Default.ShoppingCart
                                    "donation" -> Icons.Default.FavoriteBorder
                                    else -> Icons.Default.Notifications
                                },
                                contentDescription = null,
                                tint = VetGreen,
                                modifier = Modifier.size(28.dp).padding(top = 2.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(note.title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(note.text, fontSize = 13.sp, color = Color.DarkGray)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(note.date, fontSize = 11.sp, color = Color.Gray)
                            }
                        }
                    }
                }
            }
        }
    }
}