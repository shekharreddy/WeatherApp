package com.nsr.weather.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import android.content.Intent
import android.provider.Settings

/**
 *  Location Helper to request location permission and to get use location info.
 */

class LocationHelper(
    private val context: Fragment
) {
    private val requestLocationPermissionLauncher =
        context.registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            onLocationPermissionResult(isGranted)
        }

    private var locationPermissionCallback: ((Boolean, Location?) -> Unit)? = null

    // With more time, I'll include a popup to show how location permission is used
    // will handle error conditions with proper messages.

    fun setCallback(callback: (Boolean, Location?) -> Unit) {
        locationPermissionCallback = callback
    }

    fun requestLocationPermission() {
        when {
            isLocationServiceEnabled() -> {
                if (ContextCompat.checkSelfPermission(
                        context.requireContext(),
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    // Permission granted, get the last known location
                    val lastKnownLocation = getLastKnownLocation()
                    locationPermissionCallback?.invoke(true, lastKnownLocation)
                } else {
                    // Request the permission
                    requestLocationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                }
            }
            else -> promptUserToEnableLocation()
        }
    }

     private fun isLocationServiceEnabled(): Boolean {
        val locationManager = context.requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    private fun promptUserToEnableLocation() {
        val settingsIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        context.startActivity(settingsIntent)
    }

    private fun getLastKnownLocation(): Location? {
        val locationManager = context.requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return try {
            val providers = locationManager.getProviders(true)
            var bestLocation: Location? = null
            for (provider in providers) {
                val location = locationManager.getLastKnownLocation(provider) ?: continue
                if (bestLocation == null || location.accuracy < bestLocation.accuracy) {
                    bestLocation = location
                }
            }
            bestLocation
        } catch (e: SecurityException) {
            null
        }
    }

    private fun onLocationPermissionResult(isGranted: Boolean) {
        val lastKnownLocation = if (isGranted) getLastKnownLocation() else null
        locationPermissionCallback?.invoke(isGranted, lastKnownLocation)
    }
}