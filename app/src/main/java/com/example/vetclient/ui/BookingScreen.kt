package com.example.vetclient.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
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

    var currentStep by mutableStateOf(1)
    var selectedCategory by mutableStateOf<String?>(null)
    var selectedServiceId by mutableStateOf<Int?>(null)
    var selectedDoctorId by mutableStateOf<Int?>(null)
    var selectedSlotId by mutableStateOf<Int?>(null)
    var selectedPetId by mutableStateOf<Int?>(null)
    var selectedDate by mutableStateOf<String?>(null)
    var selectedTime by mutableStateOf<String?>(null)

    fun loadData(date: String? = null, category: String? = null) {
        isLoading = true
        message = ""
        viewModelScope.launch {
            try {
                val response = ApiClient.apiService.getBookingData(date, category)
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
                        currentStep = 7
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
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Запись на прием") },
                navigationIcon = {
                    if (viewModel.currentStep > 1 && viewModel.currentStep < 7) {
                        IconButton(onClick = {
                            viewModel.currentStep--
                            viewModel.message = ""
                        }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад", tint = Color.White)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = VetGreen, titleContentColor = Color.White)
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            LinearProgressIndicator(
                progress = viewModel.currentStep / 7f,
                modifier = Modifier.fillMaxWidth().height(8.dp),
                color = VetGreen,
                trackColor = VetGreen.copy(alpha = 0.2f)
            )

            LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                when (viewModel.currentStep) {
                    1 -> item {
                        Text("Шаг 1: Выберите питомца", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(16.dp))
                        pets.forEach { pet ->
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable {
                                    viewModel.selectedPetId = pet.id
                                    viewModel.currentStep = 2
                                    viewModel.message = ""
                                },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (viewModel.selectedPetId == pet.id) VetGreen.copy(alpha = 0.2f) else Color.White
                                )
                            ) {
                                Text(pet.name, modifier = Modifier.padding(16.dp), fontSize = 18.sp)
                            }
                        }
                    }
                    2 -> item {
                        Text("Шаг 2: Выберите категорию", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(16.dp))
                        listOf("Приём", "Груминг", "Выгул").forEach { category ->
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable {
                                    viewModel.selectedCategory = category
                                    viewModel.loadData(category = category)
                                    viewModel.currentStep = 3
                                    viewModel.message = ""
                                },
                                colors = CardDefaults.cardColors(containerColor = Color.White)
                            ) {
                                Text(category, modifier = Modifier.padding(16.dp), fontSize = 18.sp)
                            }
                        }
                    }
                    3 -> item {
                        Text("Шаг 3: Выберите услугу", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(16.dp))
                        if (viewModel.bookingData?.services.isNullOrEmpty()) {
                            Text("Услуги не найдены", color = Color.Red)
                        } else {
                            viewModel.bookingData?.services?.forEach { service ->
                                Card(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable {
                                        viewModel.selectedServiceId = service.id
                                        viewModel.selectedDoctorId = service.employee_id
                                        viewModel.currentStep = 4
                                        viewModel.message = ""
                                    },
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (viewModel.selectedServiceId == service.id) VetGreen.copy(alpha = 0.2f) else Color.White
                                    )
                                ) {
                                    Text("${service.name} (${service.price} руб.)", modifier = Modifier.padding(16.dp))
                                }
                            }
                        }
                    }
                    4 -> item {
                        Text("Шаг 4: Специалист", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(16.dp))
                        val doctor = viewModel.bookingData?.doctors?.find { it.id == viewModel.selectedDoctorId }
                        if (doctor != null) {
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(containerColor = VetGreen.copy(alpha = 0.2f))
                            ) {
                                Text(doctor.full_name, modifier = Modifier.padding(16.dp))
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = {
                                    viewModel.currentStep = 5
                                    viewModel.message = ""
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = VetGreen)
                            ) { Text("Далее") }
                        } else {
                            Text("Специалист не найден", color = Color.Red)
                        }
                    }
                    5 -> item {
                        Text("Шаг 5: Выберите дату", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(16.dp))
                        var showDatePicker by remember { mutableStateOf(false) }
                        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = System.currentTimeMillis())

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
                                                add(Calendar.DAY_OF_YEAR, 60)
                                                set(Calendar.HOUR_OF_DAY, 0)
                                                set(Calendar.MINUTE, 0)
                                                set(Calendar.SECOND, 0)
                                                set(Calendar.MILLISECOND, 0)
                                            }

                                            if (selectedCal.timeInMillis < todayCal.timeInMillis) {
                                                viewModel.message = "Нельзя выбрать прошедшую дату"
                                                viewModel.isError = true
                                                showDatePicker = false
                                            } else if (selectedCal.timeInMillis > maxCal.timeInMillis) {
                                                viewModel.message = "Нельзя записаться более чем на 60 дней вперед"
                                                viewModel.isError = true
                                                showDatePicker = false
                                            } else {
                                                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                                viewModel.selectedDate = dateFormat.format(Date(selectedMillis))
                                                viewModel.loadData(viewModel.selectedDate, viewModel.selectedCategory)
                                                viewModel.currentStep = 6
                                                showDatePicker = false
                                            }
                                        }
                                    }) { Text("Выбрать") }
                                },
                                dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Отмена") } }
                            ) {
                                DatePicker(
                                    state = datePickerState,
                                    showModeToggle = false
                                )
                            }
                        }

                        if (viewModel.message.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = viewModel.message,
                                color = Color.Red
                            )
                        }
                    }
                    6 -> item {
                        Text("Шаг 6: Выберите время", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(16.dp))
                        val doctorSlots = viewModel.bookingData?.slots?.filter { it.employee_id == viewModel.selectedDoctorId }
                        if (doctorSlots.isNullOrEmpty()) {
                            Text("Нет свободных слотов на эту дату", color = Color.Red)
                        } else {
                            doctorSlots.forEach { slot ->
                                val time = slot.start_at.split(" ").getOrNull(1)?.substring(0, 5) ?: slot.start_at
                                Card(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable {
                                        viewModel.selectedSlotId = slot.id
                                        viewModel.selectedTime = time
                                        viewModel.currentStep = 7
                                        viewModel.message = ""
                                    },
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (viewModel.selectedSlotId == slot.id) VetGreen.copy(alpha = 0.2f) else Color.White
                                    )
                                ) {
                                    Text(time, modifier = Modifier.padding(16.dp), fontSize = 18.sp)
                                }
                            }
                        }
                    }
                    7 -> item {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = VetGreen, modifier = Modifier.size(100.dp))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Запись одобрена!", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = VetGreen)
                            Spacer(modifier = Modifier.height(16.dp))
                            Card(colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("Питомец: ${pets.find { it.id == viewModel.selectedPetId }?.name ?: "---"}")
                                    Text("Категория: ${viewModel.selectedCategory ?: "---"}")
                                    Text("Услуга: ${viewModel.bookingData?.services?.find { it.id == viewModel.selectedServiceId }?.name ?: "---"}")
                                    Text("Специалист: ${viewModel.bookingData?.doctors?.find { it.id == viewModel.selectedDoctorId }?.full_name ?: "---"}")
                                    Text("Дата: ${viewModel.selectedDate ?: "---"}")
                                    Text("Время: ${viewModel.selectedTime ?: "---"}")
                                }
                            }
                            Spacer(modifier = Modifier.height(24.dp))
                            Button(
                                onClick = { viewModel.book(userId) },
                                modifier = Modifier.fillMaxWidth().height(50.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = VetGreen)
                            ) { Text("Подтвердить") }

                            if (viewModel.isSuccess) {
                                Spacer(modifier = Modifier.height(16.dp))
                                Text("Вы успешно записаны!", color = VetGreen, fontWeight = FontWeight.Bold)
                                Button(onClick = onBookingSuccess, modifier = Modifier.fillMaxWidth()) { Text("На главную") }
                            }

                            if (viewModel.message.isNotEmpty() && !viewModel.isSuccess) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(viewModel.message, color = Color.Red)
                            }
                        }
                    }
                }
            }
        }
    }
}