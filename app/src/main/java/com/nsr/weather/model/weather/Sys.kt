package com.nsr.weather.model.weather

data class Sys(
    val country: String,
    val id: Int,
    val sunrise: Int,
    val sunset: Int,
    val type: Int
)