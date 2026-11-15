package com.mvl.aqibooking.data

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.mvl.aqibooking.domain.Booking
import com.mvl.aqibooking.domain.BookingRepository
import com.mvl.aqibooking.domain.Coordinate
import com.mvl.aqibooking.domain.Location
import com.mvl.aqibooking.domain.LocationRepository
import com.mvl.aqibooking.domain.SelectedLocation
import com.mvl.aqibooking.domain.TripRepository
import com.mvl.aqibooking.domain.TripSelection
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonPrimitive

class LocationRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val airQualityApi: AirQualityApi,
    private val geocodingApi: GeocodingApi,
) : LocationRepository {

    @SuppressLint("MissingPermission")
    override suspend fun getCurrentLocation(): Coordinate? {
        val granted = listOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
            .any { ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED }
        if (!granted) return null

        val client = LocationServices.getFusedLocationProviderClient(context)
        val location = client.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, null).await()
            ?: client.lastLocation.await()
        return location?.let { Coordinate(it.latitude, it.longitude) }
    }

    override suspend fun getAirQuality(coordinate: Coordinate): Int {
        val response = airQualityApi.getAirQuality(coordinate.latitude, coordinate.longitude)
        check(response.status == "ok") { "Air quality service returned an error" }
        val aqi = (response.data as? JsonObject)?.get("aqi")?.jsonPrimitive?.intOrNull
        return checkNotNull(aqi) { "No air quality reading for this location" }
    }

    override suspend fun getAddressName(coordinate: Coordinate): String {
        val response = geocodingApi.reverseGeocode(coordinate.latitude, coordinate.longitude, language = "en")
        return checkNotNull(formatAddressName(response)) { "No address found for this location" }
    }
}

internal fun formatAddressName(response: GeocodingResponse): String? {
    val name = response.localityInfo?.administrative.orEmpty()
        .filter { !it.name.isNullOrBlank() }
        .sortedBy { it.order }
        .takeLast(2)
        .joinToString(", ") { it.name.orEmpty() }
    return name.ifBlank { response.locality ?: response.city }
}

class BookingRepositoryImpl @Inject constructor(private val booksApi: BooksApi) : BookingRepository {

    override suspend fun createBooking(a: SelectedLocation, b: SelectedLocation): Booking {
        val response = booksApi.createBook(BookRequest(a = a.location.toDto(), b = b.location.toDto()))
        return Booking(
            a = SelectedLocation(response.a.toLocation(), a.nickname),
            b = SelectedLocation(response.b.toLocation(), b.nickname),
            price = response.price,
        )
    }

    override suspend fun getBookings(year: Int, month: Int): List<Booking> =
        booksApi.getBooks(year, month).map { response ->
            Booking(
                a = SelectedLocation(response.a.toLocation()),
                b = SelectedLocation(response.b.toLocation()),
                price = response.price,
            )
        }

    private fun Location.toDto() = LocationDto(coordinate.latitude, coordinate.longitude, aqi, name)

    private fun LocationDto.toLocation() = Location(Coordinate(latitude, longitude), name, aqi)
}

@Singleton
class TripRepositoryImpl @Inject constructor() : TripRepository {

    private val _selection = MutableStateFlow(TripSelection())
    override val selection: StateFlow<TripSelection> = _selection.asStateFlow()

    override fun update(transform: (TripSelection) -> TripSelection) = _selection.update(transform)

    override fun clear() {
        _selection.value = TripSelection()
    }
}
