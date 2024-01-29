package com.nsr.weather.hilt

import android.content.Context
import com.nsr.weather.network.APIHelper
import com.nsr.weather.network.RetrofitBuilder
import com.nsr.weather.repositories.weather.WeatherRepository
import com.nsr.weather.repositories.location.LocationDataRepository
import com.nsr.weather.utils.ResourcesProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object WeatherModule {

    @Provides
    @Singleton
    fun provideApplicationContext(@ApplicationContext context: Context): Context {
        return context
    }

    @Provides
    @Singleton
    fun provideResourcesProvider(@ApplicationContext context: Context): ResourcesProvider {
        return ResourcesProvider(context)
    }

    @Provides
    @Singleton
    fun provideAPIHelper(): APIHelper {
        return APIHelper(RetrofitBuilder.apiService)
    }

    @Provides
    @Singleton
    fun provideWeatherRepository(apiHelper: APIHelper): WeatherRepository {
        return WeatherRepository(apiHelper)
    }

    @Provides
    @Singleton
    fun provideLocationRepository(@ApplicationContext context: Context): LocationDataRepository {
        return LocationDataRepository(context)
    }
}