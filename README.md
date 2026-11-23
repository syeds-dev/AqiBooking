# AQI Booking

https://github.com/user-attachments/assets/1faf89a9-54ef-451c-adbd-d323a1cf9fd2

Pick two places (A and B) on a Google Map, see the air quality at the marker, book the trip
(`POST /books`) and see this month's bookings (`GET /books?year=&month=`).

Built with a single Activity, Jetpack Compose, AAC ViewModel, Hilt, Kotlin Coroutines/Flow, Retrofit and the Google Maps SDK.

---

## 1. Setup

**Requirements:** Android Studio (Ladybug or newer), JDK 17+, Android SDK 35, a device or emulator with Google Play services.

1. Open the project folder in Android Studio. The Gradle wrapper is included.
2. Add your keys to `local.properties` in the project root. This file is git-ignored; see `local.properties.example`:
   ```properties
   MAPS_API_KEY=your_google_maps_android_key
   AQICN_TOKEN=your_aqicn_token      # optional: leave empty to use the built-in mock
   USE_MOCK_BOOKS_API=true           # there is no real /books server
   ```
3. Run the `app` configuration and allow location access.

From the command line:
```bash
./gradlew :app:assembleDebug        # build
```

**Keys:** Google Maps comes from Google Cloud Console (enable *Maps SDK for Android*). An air quality token is free at https://aqicn.org/data-platform/token/. Reverse geocoding (BigDataCloud) needs no key.

### Security
- No key is in source code. `app/build.gradle.kts` reads `local.properties`: `MAPS_API_KEY` goes into the manifest as a placeholder, and `AQICN_TOKEN` and `USE_MOCK_BOOKS_API` become `BuildConfig` fields.
- The AQI token is added by an OkHttp interceptor in `AppModule`. That client has no HTTP logging, so the token is never printed to logcat.

---

## 2. Project structure (19 files)

```
com.mvl.aqibooking
├── MainActivity.kt              Application + the single Activity
├── domain/                      Pure Kotlin: business rules, no Android/Retrofit
│   ├── Models.kt                Coordinate, Location, TripSelection, Booking, BookingHistory
│   ├── Repositories.kt          LocationRepository, BookingRepository, TripRepository (interfaces)
│   └── UseCases.kt              SelectLocation, UpdateNickname, BookTrip, GetMonthlyHistory, ...
├── data/                        Implements the domain interfaces
│   ├── Apis.kt                  Retrofit interfaces + JSON models
│   ├── RepositoryImpls.kt       Repository implementations (+ address-name rule)
│   ├── MockApiInterceptor.kt    Fake backend, runs inside OkHttp
│   └── AppModule.kt             Hilt: Retrofit clients, mock switch, repository bindings
└── presentation/                Compose UI, one ViewModel per screen
    ├── AppNavHost.kt            Routes and navigation
    ├── Theme.kt, Components.kt  Colours, sizes, shared UI pieces
    ├── map/                     Screen 1: MapScreen + MapViewModel
    ├── detail/                  Screen 2: LocationDetailScreen + LocationDetailViewModel
    ├── booking/                 Screen 3: BookingScreen + BookingViewModel
    └── history/                 Screen 4: HistoryScreen + HistoryViewModel
```

**How a request flows:** Screen → ViewModel → UseCase → Repository interface → RepositoryImpl → Retrofit → OkHttp (→ MockApiInterceptor when mocking).

- **Domain** holds every rule: the Set A → Set B → Book order (`TripSelection.nextSlot`), the 20-character nickname limit, resetting after a booking, and computing totals.
- **ViewModels** call use cases and expose one `StateFlow<UiState>`. Screens only display that state.
- **A/B selection** is shared through `TripRepository` (a singleton), not through a shared ViewModel.

---

## 3. Mock design
There is no real `/books` server, so `MockApiInterceptor` acts as one. It is an OkHttp interceptor, which means it sits **below** Retrofit: a mocked call runs exactly the same API interface, repository and use case code as a real one.

- `POST /books`: reads the request body and returns A and B with an `id` and a price based on the distance between them. The booking is stored, so the next `GET` returns it.
- `GET /books?year=&month=`: returns that month's bookings plus a few older records for that month. It returns 400 if the query is missing.
- Air quality (only when no `AQICN_TOKEN` is set): the value depends on the coordinates, so moving the map changes it.

**Switching to a real server:** set `USE_MOCK_BOOKS_API=false`. `AppModule` simply doesn't add the interceptor, and no other code changes.

---

## 4. Async and state
- **AQI while moving the map:** camera positions are `debounce`d (300 ms), and `collectLatest` cancels the previous request when the user moves again. An old, slow response can never overwrite the current value.
- **Set A / Set B:** the address and AQI are fetched in parallel (`async`). The button shows a spinner and ignores taps while loading.
- **Loading / success / error:** Screens 3 and 4 use a sealed `UiState` with a Retry button on error. Screen 1 shows the AQI loading spinner, N/A on error, and a snackbar if Set A/B fails.
- **Navigation:** callbacks only run while the screen is RESUMED, so a fast double tap can't navigate twice.

---

## 5. Screens
1. **Map:** requests location permission and centres on the user. AQI is shown at the top right. The V button goes Set A → Set B → Book. Tapping a filled label opens Screen 2.
2. **Location detail:** shows the address and AQI, with an optional nickname (max 20 characters, can be blank). The Map label then shows the nickname.
3. **Booking:** sends `POST /books` with A and B and shows the response (A, B, price). Back returns to the Map in its initial state.
4. **History:** sends `GET /books` for the current month and shows Total Count, Total Price and the list.

The assignment's optional features (cached locations screen, restoring a trip from history) are intentionally left out to keep the code small.

## 6. Notes
- The Figma shows "V" on the Screen 2 and 3 buttons. They are labelled **Save** and **View history** for clarity.
- Nicknames are not part of the `/books` API, so the request body matches the assignment exactly. They are attached again to the response for display on Screen 3.
