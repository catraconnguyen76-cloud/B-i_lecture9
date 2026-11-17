package com.example.test.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    // Thay bằng URL HTTPS của server REST thật; URL phải kết thúc bằng dấu '/'.
    private const val BASE_URL = "https://api.example.com/"

    val studentApi: StudentApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(StudentApiService::class.java)
    }
}
