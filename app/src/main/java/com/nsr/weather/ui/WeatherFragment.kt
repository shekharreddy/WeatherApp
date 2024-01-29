package com.nsr.weather.ui

import android.content.Context
import android.os.Bundle
import android.view.KeyEvent
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.fragment.app.viewModels
import com.nsr.weather.R
import com.nsr.weather.databinding.FragmentWeatherBinding
import com.nsr.weather.location.LocationHelper
import com.nsr.weather.network.ResponseResource
import com.nsr.weather.network.Status
import com.nsr.weather.utils.loadWeatherIcon
import com.nsr.weather.viewmodels.WeatherItem
import com.nsr.weather.viewmodels.WeatherViewModel
import dagger.hilt.android.AndroidEntryPoint

/**
 * Fragment to Show Weather Details
 */

@AndroidEntryPoint
class WeatherFragment : Fragment() {

    private val viewModel: WeatherViewModel by viewModels()
    private lateinit var locationHelper: LocationHelper

    private var _binding: FragmentWeatherBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWeatherBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        locationHelper = LocationHelper(this)
        setupViewModelObservers()
        initializeListeners()
        fetchLastKnownLocationWeather()
    }

    // Initialize Input and button listeners
    private fun initializeListeners() {
        //Given more time, will add a textlistener on input field to
        // enable/disable the status of submit button based on entered text.
        with(binding) {
            cityNameInput.setOnEditorActionListener { input, actionId, event ->
                if (actionId == EditorInfo.IME_ACTION_DONE ||
                    (event != null && event.action == KeyEvent.ACTION_DOWN && event.keyCode == KeyEvent.KEYCODE_ENTER)
                ) {
                    val cityName = input.text.toString()
                    if (cityName.isNotBlank()) {
                        fetchEnteredCityWeather(cityName)
                    }
                    return@setOnEditorActionListener true
                }
                false
            }

            btnCurrentLocation.setOnClickListener {
                locationHelper.setCallback { isGranted, location ->
                    if (isGranted) {
                        location?.let {
                            viewModel.fetchWeatherInfoByLatLon(it.latitude, it.longitude)
                        }?: run {
                            showErrorMessage(R.string.location_not_found)
                        }
                    } else {
                        // Permission denied
                        showErrorMessage(R.string.location_permission_error)
                    }
                }
                locationHelper.requestLocationPermission()
            }

            btnCitySubmit.setOnClickListener {
                val cityName = cityNameInput.text.toString()
                if(cityName.isNotBlank()) {
                    fetchEnteredCityWeather(cityName)
                }
            }
        }
    }

    //On app start show last known location weather info if any.
    private fun fetchLastKnownLocationWeather() {
        with(viewModel) {
            retrieveLastLocationData { lastKnownLocation ->
                val (lat, lon) = lastKnownLocation
                if(lat != null && lon != null) {
                    fetchWeatherInfoByLatLon(lat, lon)
                }
            }
        }
    }

    private fun fetchEnteredCityWeather(cityName: String){
        viewModel.fetchWeatherInfoByCityName(cityName)
        val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
        imm?.hideSoftInputFromWindow(binding.cityNameInput.windowToken, 0)
    }

    private fun setupViewModelObservers() {
        viewModel.weatherResponse.observe(viewLifecycleOwner) {
            it?.let { response ->
                handleResponse(response)
            }?: run {
                showErrorMessage(R.string.error_msg)
            }
        }
    }

    private fun showErrorMessage(msgRes: Int) {
        Toast.makeText(context, resources.getString(msgRes), Toast.LENGTH_LONG).show()
    }

    //Update weather info on UI
    private fun updateUI(item: WeatherItem) {
        // With more time, I'll add conditions to check invalid data (Ex: null) and hide the views.
        // will provide additional views with useful data like sunrise, sunset, etc
        with(binding) {
            cityNameView.text = item.cityName
            temperatureView.text = item.temperature
            descriptionView.text = item.description
            feelsLikeView.text= item.minMaxFeelsLike
            humidityView.text = item.humidity
            loadWeatherIcon(item.iconURL, weatherIcon)
        }
    }

    private fun handleResponse(response: ResponseResource<WeatherItem>) {
        with(binding) {
            when (response.status) {
                Status.SUCCESS -> {
                    progressBar.visibility = View.GONE
                    // Clear the City Name edittext
                    cityNameInput.setText("")
                    response.data?.let {
                        updateUI(it)
                    } ?: run {
                        showErrorMessage(R.string.error_msg)
                    }
                }
                Status.ERROR -> {
                    binding.progressBar.visibility = View.GONE
                    showErrorMessage(response.message)
                }
                Status.INITIAL_LOADING -> {
                    progressBar.visibility = View.VISIBLE
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}