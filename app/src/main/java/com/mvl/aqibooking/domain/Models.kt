package com.mvl.aqibooking.domain

const val NICKNAME_MAX_LENGTH = 20

data class Coordinate(
    val latitude: Double,
    val longitude: Double
)

data class Location(
    val coordinate: Coordinate,
    val name: String, val aqi: Int
)

enum class LocationSlot {
    A,
    B
}

data class SelectedLocation(
    val location: Location,
    val nickname: String? = null
) {
    val displayName: String get() = nickname ?: location.name
}

data class TripSelection(
    val a: SelectedLocation? = null,
    val b: SelectedLocation? = null
) {
    operator fun get(slot: LocationSlot): SelectedLocation? = if (slot == LocationSlot.A) a else b

    fun with(slot: LocationSlot, location: SelectedLocation?): TripSelection =
        if (slot == LocationSlot.A) copy(a = location) else copy(b = location)

    val nextSlot: LocationSlot?
        get() = when {
            a == null -> LocationSlot.A
            b == null -> LocationSlot.B
            else -> null
        }
}

data class Booking(
    val a: SelectedLocation,
    val b: SelectedLocation,
    val price: Double
)

data class BookingHistory(
    val bookings: List<Booking>
) {
    val totalCount: Int get() = bookings.size
    val totalPrice: Double get() = bookings.sumOf { it.price }
}
