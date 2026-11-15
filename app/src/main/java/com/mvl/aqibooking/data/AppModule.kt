package com.mvl.aqibooking.data

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.mvl.aqibooking.BuildConfig
import com.mvl.aqibooking.domain.BookingRepository
import com.mvl.aqibooking.domain.LocationRepository
import com.mvl.aqibooking.domain.TripRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.time.Clock
import javax.inject.Singleton
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val AIR_QUALITY_URL = "https://api.waqi.info/"
    private const val GEOCODING_URL = "https://api.bigdatacloud.net/"
    private const val BOOKS_URL = "https://api.mvl-books.mock/"

    @Provides
    @Singleton
    fun provideJson(): Json = Json { ignoreUnknownKeys = true }

    @Provides
    @Singleton
    fun provideClock(): Clock = Clock.systemDefaultZone()

    @Provides
    @Singleton
    fun provideAirQualityApi(json: Json, mock: MockApiInterceptor): AirQualityApi {
        val token = BuildConfig.AQICN_TOKEN
        val client = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val url = chain.request().url.newBuilder().addQueryParameter("token", token).build()
                chain.proceed(chain.request().newBuilder().url(url).build())
            }
            .apply { if (token.isBlank()) addInterceptor(mock) }
            .build()
        return retrofit(AIR_QUALITY_URL, client, json).create(AirQualityApi::class.java)
    }

    @Provides
    @Singleton
    fun provideGeocodingApi(json: Json): GeocodingApi {
        val client = OkHttpClient.Builder().addDebugLogging().build()
        return retrofit(GEOCODING_URL, client, json).create(GeocodingApi::class.java)
    }

    @Provides
    @Singleton
    fun provideBooksApi(json: Json, mock: MockApiInterceptor): BooksApi {
        val client = OkHttpClient.Builder()
            .addDebugLogging()
            .apply { if (BuildConfig.USE_MOCK_BOOKS_API) addInterceptor(mock) }
            .build()
        return retrofit(BOOKS_URL, client, json).create(BooksApi::class.java)
    }

    private fun retrofit(baseUrl: String, client: OkHttpClient, json: Json): Retrofit =
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()

    private fun OkHttpClient.Builder.addDebugLogging(): OkHttpClient.Builder = apply {
        if (BuildConfig.DEBUG) {
            addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY })
        }
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindLocationRepository(impl: LocationRepositoryImpl): LocationRepository

    @Binds
    abstract fun bindBookingRepository(impl: BookingRepositoryImpl): BookingRepository

    @Binds
    abstract fun bindTripRepository(impl: TripRepositoryImpl): TripRepository
}
