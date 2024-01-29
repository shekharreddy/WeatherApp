package com.nsr.weather.model.city

data class CityGeoInfoItem(
    val country: String,
    val lat: Double,
    val lon: Double,
    val name: String,
    val state: String
)