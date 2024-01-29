package com.nsr.weather.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nsr.weather.R
import com.nsr.weather.model.weather.WeatherResponse
import com.nsr.weather.network.ResponseResource
import com.nsr.weather.repositories.weather.WeatherRepository
import com.nsr.weather.repositories.location.LocationDataRepository
import com.nsr.weather.utils.ResourcesProvider
import com.nsr.weather.utils.kelvinToFahrenheit
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class WeatherViewModel @Inject constructor(
    private val resources: ResourcesProvider,
    private val weatherRepository: WeatherRepository,
    private val locationDataRepository: LocationDataRepository
) : ViewModel() {

    private val _weatherResponse: MutableLiveData<ResponseResource<WeatherItem>> by lazy {
        MutableLiveData<ResponseResource<WeatherItem>>()
    }

    val weatherResponse : LiveData<ResponseResource<WeatherItem>>
        get() = _weatherResponse

    //To update loading status on the UI
    private fun updateLoadingStatus(){
        _weatherResponse.value = ResponseResource.loading()
    }

    //Fetch Weather details using service by City Name
    fun fetchWeatherInfoByCityName(cityName: String) {
        updateLoadingStatus()
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val weatherResponse = weatherRepository.fetchCityGeoApi(cityName)
                    weatherResponse?.let {
                        updateLiveData(it)
                    } ?:run {
                        postErrorMessage()
                    }
                } catch (e: Exception) {
                    postErrorMessage()
                }
            }
        }
    }

    //Fetch Weather details using service by Lat and Lon
    fun fetchWeatherInfoByLatLon(lat: Double, lon: Double) {
        updateLoadingStatus()
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val weatherResponse = weatherRepository.fetchWeatherDataFromApi(lat.toString(), lon.toString())
                    weatherResponse?.let {
                        updateLiveData(it)
                    } ?:run {
                        postErrorMessage()
                    }
                } catch (e: Exception) {
                    postErrorMessage()
                }
            }
        }
    }

    private fun postErrorMessage(){
        // With more time, I'll include additional error conditions.
        _weatherResponse.postValue(
            ResponseResource.error(message = R.string.error_msg)
        )
    }

    //Update WeatherResponse with latest data and live data
    private fun updateLiveData(weatherRes: WeatherResponse) {
        val weatherItem = weatherRes.weather[0]
        val weatherMainInfo = weatherRes.main
        val response  = with(weatherItem) {
            WeatherItem(
                description = "$main - $description",
                iconURL = icon,
                cityName = weatherRes.name,
                humidity = String.format(resources.getString(R.string.humidity),
                    weatherMainInfo.humidity),
                temperature = String.format(resources.getString(R.string.temperature),
                    kelvinToFahrenheit(weatherRes.main.temp)),
                minMaxFeelsLike =
                String.format(resources.getString(R.string.minMaxFeelsLike),
                    kelvinToFahrenheit(weatherMainInfo.temp_min),
                    kelvinToFahrenheit(weatherMainInfo.temp_max),
                    kelvinToFahrenheit(weatherMainInfo.feels_like)
                )
            )
        }
        saveLocationData(lat = weatherRes.coord.lat, weatherRes.coord.lon)
        _weatherResponse.postValue(ResponseResource.success(data = response))
    }

    // Save location data on successful response, to use on app relaunch
    fun saveLocationData(lat: Double, lon: Double) {
        viewModelScope.launch {
            locationDataRepository.saveLocationData(lat, lon)
        }
    }

    // Get location data app relaunch to show prior location's weather
    fun retrieveLastLocationData(callback: (Pair<Double?, Double?>) -> Unit){
        viewModelScope.launch {
            val latLon = locationDataRepository.getLocationData()
            callback(latLon)
        }
    }

}

//Simple Data class to construct all the required data for UI
data class WeatherItem(
    val description: String,
    val cityName: String,
    val temperature: String,
    val humidity: String,
    val minMaxFeelsLike: String,
    val iconURL: String,
)
