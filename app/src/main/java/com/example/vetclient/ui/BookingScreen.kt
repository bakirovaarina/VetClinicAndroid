package com.example.vetclient.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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

class BookingViewModel : ViewModel() {
    var bookingData by mutableStateOf<BookingData?>(null)
    var isLoading by mutableStateOf(false)
    var message by mutableStateOf("")
    var isError by mutableStateOf(false)
    var isSuccess by mutableStateOf(false)

    var selectedServiceId by mutableStateOf<Int?>(null)
    var selectedDoctorId by mutableStateOf<Int?>(null)
    var selectedSlotId by mutableStateOf<Int?>(null)
    var selectedPetId by mutableStateOf<Int?>(null)
    var selectedDate by mutableStateOf<String?>(null)

    fun loadData(date: String? = null) {
        isLoading = true
        message = ""
        viewModelScope.launch {
            try {
                val response = ApiClient.apiService.getBookingData(date)
                if (response.isSuccessful) {
                    bookingData = response.body() ?: BookingData(emptyList(), emptyList(), emptyList())
                } else {
                    message = "Ошибка загрузки данных: ${response.code()}"
                    isError = true
                }
            } catch (e: Exception) {
                message = "Нет связи с сервером: ${e.message}"
                isError = true
            } finally {
                isLoading = false
            }
        }
    }

    fun book(userId: Int) {
        if (selectedServiceId == null || selectedSlotId == null || selectedPetId == null) {
            message = "Выберите питомца, услугу и время"
            isError = true
            return
        }
        isLoading = true
        viewModelScope.launch {
            try {
                val response = ApiClient.apiService.bookAppointment(
                    BookRequest(userId, selectedPetId!!, selectedSlotId!!, selectedServiceId!!)
                )
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.status == "success") {
                        message = body.message
                        isError = false
                        isSuccess = true
                    } else {
                        message = body?.message ?: "Ошибка"
                        isError = true
                    }
                } else {
                    message = "Ошибка сервера: ${response.code()}"
                    isError = true
                }
            } catch (e: Exception) {
                message = "Нет связи с сервером: ${e.message}"
                isError = true
            } finally {
                isLoading = false
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingScreen(
    userId: Int,
    pets: List<Pet>,
    onBookingSuccess: () -> Unit,
    viewModel: BookingViewModel = viewModel()
) {
    LaunchedEffect(Unit) {
        viewModel.loadData()
    }

    LaunchedEffect(viewModel.isSuccess) {
        if (viewModel.isSuccess) {
            kotlinx.coroutines.delay(1500)
            onBookingSuccess()
        }
    }

    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    if (viewModel.isLoading && viewModel.bookingData == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = VetGreen)
        }
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            item {
                Text("Запись на прием", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                Text("1. Выберите питомца", fontWeight = FontWeight.Bold)
                LazyColumn(modifier = Modifier.height(100.dp)) {
                    items(pets) { pet ->
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp).clickable { viewModel.selectedPetId = pet.id },
                            colors = CardDefaults.cardColors(
                                containerColor = if (viewModel.selectedPetId == pet.id) VetGreen.copy(alpha = 0.2f) else Color.White
                            )
                        ) { Text(pet.name, modifier = Modifier.padding(8.dp)) }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text("2. Выберите услугу", fontWeight = FontWeight.Bold)
                LazyColumn(modifier = Modifier.height(100.dp)) {
                    items(viewModel.bookingData?.services ?: emptyList()) { service ->
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp).clickable { viewModel.selectedServiceId = service.id },
                            colors = CardDefaults.cardColors(
                                containerColor = if (viewModel.selectedServiceId == service.id) VetGreen.copy(alpha = 0.2f) else Color.White
                            )
                        ) { Text("${service.name} (${service.price} руб.)", modifier = Modifier.padding(8.dp)) }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text("3. Выберите специалиста", fontWeight = FontWeight.Bold)
                LazyColumn(modifier = Modifier.height(100.dp)) {
                    items(viewModel.bookingData?.doctors ?: emptyList()) { doctor ->
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp).clickable { viewModel.selectedDoctorId = doctor.id },
                            colors = CardDefaults.cardColors(
                                containerColor = if (viewModel.selectedDoctorId == doctor.id) VetGreen.copy(alpha = 0.2f) else Color.White
                            )
                        ) { Text(doctor.full_name, modifier = Modifier.padding(8.dp)) }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text("4. Выберите дату", fontWeight = FontWeight.Bold)
                Button(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = VetGreen)
                ) { Text(viewModel.selectedDate ?: "Открыть календарь") }

                if (showDatePicker) {
                    DatePickerDialog(
                        onDismissRequest = { showDatePicker = false },
                        confirmButton = {
                            TextButton(onClick = {
                                val selectedMillis = datePickerState.selectedDateMillis
                                if (selectedMillis != null) {
                                    val selectedCal = Calendar.getInstance().apply { timeInMillis = selectedMillis }
                                    selectedCal.set(Calendar.HOUR_OF_DAY, 0)
                                    selectedCal.set(Calendar.MINUTE, 0)
                                    selectedCal.set(Calendar.SECOND, 0)
                                    selectedCal.set(Calendar.MILLISECOND, 0)

                                    val todayCal = Calendar.getInstance().apply {
                                        set(Calendar.HOUR_OF_DAY, 0)
                                        set(Calendar.MINUTE, 0)
                                        set(Calendar.SECOND, 0)
                                        set(Calendar.MILLISECOND, 0)
                                    }
                                    val maxCal = Calendar.getInstance().apply {
                                        add(Calendar.DAY_OF_YEAR, 30)
                                        set(Calendar.HOUR_OF_DAY, 0)
                                        set(Calendar.MINUTE, 0)
                                        set(Calendar.SECOND, 0)
                                        set(Calendar.MILLISECOND, 0)
                                    }

                                    if (selectedCal.timeInMillis < todayCal.timeInMillis) {
                                        viewModel.message = "Нельзя выбрать прошедшую дату"
                                        viewModel.isError = true
                                    } else if (selectedCal.timeInMillis > maxCal.timeInMillis) {
                                        viewModel.message = "Нельзя записаться более чем на 30 дней вперед"
                                        viewModel.isError = true
                                    } else {
                                        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                        viewModel.selectedDate = dateFormat.format(Date(selectedMillis))
                                        viewModel.loadData(viewModel.selectedDate)
                                    }
                                }
                                showDatePicker = false
                            }) { Text("Выбрать") }
                        },
                        dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Отмена") } }
                    ) {
                        DatePicker(
                            state = datePickerState
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text("5. Выберите время", fontWeight = FontWeight.Bold)
                LazyColumn(modifier = Modifier.height(120.dp)) {
                    items(viewModel.bookingData?.slots ?: emptyList()) { slot ->
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp).clickable { viewModel.selectedSlotId = slot.id },
                            colors = CardDefaults.cardColors(
                                containerColor = if (viewModel.selectedSlotId == slot.id) VetGreen.copy(alpha = 0.2f) else Color.White
                            )
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text(slot.start_at)
                                Text(slot.doctor_name, fontSize = 12.sp, color = Color.Gray)
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { viewModel.book(userId) },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = VetGreen)
                ) { Text("Записаться") }

                if (viewModel.message.isNotEmpty()) {
                    Text(
                        text = viewModel.message,
                        color = if (viewModel.isError) Color.Red else Color.Green,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }
    }
}