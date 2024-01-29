package com.nsr.weather.repositories.weather

import com.nsr.weather.model.weather.WeatherResponse
import com.nsr.weather.network.APIHelper
import com.nsr.weather.network.RetrofitBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/***
 *  Repository to fetch weather info using City name or with Latitude and Longitude
 */
class WeatherRepository(private val apiService: APIHelper) {
    // With more time, I'll include additional error conditions to handle multiple error scenarios.

    suspend fun fetchWeatherDataFromApi(lat: String, long: String): WeatherResponse? = withContext(Dispatchers.IO) {
        try {
            return@withContext apiService.getWeatherDetails(String.format(GET_LAT_LONG, lat, long))
        } catch (e: Exception) {
            return@withContext null
        }
    }

    suspend fun fetchCityGeoApi(cityName: String): WeatherResponse? = withContext(Dispatchers.IO) {
        try {
            val cityGeoInfo =  apiService.getCityGeoDetails(String.format(CITY_URL, cityName))

            return@withContext fetchWeatherDataFromApi(
                cityGeoInfo[0].lat.toString(),
                cityGeoInfo[0].lon.toString()
            )
        } catch (e: Exception) {
            return@withContext null
        }
    }

    private companion object {
        const val GET_LAT_LONG = "weather?lat=%s&lon=%s&appid=${RetrofitBuilder.API_KEY}"
        const val CITY_URL = "https://api.openweathermap.org/geo/1.0/direct?q=%s&appid=${RetrofitBuilder.API_KEY}"
    }
}