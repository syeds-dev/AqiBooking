package com.mvl.aqibooking.domain

import kotlinx.coroutines.flow.StateFlow

interface LocationRepository {
    suspend fun getCurrentLocation(): Coordinate?
    suspend fun getAirQuality(coordinate: Coordinate): Int
    suspend fun getAddressName(coordinate: Coordinate): String
}

interface BookingRepository {
    suspend fun createBooking(a: SelectedLocation, b: SelectedLocation): Booking

    suspend fun getBookings(year: Int, month: Int): List<Booking>
}

interface TripRepository {
    val selection: StateFlow<TripSelection>
    fun update(transform: (TripSelection) -> TripSelection)
    fun clear()
}
