package com.nsr.weather.network

import com.nsr.weather.R

/**
 *  API Response data model to handle different states, Ex: Loading, Success, Error
 */

data class ResponseResource<out T>(val status: Status, val data: T? = null, val message: Int = R.string.error_msg) {
    companion object {
        fun <T> success(data: T): ResponseResource<T> = ResponseResource(status = Status.SUCCESS, data = data)

        fun <T> error(message: Int): ResponseResource<T> =
            ResponseResource(status = Status.ERROR, message = message)

        fun <T> loading(): ResponseResource<T> = ResponseResource(status = Status.INITIAL_LOADING)
    }
}

