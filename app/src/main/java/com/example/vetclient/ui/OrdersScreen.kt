package com.example.vetclient.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.vetclient.network.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class OrdersViewModel : ViewModel() {
    var orders by mutableStateOf<List<Order>>(emptyList())
    var isLoading by mutableStateOf(false)
    var message by mutableStateOf("")

    fun loadOrders(userId: Int) {
        isLoading = true
        viewModelScope.launch {
            try {
                val response = ApiClient.apiService.getOrders(userId)
                if (response.isSuccessful) {
                    orders = response.body() ?: emptyList()
                }
            } catch (e: Exception) {
            } finally {
                isLoading = false
            }
        }
    }

    fun cancelOrder(orderId: Int, userId: Int) {
        viewModelScope.launch {
            try {
                val response = ApiClient.apiService.cancelOrder(CancelOrderRequest(orderId))
                if (response.isSuccessful) {
                    val body = response.body()
                    message = body?.message ?: "Заказ отменен"
                    loadOrders(userId)
                }
            } catch (e: Exception) {
                message = "Нет связи с сервером"
            }
        }
    }

    fun canCancel(order: Order): Boolean {
        if (order.status_id != 1) return false
        return try {
            val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val created = format.parse(order.created_at) ?: return false
            val now = Date()
            val diff = now.time - created.time
            diff < 86400000
        } catch (e: Exception) {
            false
        }
    }

    fun getPickupDeadline(createdAt: String): String {
        return try {
            val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val created = format.parse(createdAt) ?: return "---"
            val calendar = Calendar.getInstance()
            calendar.time = created
            calendar.add(Calendar.DAY_OF_YEAR, 3)
            val outputFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
            "до ${outputFormat.format(calendar.time)}"
        } catch (e: Exception) {
            "---"
        }
    }
}

@Composable
fun OrdersScreen(userId: Int, onNavigateBack: () -> Unit, viewModel: OrdersViewModel = viewModel()) {
    LaunchedEffect(userId) {
        viewModel.loadOrders(userId)
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
            Text("Мои заказы", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (viewModel.message.isNotEmpty()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Text(
                    text = viewModel.message,
                    color = Color(0xFF2E7D32),
                    modifier = Modifier.padding(12.dp),
                    fontSize = 14.sp
                )
            }
        }

        if (viewModel.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = VetGreen)
            }
        } else if (viewModel.orders.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("У вас нет заказов", color = Color.Gray)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(viewModel.orders) { order ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Заказ #${order.id}", fontWeight = FontWeight.Bold)

                                val statusName = order.status_name ?: "Новый"
                                val statusColor = when (statusName) {
                                    "Новый" -> Color(0xFFE3F2FD)
                                    "В обработке" -> Color(0xFFFFF3E0)
                                    "Готов к получению" -> Color(0xFFE8F5E9)
                                    "Получен" -> Color(0xFFF5F5F5)
                                    "Отменен" -> Color(0xFFFFEBEE)
                                    else -> Color(0xFFF5F5F5)
                                }
                                val statusTextColor = when (statusName) {
                                    "Новый" -> Color(0xFF1565C0)
                                    "В обработке" -> Color(0xFFE65100)
                                    "Готов к получению" -> Color(0xFF2E7D32)
                                    "Получен" -> Color(0xFF616161)
                                    "Отменен" -> Color(0xFFC62828)
                                    else -> Color(0xFF616161)
                                }
                                Surface(
                                    color = statusColor,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = statusName,
                                        color = statusTextColor,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                            Text("Сумма: ${order.total_amount} руб.", fontSize = 14.sp)
                            Text("Забрать ${viewModel.getPickupDeadline(order.created_at)}", fontSize = 14.sp, color = VetGreen)
                            Text("Самовывоз: г. Москва, ул. Ленина, д. 10", fontSize = 12.sp, color = Color.Gray)

                            if (viewModel.canCancel(order)) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = { viewModel.cancelOrder(order.id, userId) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFFFFEBEE),
                                        contentColor = Color(0xFFC62828)
                                    ),
                                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Отменить заказ")
                                }
                            } else if (order.status_id == 1) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Отмена недоступна (прошло более 24 часов)", fontSize = 12.sp, color = Color.Gray)
                            }
                        }
                    }
                }
            }
        }
    }
}