package com.example.vetclient.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    // ВАЖНО: 10.0.2.2 — это специальный адрес для эмулятора Android,
    // который указывает на localhost вашего компьютера (Laragon).
    private const val BASE_URL = "http://10.0.2.2/vetapi/"

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}