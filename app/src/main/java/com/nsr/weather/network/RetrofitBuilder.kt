package com.nsr.weather.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitBuilder {
    const val API_KEY = "a36e1336192e9829f1c9f736835a79c1"
    private const val BASE_URL = "https://api.openweathermap.org/data/2.5/"

    private var loggingInterceptor =  HttpLoggingInterceptor().also {
        it.level = HttpLoggingInterceptor.Level.BODY
    }

    private val client: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()

    private fun getRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val apiService: RetroAPIService = getRetrofit().create(RetroAPIService::class.java)
}