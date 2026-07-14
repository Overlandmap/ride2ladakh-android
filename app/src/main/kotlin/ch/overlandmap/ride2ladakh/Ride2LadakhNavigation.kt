package ch.overlandmap.ride2ladakh

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import ch.overlandmap.map.OverlandApp
import ch.overlandmap.map.model.TrackPack
import ch.overlandmap.map.ui.home.ItineraryScreen
import ch.overlandmap.map.ui.home.LocalPackScreen
import ch.overlandmap.map.ui.settings.LanguageScreen
import ch.overlandmap.map.ui.settings.ProfileScreen
import ch.overlandmap.map.ui.settings.SettingsScreen
import ch.overlandmap.map.ui.settings.SignInScreen
import ch.overlandmap.map.ui.settings.UnitsScreen
import ch.overlandmap.map.ui.shop.PackDetailScreen

/** The single region this app is dedicated to (the Ladakh track pack). */
private const val LADAKH_PACK_ID = "suWHrUUD6S4ZwVdGvyPt"

/**
 * The single-pack app shell: no bottom tabs. Home is the Ladakh pack itself —
 * its itineraries once it's in the local library, otherwise its shop detail to
 * buy/download. Settings (and everything under it) are reached from the pack's
 * top-bar action, and itineraries open full-screen. All screens are shared
 * from :core.
 */
@Composable
fun Ride2LadakhNavigation() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            BelowStatusBar {
                Ride2LadakhHome(
                    onOpenItinerary = navController::navigateToItinerary,
                    onOpenShopPack = { navController.navigate("pack/$it") },
                    onOpenSettings = { navController.navigate("settings") },
                )
            }
        }
        composable("pack/{packId}") { entry ->
            PackDetailScreen(
                packId = entry.arguments?.getString("packId") ?: return@composable,
                onBack = { navController.popBackStack() },
                onOpenSignIn = { navController.navigate("settings/signIn") },
            )
        }
        composable(
            "itinerary/{itineraryId}?step={step}",
            arguments = listOf(
                navArgument("step") {
                    type = NavType.IntType
                    defaultValue = 0
                },
            ),
        ) { entry ->
            ItineraryScreen(
                itineraryId = entry.arguments?.getString("itineraryId") ?: return@composable,
                onBack = { navController.popBackStack() },
                onOpenItinerary = navController::navigateToItinerary,
                onOpenPack = { navController.navigate("pack/$it") },
                initialStepId = entry.arguments?.getInt("step")?.takeIf { it > 0 },
            )
        }
        composable("settings") {
            BelowStatusBar {
                SettingsScreen(
                    onOpenSignIn = { navController.navigate("settings/signIn") },
                    onOpenProfile = { navController.navigate("settings/profile") },
                    onOpenLanguage = { navController.navigate("settings/language") },
                    onOpenUnits = { navController.navigate("settings/units") },
                )
            }
        }
        composable("settings/signIn") {
            BelowStatusBar { SignInScreen(onBack = { navController.popBackStack() }) }
        }
        composable("settings/profile") {
            BelowStatusBar {
                ProfileScreen(
                    onBack = { navController.popBackStack() },
                    onOpenSignIn = { navController.navigate("settings/signIn") },
                )
            }
        }
        composable("settings/language") {
            BelowStatusBar { LanguageScreen(onBack = { navController.popBackStack() }) }
        }
        composable("settings/units") {
            BelowStatusBar { UnitsScreen(onBack = { navController.popBackStack() }) }
        }
    }
}

/**
 * Home: the Ladakh pack's viewer if it's already in the local library,
 * otherwise its shop detail (buy + download). Switches automatically when a
 * download lands, since it observes the local library.
 */
@Composable
private fun Ride2LadakhHome(
    onOpenItinerary: (documentId: String, stepId: Int?) -> Unit,
    onOpenShopPack: (packId: String) -> Unit,
    onOpenSettings: () -> Unit,
) {
    val app = LocalContext.current.applicationContext as OverlandApp
    val packs by produceState<List<TrackPack>?>(initialValue = null, app) {
        app.libraryRepository.observeTrackPacks().collect { value = it }
    }
    val local = packs?.any { it.documentId == LADAKH_PACK_ID }

    when (local) {
        null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        true -> LocalPackScreen(
            packId = LADAKH_PACK_ID,
            // Single-screen app: there's nothing to go back to.
            onBack = {},
            onOpenItinerary = onOpenItinerary,
            onOpenShopPack = onOpenShopPack,
            onOpenSettings = onOpenSettings,
        )
        false -> PackDetailScreen(
            packId = LADAKH_PACK_ID,
            onBack = {},
            onOpenSignIn = onOpenSettings,
        )
    }
}

@Composable
private fun BelowStatusBar(content: @Composable () -> Unit) {
    Box(modifier = Modifier.fillMaxSize().statusBarsPadding()) { content() }
}

/** Opens an itinerary, optionally on one of its steps (from a markup link). */
private fun NavController.navigateToItinerary(documentId: String, stepId: Int?) {
    navigate("itinerary/$documentId" + if (stepId != null) "?step=$stepId" else "")
}
