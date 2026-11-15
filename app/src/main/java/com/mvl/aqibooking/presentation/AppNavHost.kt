package com.mvl.aqibooking.presentation

import androidx.compose.runtime.Composable
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mvl.aqibooking.presentation.booking.BookingScreen
import com.mvl.aqibooking.presentation.detail.LocationDetailScreen
import com.mvl.aqibooking.presentation.history.HistoryScreen
import com.mvl.aqibooking.presentation.map.MapScreen
import kotlinx.serialization.Serializable

@Serializable data object MapRoute
@Serializable data class DetailRoute(val slot: String)
@Serializable data object BookingRoute
@Serializable data object HistoryRoute

@Composable
fun AppNavHost() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = MapRoute) {
        composable<MapRoute> { entry ->
            MapScreen(
                onOpenDetail = { slot ->
                    entry.ifResumed {
                        navController.navigate(DetailRoute(slot.name))
                    }
                },
                onBook = { entry.ifResumed { navController.navigate(BookingRoute) } },
            )
        }
        composable<DetailRoute> { entry ->
            LocationDetailScreen(onDone = {
                entry.ifResumed {
                    navController.popBackStack()
                }
            })
        }
        composable<BookingRoute> { entry ->
            BookingScreen(onOpenHistory = {
                entry.ifResumed {
                    navController.navigate(HistoryRoute)
                }
            })
        }
        composable<HistoryRoute> {
            HistoryScreen()
        }
    }
}

private inline fun NavBackStackEntry.ifResumed(action: () -> Unit) {
    if (lifecycle.currentState == Lifecycle.State.RESUMED) action()
}
