package com.mvl.aqibooking.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface AirQualityApi {
    @GET("feed/geo:{lat};{lng}/")
    suspend fun getAirQuality(
        @Path("lat") latitude: Double,
        @Path("lng") longitude: Double,
    ): AirQualityResponse
}

@Serializable
data class AirQualityResponse(val status: String, val data: JsonElement? = null)

interface GeocodingApi {
    @GET("data/reverse-geocode-client")
    suspend fun reverseGeocode(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("localityLanguage") language: String,
    ): GeocodingResponse
}

@Serializable
data class GeocodingResponse(
    val city: String? = null,
    val locality: String? = null,
    val localityInfo: LocalityInfo? = null,
)

@Serializable
data class LocalityInfo(val administrative: List<AdministrativeArea> = emptyList())

@Serializable
data class AdministrativeArea(val order: Int, val name: String? = null)

interface BooksApi {
    @POST("books")
    suspend fun createBook(@Body request: BookRequest): BookResponse

    @GET("books")
    suspend fun getBooks(@Query("year") year: Int, @Query("month") month: Int): List<BookResponse>
}

@Serializable
data class LocationDto(val latitude: Double, val longitude: Double, val aqi: Int, val name: String)

@Serializable
data class BookRequest(val a: LocationDto, val b: LocationDto)

@Serializable
data class BookResponse(val id: String? = null, val a: LocationDto, val b: LocationDto, val price: Double)
