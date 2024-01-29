package com.nsr.weather.repositories.location

import android.content.Context
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

/**
 *  Repository to save and retrieve Last known location.
 *  Required to show last known location weather info on app app launch.
 */

private val Context.dataStore by preferencesDataStore(name = "geo_prefs")

class LocationDataRepository(context: Context) {
    private val geoDataStore = context.dataStore

    suspend fun saveLocationData(lat: Double, lon: Double) {
        withContext(Dispatchers.IO) {
            geoDataStore.edit { preferences ->
                preferences[KEY_LATITUDE] = lat
                preferences[KEY_LONGITUDE] = lon
            }
        }
    }

    suspend fun getLocationData(): Pair<Double?, Double?> {
        return withContext(Dispatchers.IO) {
            val preferences = geoDataStore.data.first()
            val lat = preferences[KEY_LATITUDE]
            val lon = preferences[KEY_LONGITUDE]
            Pair(lat, lon)
        }
    }

    private companion object {
        val KEY_LATITUDE = doublePreferencesKey("key_lat")
        val KEY_LONGITUDE = doublePreferencesKey("key_lon")
    }

}