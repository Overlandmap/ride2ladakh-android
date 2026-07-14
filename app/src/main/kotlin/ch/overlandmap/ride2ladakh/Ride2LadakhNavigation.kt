package ch.overlandmap.ride2ladakh

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import ch.overlandmap.map.ui.SingleTrackPackRoot
import ch.overlandmap.map.ui.home.ItineraryScreen
import ch.overlandmap.map.ui.settings.LanguageScreen
import ch.overlandmap.map.ui.settings.ProfileScreen
import ch.overlandmap.map.ui.settings.SettingsScreen
import ch.overlandmap.map.ui.settings.SignInScreen
import ch.overlandmap.map.ui.settings.UnitsScreen
import ch.overlandmap.map.ui.shop.PackDetailScreen

/** The single region this app is dedicated to, resolved by pack name. */
const val LADAKH_PACK_NAME = "Ladakh"

/**
 * The single-pack app shell: no bottom tabs. Home is [SingleTrackPackRoot],
 * which resolves the Ladakh pack by name and shows its local viewer (once
 * downloaded) or its shop detail (to buy/download). Itineraries open
 * full-screen; Settings is reached from the top-bar action. All screens are
 * shared from :core.
 */
@Composable
fun Ride2LadakhNavigation() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            BelowStatusBar {
                SingleTrackPackRoot(
                    trackPackName = LADAKH_PACK_NAME,
                    onOpenItinerary = navController::navigateToItinerary,
                    onOpenShopPack = { navController.navigate("pack/$it") },
                    onOpenSignIn = { navController.navigate("settings/signIn") },
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

@Composable
private fun BelowStatusBar(content: @Composable () -> Unit) {
    Box(modifier = Modifier.fillMaxSize().statusBarsPadding()) { content() }
}

/** Opens an itinerary, optionally on one of its steps (from a markup link). */
private fun NavController.navigateToItinerary(documentId: String, stepId: Int?) {
    navigate("itinerary/$documentId" + if (stepId != null) "?step=$stepId" else "")
}
