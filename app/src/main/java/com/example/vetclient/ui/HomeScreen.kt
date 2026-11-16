package com.example.vetclient.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.vetclient.R
import com.example.vetclient.network.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class HomeViewModel : ViewModel() {
    var homeData by mutableStateOf<HomeData?>(null)
    var isLoading by mutableStateOf(false)

    fun loadData(userId: Int) {
        isLoading = true
        viewModelScope.launch {
            try {
                val response = ApiClient.apiService.getHomeData(userId)
                if (response.isSuccessful) {
                    homeData = response.body()
                }
            } catch (e: Exception) {
            } finally {
                isLoading = false
            }
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
fun HomeScreen(
    userName: String,
    userId: Int,
    onNavigateToBooking: (String?) -> Unit,
    onNavigateToPets: () -> Unit,
    onNavigateToShop: () -> Unit,
    onNavigateToHelp: () -> Unit
) {
    val viewModel: HomeViewModel = viewModel()

    LaunchedEffect(userId) {
        viewModel.loadData(userId)
    }

    if (viewModel.isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = VetGreen)
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(VetLightBackground)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Добрый день,", fontSize = 14.sp, color = Color.Gray)
                        Text(userName, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                    }
                    Icon(Icons.Default.Notifications, contentDescription = null, tint = VetGreen)
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = VetGreen)
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = viewModel.homeData?.promo?.title ?: "Загрузка...",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = viewModel.homeData?.promo?.subtitle ?: "",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 12.sp
                            )
                        }
                        Image(
                            painter = painterResource(id = R.drawable.promo_image),
                            contentDescription = null,
                            modifier = Modifier
                                .size(80.dp)
                                .padding(4.dp),
                            contentScale = ContentScale.Fit
                        )
                    }
                }
            }

            item {
                Text("Быстрые действия", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    ActionIcon("Запись на приём", Icons.Default.DateRange) { onNavigateToBooking("Приём") }
                    ActionIcon("Мои питомцы", Icons.Default.Pets) { onNavigateToPets() }
                    ActionIcon("Магазин", Icons.Default.ShoppingCart) { onNavigateToShop() }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    ActionIcon("Груминг", Icons.Default.ContentCut) { onNavigateToBooking("Груминг") }
                    ActionIcon("Прогулка", Icons.Default.DirectionsWalk) { onNavigateToBooking("Выгул") }
                    ActionIcon("Пожертвования", Icons.Default.Favorite) { onNavigateToHelp() }
                }
            }

            item {
                Text("Предстоящие события", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                Spacer(modifier = Modifier.height(8.dp))
                val appointments = viewModel.homeData?.next_appointments ?: emptyList()
                if (appointments.isNotEmpty()) {
                    appointments.forEach { next ->
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.DateRange, contentDescription = null, tint = VetGreen)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(next.service_name, fontWeight = FontWeight.Bold)
                                    Text("Питомец: ${next.pet_name}", fontSize = 12.sp, color = Color.DarkGray)
                                    Text("${next.start_at} • ${next.doctor_name}", fontSize = 12.sp, color = Color.Gray)
                                }
                            }
                        }
                    }
                } else {
                    Text("Нет предстоящих событий", color = Color.Gray, fontSize = 14.sp)
                }
            }

            item {
                val readyOrders = viewModel.homeData?.ready_orders
                if (readyOrders != null && readyOrders.isNotEmpty()) {
                    Text("Заказы готовы к получению", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                    Spacer(modifier = Modifier.height(8.dp))
                    readyOrders.forEach { order ->
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))
                        ) {
                            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF2E7D32))
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("Заказ #${order.id}", fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                                    Text("Забрать ${viewModel.getPickupDeadline(order.created_at)}", fontSize = 12.sp, color = Color.Gray)
                                    Text("Самовывоз: г. Москва, ул. Ленина, д. 10", fontSize = 12.sp, color = Color.Gray)
                                }
                            }
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { onNavigateToHelp() },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF0F5))
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = null,
                            tint = Color(0xFFE57373),
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Помогаем бездомным животным",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color(0xFF5D4037)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Button(
                                onClick = onNavigateToHelp,
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00897B)),
                                shape = RoundedCornerShape(20.dp),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Text("Сделать взнос", color = Color(0xFF00897B), fontSize = 12.sp)
                            }
                        }
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFFFFC107),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ActionIcon(title: String, icon: ImageVector, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(100.dp).clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(VetGreen.copy(alpha = 0.1f), shape = RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = VetGreen)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(title, fontSize = 12.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
    }
}