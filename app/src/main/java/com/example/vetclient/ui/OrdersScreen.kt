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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdersScreen(userId: Int, onNavigateBack: () -> Unit, viewModel: OrdersViewModel = viewModel()) {
    LaunchedEffect(userId) {
        viewModel.loadOrders(userId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Мои заказы") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = VetGreen, titleContentColor = Color.White)
            )
        },
        containerColor = Color(0xFFF5F9F9)
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).padding(16.dp)) {
            if (viewModel.message.isNotEmpty()) {
                Text(viewModel.message, color = VetGreen, modifier = Modifier.padding(bottom = 8.dp))
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
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Заказ #${order.id}", fontWeight = FontWeight.Bold)
                                    Text(
                                        order.status_name ?: "Новый",
                                        color = when (order.status_name) {
                                            "Новый" -> Color(0xFFFFA000)
                                            "Выполнен" -> Color(0xFF388E3C)
                                            "Отменен" -> Color(0xFFD32F2F)
                                            else -> Color.Gray
                                        },
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text("Сумма: ${order.total_amount} руб.", fontSize = 14.sp)
                                Text("Забрать ${viewModel.getPickupDeadline(order.created_at)}", fontSize = 14.sp, color = VetGreen)
                                Text("Самовывоз: г. Москва, ул. Ленина, д. 10", fontSize = 12.sp, color = Color.Gray)

                                if (viewModel.canCancel(order)) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Button(
                                        onClick = { viewModel.cancelOrder(order.id, userId) },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
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
}