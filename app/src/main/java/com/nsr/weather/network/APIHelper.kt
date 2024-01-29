package com.nsr.weather.network

class APIHelper(private val apiService: RetroAPIService) {
    suspend fun getWeatherDetails(url: String) = apiService.getWeatherDetails(url)
    suspend fun getCityGeoDetails(url: String) = apiService.geCityGeoDetails(url)
}