package com.nsr.weather.network

import com.nsr.weather.model.weather.WeatherResponse
import com.nsr.weather.model.city.CityGeoInfo
import retrofit2.http.GET
import retrofit2.http.Url

interface RetroAPIService {
    @GET
    suspend fun getWeatherDetails(@Url url:String): WeatherResponse

    @GET
    suspend fun geCityGeoDetails(@Url url:String): CityGeoInfo
}