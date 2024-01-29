package com.nsr.weather.utils

// With more time, will provide user to change the Temperature Unit (F/C/K)
// currently it will show only in Fahrenheit, by using kelvinToFahrenheit()

fun kelvinToCelsius(kelvin: Double): Double {
    return kelvin - 273.15
}

fun celsiusToFahrenheit(celsius: Double): Double {
    return (celsius * 9 / 5) + 32
}

fun kelvinToFahrenheit(kelvin: Double): String {
    val celsius = kelvinToCelsius(kelvin)
    return String.format("%.0f", celsiusToFahrenheit(celsius))
}