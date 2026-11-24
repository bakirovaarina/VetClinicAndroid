package com.example.vetclient.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.GET
import retrofit2.http.Query
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Multipart
import retrofit2.http.Part

// Модели для главного экрана
data class HomeData(
    val next_appointments: List<NextAppointment>,
    val ready_orders: List<ReadyOrder>,
    val promo: Promo?
)

data class NextAppointment(
    val id: Int,
    val start_at: String,
    val service_name: String,
    val doctor_name: String,
    val pet_name: String
)

data class ReadyOrder(
    val id: Int,
    val created_at: String
)

data class Promo(
    val title: String,
    val subtitle: String,
    val image: String?
)

// Модели для регистрации
data class RegisterRequest(
    val full_name: String,
    val email: String,
    val password: String,
    val phone: String
)

data class AuthResponse(
    val status: String,
    val message: String,
    val pet_id: Int? = null
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
    val species: String,
    val age: Int?,
    val image_url: String?
)

data class AddPetRequest(
    val id: Int? = null,
    val user_id: Int,
    val name: String,
    val breed: String?,
    val species: String,
    val weight_kg: String?,
    val age: String?
)

data class DeletePetRequest(val id: Int)

// Модели для записи
data class Service(
    val id: Int,
    val name: String,
    val price: String,
    val employee_id: Int?
)

data class Doctor(
    val id: Int,
    val full_name: String,
    val image_url: String?
)

data class Slot(
    val id: Int,
    val start_at: String,
    val end_at: String,
    val doctor_name: String,
    val employee_id: Int
)

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
    val recommendations: String?,
    val category_id: Int,
    val status_name: String?
)

data class CancelAppointmentRequest(
    val appointment_id: Int
)

// Модель для товара
data class Product(
    val id: Int,
    val name: String,
    val price: String,
    val stock_qty: Int,
    val category: String,
    val image_url: String?
)

// Модели для заказа
data class Order(
    val id: Int,
    val created_at: String,
    val status_id: Int,
    val status_name: String?,
    val total_amount: String?
)

data class CancelOrderRequest(
    val order_id: Int
)

data class OrderItem(
    val product_id: Int,
    val quantity: Int
)

data class CreateOrderRequest(
    val user_id: Int,
    val items: List<OrderItem>
)

data class OrderResponse(
    val status: String,
    val message: String,
    val order_id: Int?,
    val total_amount: Double?
)

data class UpdateOrderStatusRequest(
    val order_id: Int,
    val status_id: Int
)

// Модели для профиля
data class UserProfile(
    val full_name: String,
    val email: String,
    val phone: String
)

data class UpdateProfileRequest(
    val user_id: Int,
    val full_name: String,
    val phone: String
)

data class ChangePasswordRequest(
    val user_id: Int,
    val old_password: String,
    val new_password: String
)

// Модели для пожертвований
data class Fundraising(
    val id: Int,
    val title: String,
    val goal_amount: String,
    val collected_amount: String,
    val is_active: Int
)

data class DonationRequest(
    val user_id: Int,
    val fundraising_id: Int,
    val amount: Double
)

data class UserNotification(
    val id: Int,
    val title: String,
    val text: String,
    val type: String,
    val date: String
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
    suspend fun getBookingData(
        @Query("date") date: String? = null,
        @Query("category") category: String? = null
    ): Response<BookingData>

    @POST("book_appointment.php")
    suspend fun bookAppointment(@Body request: BookRequest): Response<AuthResponse>

    @GET("get_history.php")
    suspend fun getHistory(@Query("pet_id") petId: Int): Response<List<HistoryItem>>

    @GET("get_products.php")
    suspend fun getProducts(
        @Query("category") category: String? = null,
        @Query("search") search: String? = null
    ): Response<List<Product>>

    @POST("create_order.php")
    suspend fun createOrder(@Body request: CreateOrderRequest): Response<OrderResponse>

    @GET("get_profile.php")
    suspend fun getProfile(@Query("user_id") userId: Int): Response<UserProfile>

    @GET("get_donations.php")
    suspend fun getDonations(): Response<List<Fundraising>>

    @POST("make_donation.php")
    suspend fun makeDonation(@Body request: DonationRequest): Response<AuthResponse>

    @POST("cancel_appointment.php")
    suspend fun cancelAppointment(@Body request: CancelAppointmentRequest): Response<AuthResponse>

    @POST("update_profile.php")
    suspend fun updateProfile(@Body request: UpdateProfileRequest): Response<AuthResponse>

    @POST("change_password.php")
    suspend fun changePassword(@Body request: ChangePasswordRequest): Response<AuthResponse>

    @GET("get_orders.php")
    suspend fun getOrders(@Query("user_id") userId: Int): Response<List<Order>>

    @POST("cancel_order.php")
    suspend fun cancelOrder(@Body request: CancelOrderRequest): Response<AuthResponse>

    @GET("get_home_data.php")
    suspend fun getHomeData(@Query("user_id") userId: Int): Response<HomeData>

    @Multipart
    @POST("upload_photo.php")
    suspend fun uploadPhoto(
        @Part("pet_id") petId: RequestBody,
        @Part photo: MultipartBody.Part
    ): Response<AuthResponse>

    @POST("update_order_status.php")
    suspend fun updateOrderStatus(@Body request: UpdateOrderStatusRequest): Response<AuthResponse>

    @GET("get_notifications.php")
    suspend fun getNotifications(@Query("user_id") userId: Int): Response<List<UserNotification>>
}