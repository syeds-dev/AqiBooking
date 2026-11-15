package com.mvl.aqibooking.data

import java.time.Clock
import java.time.YearMonth
import java.util.Objects
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.roundToLong
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer

@Singleton
class MockApiInterceptor @Inject constructor(
    private val json: Json,
    private val clock: Clock,
) : Interceptor {

    private val createdBookings = mutableListOf<Pair<YearMonth, BookResponse>>()

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val path = request.url.pathSegments.filter { it.isNotEmpty() }

        val (code, body) = when {
            path.firstOrNull() == "feed" -> airQuality(path.getOrNull(1))
            path.lastOrNull() == "books" && request.method == "POST" -> createBooking(request)
            path.lastOrNull() == "books" && request.method == "GET" -> listBookings(request)
            else -> return chain.proceed(request)
        }

        return Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(code)
            .message(if (code < 300) "OK" else "Error")
            .body(body.toResponseBody(JSON_MEDIA_TYPE))
            .build()
    }

    private fun airQuality(geoSegment: String?): Pair<Int, String> {
        val values = geoSegment?.removePrefix("geo:")?.split(";")?.mapNotNull { it.toDoubleOrNull() }
        if (values == null || values.size != 2) return errorResponse("Expected /feed/geo:{lat};{lng}/")

        val seed = Objects.hash(
            (values[0] * 1000).toLong(),
            (values[1] * 1000).toLong(),
            clock.millis() / AQI_REFRESH_MILLIS,
        )
        val aqi = Random(seed).nextInt(MIN_AQI, MAX_AQI)
        return 200 to buildJsonObject {
            put("status", "ok")
            putJsonObject("data") { put("aqi", aqi) }
        }.toString()
    }

    private fun createBooking(request: Request): Pair<Int, String> {
        val rawBody = request.body?.let { body -> Buffer().also { body.writeTo(it) }.readUtf8() }
            ?: return errorResponse("Request body is required")
        val payload = runCatching { json.decodeFromString(BookRequest.serializer(), rawBody) }.getOrNull()
            ?: return errorResponse("Invalid request body")

        val booking = BookResponse(
            id = UUID.randomUUID().toString(),
            a = payload.a,
            b = payload.b,
            price = priceFor(payload.a, payload.b),
        )
        synchronized(createdBookings) { createdBookings += YearMonth.now(clock) to booking }
        return 201 to json.encodeToString(BookResponse.serializer(), booking)
    }

    private fun listBookings(request: Request): Pair<Int, String> {
        val year = request.url.queryParameter("year")?.toIntOrNull()
        val month = request.url.queryParameter("month")?.toIntOrNull()
        if (year == null || month == null || month !in 1..12) {
            return errorResponse("Query parameters year and month are required")
        }

        val yearMonth = YearMonth.of(year, month)
        val created = synchronized(createdBookings) {
            createdBookings.filter { it.first == yearMonth }.map { it.second }
        }
        val bookings = created.reversed() + pastBookings(yearMonth)
        return 200 to json.encodeToString(ListSerializer(BookResponse.serializer()), bookings)
    }

    private fun pastBookings(yearMonth: YearMonth): List<BookResponse> {
        val random = Random(yearMonth.year * 100 + yearMonth.monthValue)

        fun randomLocation() = LocationDto(
            latitude = round4(SEOUL_LAT + random.nextDouble(-0.05, 0.05)),
            longitude = round4(SEOUL_LNG + random.nextDouble(-0.05, 0.05)),
            aqi = random.nextInt(MIN_AQI, MAX_AQI),
            name = SEOUL_DISTRICTS[random.nextInt(SEOUL_DISTRICTS.size)],
        )

        return List(random.nextInt(2, 5)) { index ->
            val a = randomLocation()
            val b = randomLocation()
            BookResponse(id = "past-$yearMonth-$index", a = a, b = b, price = priceFor(a, b))
        }
    }

    private fun priceFor(a: LocationDto, b: LocationDto): Double {
        val dLat = Math.toRadians(b.latitude - a.latitude)
        val dLng = Math.toRadians(b.longitude - a.longitude)
        val h = sin(dLat / 2).pow(2) +
            cos(Math.toRadians(a.latitude)) * cos(Math.toRadians(b.latitude)) * sin(dLng / 2).pow(2)
        val distanceKm = 2 * EARTH_RADIUS_KM * asin(sqrt(h))
        return ((BASE_FARE + distanceKm * FARE_PER_KM) / 100).roundToLong() * 100.0
    }

    private fun round4(value: Double): Double = (value * 10_000).roundToLong() / 10_000.0

    private fun errorResponse(message: String): Pair<Int, String> =
        400 to buildJsonObject { put("error", message) }.toString()

    private companion object {
        val JSON_MEDIA_TYPE = "application/json".toMediaType()
        const val AQI_REFRESH_MILLIS = 10 * 60 * 1000L
        const val MIN_AQI = 10
        const val MAX_AQI = 180
        const val BASE_FARE = 4000.0
        const val FARE_PER_KM = 1000.0
        const val EARTH_RADIUS_KM = 6371.0
        const val SEOUL_LAT = 37.5665
        const val SEOUL_LNG = 126.9780
        val SEOUL_DISTRICTS = listOf(
            "Gangnam District, Yeoksam 1-dong",
            "Seocho District, Yangjae 2(i)-dong",
            "Jongno District, Sajik-dong",
            "Mapo District, Hapjeong-dong",
            "Yongsan District, Itaewon 1-dong",
            "Songpa District, Jamsil 3-dong",
        )
    }
}
