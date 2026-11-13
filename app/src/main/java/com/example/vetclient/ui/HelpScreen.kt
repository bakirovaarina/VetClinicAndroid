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
                } else {
                    message = "Ошибка"
                }
            } catch (e: Exception) {
                message = "Нет связи с сервером"
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpScreen(userId: Int, onNavigateBack: () -> Unit, viewModel: HelpViewModel = viewModel()) {
    LaunchedEffect(Unit) {
        viewModel.loadDonations()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Помощь животным") },
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
            if (viewModel.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(viewModel.fundraisings) { fund ->
                        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(fund.title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text("Цель: ${fund.goal_amount} руб.", color = Color.Gray, fontSize = 14.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = { viewModel.donate(userId, fund.id, 500.0) },
                                    colors = ButtonDefaults.buttonColors(containerColor = VetGreen)
                                ) {
                                    Text("Пожертвовать 500 ₽")
                                }
                            }
                        }
                    }
                }

                if (viewModel.message.isNotEmpty()) {
                    Text(viewModel.message, color = VetGreen, modifier = Modifier.padding(top = 16.dp))
                }
            }
        }
    }
}