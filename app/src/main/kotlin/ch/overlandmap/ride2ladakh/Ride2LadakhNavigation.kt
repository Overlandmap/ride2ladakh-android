package ch.overlandmap.ride2ladakh

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import ch.overlandmap.map.R
import ch.overlandmap.map.ui.SingleTrackPackRoot
import ch.overlandmap.map.ui.home.ItineraryScreen
import ch.overlandmap.map.ui.settings.DownloadsScreen
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
            // No status-bar wrapper: the split's map draws full-bleed to the
            // top (under the status bar). AboveNavBar keeps the split's bottom
            // clear of the system nav bar (there's no bottom tab bar here to do
            // it). The floating Settings button carries its own status inset.
            AboveNavBar {
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
            val packId = entry.arguments?.getString("packId") ?: return@composable
            AboveNavBar {
                PackDetailScreen(
                    packId = packId,
                    onBack = { navController.popBackStack() },
                    onOpenSignIn = { navController.navigate("settings/signIn") },
                )
            }
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
            val itineraryId = entry.arguments?.getString("itineraryId") ?: return@composable
            AboveNavBar {
                ItineraryScreen(
                    itineraryId = itineraryId,
                    onBack = { navController.popBackStack() },
                    onOpenItinerary = navController::navigateToItinerary,
                    onOpenPack = { navController.navigate("pack/$it") },
                    initialStepId = entry.arguments?.getInt("step")?.takeIf { it > 0 },
                )
            }
        }
        composable("settings") {
            // Single-pack app: Settings is pushed over the root, so it needs a
            // back button (the multi-pack app reaches it via a bottom tab and
            // needs none). The Scaffold's top bar carries the status-bar inset,
            // so this route isn't wrapped in BelowStatusBar.
            SettingsWithBack(onBack = { navController.popBackStack() }) {
                SettingsScreen(
                    onOpenSignIn = { navController.navigate("settings/signIn") },
                    onOpenProfile = { navController.navigate("settings/profile") },
                    onOpenLanguage = { navController.navigate("settings/language") },
                    onOpenUnits = { navController.navigate("settings/units") },
                    onOpenDownloads = { navController.navigate("settings/downloads") },
                )
            }
        }
        composable("settings/downloads") {
            DownloadsScreen(onBack = { navController.popBackStack() })
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

/**
 * Keeps a full-bleed split screen clear of the system navigation bar at the
 * bottom while leaving the top free to extend under the status bar (the map).
 */
@Composable
private fun AboveNavBar(content: @Composable () -> Unit) {
    Box(modifier = Modifier.fillMaxSize().navigationBarsPadding()) { content() }
}

/** Wraps Settings in a top bar with a back button (single-pack app). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsWithBack(onBack: () -> Unit, content: @Composable () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.tab_settings)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
            )
        },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) { content() }
    }
}

/** Opens an itinerary, optionally on one of its steps (from a markup link). */
private fun NavController.navigateToItinerary(documentId: String, stepId: Int?) {
    navigate("itinerary/$documentId" + if (stepId != null) "?step=$stepId" else "")
}
