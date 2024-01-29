package com.nsr.weather.viewmodels

import com.nsr.weather.model.city.CityGeoInfo
import com.nsr.weather.model.city.CityGeoInfoItem
import com.nsr.weather.model.weather.Clouds
import com.nsr.weather.model.weather.Coord
import com.nsr.weather.model.weather.Main
import com.nsr.weather.model.weather.Sys
import com.nsr.weather.model.weather.Weather
import com.nsr.weather.model.weather.WeatherResponse
import com.nsr.weather.model.weather.Wind
import com.nsr.weather.network.APIHelper
import com.nsr.weather.repositories.weather.WeatherRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class WeatherRepositoryTests {

    private val mockApiService: APIHelper = mockk()
    private val weatherRepository = WeatherRepository(mockApiService)

    @Test
    fun `test fetchWeatherDataFromApi success`() = runBlocking {

        coEvery { mockApiService.getWeatherDetails(any()) } returns weatherResponse

        val result = weatherRepository.fetchWeatherDataFromApi(LATITUDE.toString(), LONGITUDE.toString())

        assertEquals(weatherResponse, result)
    }

    @Test
    fun `test fetchWeatherDataFromApi error`() = runBlocking {

        coEvery { mockApiService.getWeatherDetails(any()) } throws RuntimeException("API error")

        val result = weatherRepository.fetchWeatherDataFromApi(LATITUDE.toString(), LONGITUDE.toString())

        assertNull(result)
    }

    @Test
    fun `test fetchCityGeoApi success`() = runBlocking {

        coEvery { mockApiService.getCityGeoDetails(any()) } returns getSampleCityGeoData()
        coEvery { mockApiService.getWeatherDetails(any()) } returns weatherResponse

        val result = weatherRepository.fetchCityGeoApi(CITY_NAME)

        assertEquals(weatherResponse, result)
    }

    @Test
    fun `test fetchCityGeoApi error`() = runBlocking {
        coEvery { mockApiService.getCityGeoDetails(any()) } throws RuntimeException("API error")

        val result = weatherRepository.fetchCityGeoApi(CITY_NAME)

        assertNull(result)
    }

    private companion object {

        const val LATITUDE = -80.8431
        const val LONGITUDE = 35.2271
        const val CITY_NAME = "Charlotte"

        private fun getSampleCityGeoData(): CityGeoInfo {
            return CityGeoInfo().apply {
                add(CityGeoInfoItem(country = "US",
                    lat =  LATITUDE,
                    lon = LONGITUDE,
                    name = "Charlotte",
                    state = "North Carolina"))
            }
        }

        //API Response
        val weatherResponse = WeatherResponse(
            base = "stations",
            clouds = Clouds(
                all = 40
            ),
            cod = 200,
            coord = Coord(
                lat = -80.8431,
                lon = 35.2271
            ),
            dt = 1706495527,
            id = 4460243,
            main =  Main(
                feels_like = 278.69,
                humidity = 64,
                pressure = 1010,
                temp = 280.42,
                temp_max = 281.25,
                temp_min = 279.17
            ),
            name = "Charlotte",
            sys = Sys(
                country = "US",
                id = 2007844,
                sunrise = 1706444742,
                sunset = 1706481993,
                type = 2
            ),
            timezone = -18000,
            visibility = 10000,
            weather =  listOf(
                Weather(
                    description = "light rain",
                    icon = "10n",
                    id = 500,
                    main = "Rain"
                )
            ) ,
            wind =  Wind(
                deg = 270,
                speed = 2.57
            )
        )
    }
}