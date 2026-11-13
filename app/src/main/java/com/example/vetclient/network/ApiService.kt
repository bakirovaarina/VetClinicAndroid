package com.example.vetclient.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.GET
import retrofit2.http.Query

// Модели для регистрации
data class RegisterRequest(
    val full_name: String,
    val email: String,
    val password: String,
    val phone: String
)

data class AuthResponse(
    val status: String,
    val message: String
)

// Модели для входа
data class LoginRequest(
    val email: String,
    val password: String
)

data class LoginResponse(
    val status: String,
    val message: String,
    val user_id: Int?,
    val full_name: String?
)

// Модели для восстановления пароля
data class RecoverRequest(
    val email: String
)

data class RecoverResponse(
    val status: String,
    val message: String
)

// Модели для питомцев
data class Pet(
    val id: Int,
    val name: String,
    val breed: String?,
    val birth_date: String?,
    val weight_kg: String?,
    val species: String
)

data class AddPetRequest(
    val id: Int? = null,
    val user_id: Int,
    val name: String,
    val breed: String?,
    val species: String,
    val weight_kg: String?
)

data class DeletePetRequest(val id: Int)

// Модели для записи
data class Service(val id: Int, val name: String, val price: String)
data class Doctor(val id: Int, val full_name: String)
data class Slot(val id: Int, val start_at: String, val end_at: String, val doctor_name: String)

data class BookingData(
    val services: List<Service>,
    val doctors: List<Doctor>,
    val slots: List<Slot>
)

data class BookRequest(
    val user_id: Int,
    val pet_id: Int,
    val slot_id: Int,
    val service_id: Int
)

// Модель для истории
data class HistoryItem(
    val appointment_id: Int,
    val date: String,
    val service_name: String,
    val doctor_name: String,
    val diagnosis: String?,
    val prescriptions: String?,
    val recommendations: String?
)

interface ApiService {
    @POST("register.php")
    suspend fun registerUser(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("login.php")
    suspend fun loginUser(@Body request: LoginRequest): Response<LoginResponse>

    @POST("recover_password.php")
    suspend fun recoverPassword(@Body request: RecoverRequest): Response<RecoverResponse>

    @GET("get_pets.php")
    suspend fun getPets(@Query("user_id") userId: Int): Response<List<Pet>>

    @POST("add_pet.php")
    suspend fun addPet(@Body request: AddPetRequest): Response<AuthResponse>

    @POST("delete_pet.php")
    suspend fun deletePet(@Body request: DeletePetRequest): Response<AuthResponse>

    @POST("update_pet.php")
    suspend fun updatePet(@Body request: AddPetRequest): Response<AuthResponse>

    @GET("get_booking_data.php")
    suspend fun getBookingData(@Query("date") date: String? = null): Response<BookingData>

    @POST("book_appointment.php")
    suspend fun bookAppointment(@Body request: BookRequest): Response<AuthResponse>

    @GET("get_history.php")
    suspend fun getHistory(@Query("pet_id") petId: Int): Response<List<HistoryItem>>


}