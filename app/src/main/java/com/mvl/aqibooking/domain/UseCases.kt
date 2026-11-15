package com.mvl.aqibooking.domain

import java.time.Clock
import java.time.YearMonth
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.StateFlow

class GetCurrentLocationUseCase @Inject constructor(private val locationRepository: LocationRepository) {
    suspend operator fun invoke(): Coordinate? =
        runCatchingCancellable { locationRepository.getCurrentLocation() }.getOrNull()
}

class GetAirQualityUseCase @Inject constructor(private val locationRepository: LocationRepository) {
    suspend operator fun invoke(coordinate: Coordinate): Result<Int> =
        runCatchingCancellable { locationRepository.getAirQuality(coordinate) }
}

class ObserveTripUseCase @Inject constructor(private val tripRepository: TripRepository) {
    operator fun invoke(): StateFlow<TripSelection> = tripRepository.selection
}

class SelectLocationUseCase @Inject constructor(
    private val locationRepository: LocationRepository,
    private val tripRepository: TripRepository,
) {
    suspend operator fun invoke(coordinate: Coordinate): Result<Unit> = runCatchingCancellable {
        val slot = checkNotNull(tripRepository.selection.value.nextSlot) { "A and B are already set" }
        val location = coroutineScope {
            val name = async { locationRepository.getAddressName(coordinate) }
            val aqi = async { locationRepository.getAirQuality(coordinate) }
            Location(coordinate, name.await(), aqi.await())
        }
        tripRepository.update { it.with(slot, SelectedLocation(location)) }
    }
}

class UpdateNicknameUseCase @Inject constructor(private val tripRepository: TripRepository) {
    operator fun invoke(slot: LocationSlot, nickname: String): Result<Unit> = runCatching {
        val trimmed = nickname.trim()
        require(trimmed.length <= NICKNAME_MAX_LENGTH) { "Nickname is longer than $NICKNAME_MAX_LENGTH characters" }
        tripRepository.update { selection ->
            val current = checkNotNull(selection[slot]) { "Location $slot is not set" }
            selection.with(slot, current.copy(nickname = trimmed.ifEmpty { null }))
        }
    }
}

class BookTripUseCase @Inject constructor(
    private val bookingRepository: BookingRepository,
    private val tripRepository: TripRepository,
) {
    suspend operator fun invoke(): Result<Booking> = runCatchingCancellable {
        val selection = tripRepository.selection.value
        val a = checkNotNull(selection.a) { "A is not set" }
        val b = checkNotNull(selection.b) { "B is not set" }
        bookingRepository.createBooking(a, b).also { tripRepository.clear() }
    }
}

class GetMonthlyHistoryUseCase @Inject constructor(
    private val bookingRepository: BookingRepository,
    private val clock: Clock,
) {
    suspend operator fun invoke(): Result<BookingHistory> = runCatchingCancellable {
        val now = YearMonth.now(clock)
        BookingHistory(bookingRepository.getBookings(now.year, now.monthValue))
    }
}

private inline fun <T> runCatchingCancellable(block: () -> T): Result<T> =
    try {
        Result.success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.failure(e)
    }
