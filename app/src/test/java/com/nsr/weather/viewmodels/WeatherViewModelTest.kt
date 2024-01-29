package com.nsr.weather.viewmodels

import android.util.Log
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.nsr.weather.R
import com.nsr.weather.model.weather.Clouds
import com.nsr.weather.model.weather.Coord
import com.nsr.weather.model.weather.Main
import com.nsr.weather.model.weather.Sys
import com.nsr.weather.model.weather.Weather
import com.nsr.weather.model.weather.WeatherResponse
import com.nsr.weather.model.weather.Wind
import com.nsr.weather.network.ResponseResource
import com.nsr.weather.network.Status
import com.nsr.weather.repositories.location.LocationDataRepository
import com.nsr.weather.repositories.weather.WeatherRepository
import com.nsr.weather.utils.ResourcesProvider
import io.mockk.CapturingSlot
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verifySequence
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import java.io.IOException

@ExperimentalCoroutinesApi
class WeatherViewModelTest {

    private lateinit var viewModel: WeatherViewModel
    private lateinit var weatherRepository: WeatherRepository
    private lateinit var locationDataRepository: LocationDataRepository
    private lateinit var resProvider: ResourcesProvider

    @get:Rule
    var rule: TestRule = InstantTaskExecutorRule()


    @Before
    fun setUp() {
        // Mock the repository and API service
        Dispatchers.setMain(Dispatchers.Unconfined)
        weatherRepository = mockk()
        locationDataRepository = mockk()
        resProvider = mockk()
        viewModel = WeatherViewModel(
            resources = resProvider,
            weatherRepository = weatherRepository,
            locationDataRepository = locationDataRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    @ExperimentalCoroutinesApi
    fun `test success scenario for weather request using lat and lon`() = runTest {
        // Mock resources
        setMockResources()
        val observer = mockk<Observer<ResponseResource<WeatherItem>>>(relaxUnitFun = true)
        val valuesCaptured: CapturingSlot<ResponseResource<WeatherItem>> = slot()

        // Mock repository responses
        coEvery {
            weatherRepository.fetchWeatherDataFromApi(LATITUDE.toString(), LONGITUDE.toString())
        } returns weatherResponse
        coEvery {
            locationDataRepository.saveLocationData(any(), any())
        } just Runs

        every { observer.onChanged(capture(valuesCaptured)) } answers { /* store captured values */ }

        advanceUntilIdle() // Runs the new coroutine

        // set observer for livedata changes
        viewModel.weatherResponse.observeForever(observer)

        //Call API
        viewModel.fetchWeatherInfoByLatLon(LATITUDE, LONGITUDE)
        // Verify callbacks in sequence
        verifySequence {
            observer.onChanged(loadingState)
            observer.onChanged(successState)
        }
    }

    @Test
    @ExperimentalCoroutinesApi
    fun `test error scenario for weather request using lat and lon`() = runTest {
        // Mock resources
        setMockResources()
        val observer = mockk<Observer<ResponseResource<WeatherItem>>>(relaxUnitFun = true)
        val valuesCaptured: CapturingSlot<ResponseResource<WeatherItem>> = slot()
        // Mock repository responses
        coEvery {
            weatherRepository.fetchWeatherDataFromApi(LATITUDE.toString(), LONGITUDE.toString())
        } throws IOException("Mocked error")
        coEvery {
            locationDataRepository.saveLocationData(any(), any())
        } just Runs
        every { observer.onChanged(capture(valuesCaptured)) } answers { /* store captured values  */ }

        advanceUntilIdle() // Runs the new coroutine
        // set observer for livedata changes
        viewModel.weatherResponse.observeForever(observer)

        //Call API
        viewModel.fetchWeatherInfoByLatLon(LATITUDE, LONGITUDE)

        // Verify callbacks in sequence
        verifySequence {
            observer.onChanged(loadingState)
            observer.onChanged(errorState)
        }
    }

    @Test
    @ExperimentalCoroutinesApi
    fun `test success scenario for weather request using city name`() = runTest {
        // Mock resources
        setMockResources()
        val observer = mockk<Observer<ResponseResource<WeatherItem>>>(relaxUnitFun = true)
        val valuesCaptured: CapturingSlot<ResponseResource<WeatherItem>> = slot()
        // Mock repository responses
        coEvery {
            weatherRepository.fetchCityGeoApi(CITY_NAME)
        } returns weatherResponse
        coEvery {
            locationDataRepository.saveLocationData(any(), any())
        } just Runs

        every { observer.onChanged(capture(valuesCaptured)) } answers { /* store captured values */ }

        advanceUntilIdle() // Runs the new coroutine

        // set observer for livedata changes
        viewModel.weatherResponse.observeForever(observer)

        //Call API
        viewModel.fetchWeatherInfoByCityName(CITY_NAME)

        // Verify callbacks in sequence
        verifySequence {
            observer.onChanged(loadingState)
            observer.onChanged(successState)
        }
    }

    @Test
    @ExperimentalCoroutinesApi
    fun `test error scenario for weather request using city name`() = runTest {
        // Mock resources
        setMockResources()
        val observer = mockk<Observer<ResponseResource<WeatherItem>>>(relaxUnitFun = true)
        val valuesCaptured: CapturingSlot<ResponseResource<WeatherItem>> = slot()
        // Mock repository responses
        coEvery {
            weatherRepository.fetchCityGeoApi(CITY_NAME)
        } throws IOException("Mocked error")
        coEvery {
            locationDataRepository.saveLocationData(any(), any())
        } just Runs

        every { observer.onChanged(capture(valuesCaptured)) } answers { /* store captured values */ }

        advanceUntilIdle() // Runs the new coroutine

        // set observer for livedata changes
        viewModel.weatherResponse.observeForever(observer)

        //Call API
        viewModel.fetchWeatherInfoByCityName(CITY_NAME)

        // Verify callbacks in sequence
        verifySequence {
            observer.onChanged(loadingState)
            observer.onChanged(errorState)
        }
    }

    @Test
    fun `test lat lon save and retrieval`() = runTest {
        coEvery { locationDataRepository.saveLocationData(LATITUDE, LONGITUDE) } just Runs

        viewModel.saveLocationData(LATITUDE, LONGITUDE)
        coEvery { locationDataRepository.getLocationData() } returns Pair(LATITUDE, LONGITUDE)

        viewModel.retrieveLastLocationData {
            assertEquals(it.first, LATITUDE)
            assertEquals(it.second, LONGITUDE)
        }
    }

    private fun setMockResources(){
        every { resProvider.getString(R.string.humidity) } returns "%s Humidity"
        every { resProvider.getString(R.string.temperature) } returns "%s F"
        every { resProvider.getString(R.string.minMaxFeelsLike) } returns "%s / %s Feels Like %s"
    }

    private companion object {

        const val LATITUDE = -80.8431
        const val LONGITUDE = 35.2271
        const val CITY_NAME = "Charlotte"

        //UI related model data
        val weatherItem = WeatherItem(
            description = "Rain - light rain",
            cityName= "Charlotte",
            temperature = "45 F",
            humidity = "64 Humidity",
            minMaxFeelsLike = "43 / 47 Feels Like 42",
            iconURL="10n"
        )

        val loadingState = ResponseResource(status = Status.INITIAL_LOADING, data = null, message = R.string.error_msg)
        val successState = ResponseResource(status = Status.SUCCESS, data = weatherItem, message = R.string.error_msg)
        val errorState = ResponseResource(status= Status.ERROR, data=null, message = R.string.error_msg)

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