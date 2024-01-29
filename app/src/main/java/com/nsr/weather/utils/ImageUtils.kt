package com.nsr.weather.utils

import android.widget.ImageView
import com.nsr.weather.R
import com.squareup.picasso.Picasso

const val WEATHER_IMAGE_URL = "https://openweathermap.org/img/wn/%s@4x.png"

fun loadWeatherIcon(icon: String, imageView: ImageView) {
    Picasso.get()
        .load(String.format(WEATHER_IMAGE_URL, icon))
        .error(R.drawable.ic_launcher_foreground)
        .into(imageView)
}