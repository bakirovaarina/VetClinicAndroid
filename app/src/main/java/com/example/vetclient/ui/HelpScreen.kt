package com.example.vetclient.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import com.example.vetclient.network.*
import kotlinx.coroutines.launch

class HelpViewModel : ViewModel() {
    var fundraisings by mutableStateOf<List<Fundraising>>(emptyList())
    var isLoading by mutableStateOf(false)
    var message by mutableStateOf("")

    fun loadDonations() {
        isLoading = true
        viewModelScope.launch {
            try {
                val response = ApiClient.apiService.getDonations()
                if (response.isSuccessful) {
                    fundraisings = response.body() ?: emptyList()
                }
            } catch (e: Exception) {
                message = "Ошибка загрузки"
            } finally {
                isLoading = false
            }
        }
    }

    fun donate(userId: Int, fundraisingId: Int, amount: Double) {
        viewModelScope.launch {
            try {
                val response = ApiClient.apiService.makeDonation(DonationRequest(userId, fundraisingId, amount))
                if (response.isSuccessful) {
                    val body = response.body()
                    message = body?.message ?: "Успешно"
                    loadDonations()
                } else {
                    message = "Ошибка"
                }
            } catch (e: Exception) {
                message = "Нет связи с сервером"
            }
        }
    }
}

@Composable
fun HelpScreen(userId: Int, onNavigateBack: () -> Unit, viewModel: HelpViewModel = viewModel()) {
    val helpNavController = rememberNavController()

    NavHost(navController = helpNavController, startDestination = "list") {
        composable("list") {
            LaunchedEffect(Unit) {
                viewModel.loadDonations()
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
                    Text("Помощь животным", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (viewModel.isLoading) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = VetGreen)
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(viewModel.fundraisings) { fund ->
                            val goal = fund.goal_amount.toDoubleOrNull() ?: 0.0
                            val collected = fund.collected_amount.toDoubleOrNull() ?: 0.0
                            val progress = if (goal > 0) (collected / goal).toFloat().coerceIn(0f, 1f) else 0f
                            val isCompleted = progress >= 1f

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    if (fund.image_url != null) {
                                        Image(
                                            painter = rememberAsyncImagePainter(fund.image_url),
                                            contentDescription = null,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(150.dp)
                                                .clip(RoundedCornerShape(12.dp)),
                                            contentScale = ContentScale.Crop
                                        )
                                        Spacer(modifier = Modifier.height(12.dp))
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(fund.title, fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.weight(1f))
                                        if (isCompleted) {
                                            Surface(
                                                color = Color(0xFFE8F5E9),
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Text("Завершен", color = Color(0xFF2E7D32), fontSize = 12.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                                            }
                                        } else {
                                            Surface(
                                                color = Color(0xFFFFF3E0),
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Text("Активен", color = Color(0xFFE65100), fontSize = 12.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    LinearProgressIndicator(
                                        progress = progress,
                                        modifier = Modifier.fillMaxWidth().height(8.dp),
                                        color = if (isCompleted) Color(0xFF2E7D32) else VetGreen,
                                        trackColor = VetGreen.copy(alpha = 0.2f)
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Собрано: ${fund.collected_amount} руб.", fontSize = 14.sp, color = Color.DarkGray)
                                        Text("Цель: ${fund.goal_amount} руб.", fontSize = 14.sp, color = Color.Gray)
                                    }

                                    if (isCompleted && fund.report_text != null) {
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Card(
                                            colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Column(modifier = Modifier.padding(12.dp)) {
                                                Text("Отчет о расходовании средств:", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF2E7D32))
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(fund.report_text, fontSize = 13.sp, color = Color.DarkGray)
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    if (!isCompleted) {
                                        Button(
                                            onClick = { helpNavController.navigate("payment/${fund.id}/${fund.title}") },
                                            modifier = Modifier.fillMaxWidth().height(40.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = VetGreen)
                                        ) {
                                            Icon(Icons.Default.Favorite, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("Пожертвовать")
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (viewModel.message.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = viewModel.message,
                                color = Color(0xFF2E7D32),
                                modifier = Modifier.padding(12.dp),
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }

        composable("payment/{fundId}/{fundTitle}") { backStackEntry ->
            val fundId = backStackEntry.arguments?.getString("fundId")?.toIntOrNull() ?: 0
            val fundTitle = backStackEntry.arguments?.getString("fundTitle") ?: ""
            PaymentScreen(
                fundTitle = fundTitle,
                onPaymentSuccess = { amount ->
                    viewModel.donate(userId, fundId, amount)
                    helpNavController.popBackStack()
                },
                onNavigateBack = { helpNavController.popBackStack() }
            )
        }
    }
}