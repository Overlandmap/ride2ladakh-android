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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import ch.overlandmap.map.OverlandApp
import ch.overlandmap.map.R
import ch.overlandmap.map.ui.RestoreAndPersistLastRoute
import ch.overlandmap.map.ui.SingleTrackPackRoot
import ch.overlandmap.map.ui.home.ItineraryScreen
import ch.overlandmap.map.ui.home.SidebarScreen
import ch.overlandmap.map.ui.navigate.NavigationScreen
import ch.overlandmap.map.ui.help.HelpScreen
import ch.overlandmap.map.ui.help.HelpSectionScreen
import ch.overlandmap.map.ui.legal.LegalDoc
import ch.overlandmap.map.ui.legal.LegalDocScreen
import ch.overlandmap.map.ui.onboarding.OnboardingScreen
import ch.overlandmap.map.ui.settings.DownloadsScreen
import ch.overlandmap.map.ui.settings.LanguageScreen
import ch.overlandmap.map.ui.settings.AboutScreen
import ch.overlandmap.map.ui.settings.ProfileScreen
import ch.overlandmap.map.ui.settings.SettingsScreen
import ch.overlandmap.map.ui.settings.SignInScreen
import ch.overlandmap.map.ui.settings.TextSizeScreen
import ch.overlandmap.map.ui.settings.DebugScreen
import ch.overlandmap.map.ui.settings.LatestCheckInsScreen
import ch.overlandmap.map.ui.settings.UnitsScreen
import ch.overlandmap.map.ui.shop.PackDetailScreen
import ch.overlandmap.map.ui.social.LocalOpenSignIn

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
    val app = LocalContext.current.applicationContext as OverlandApp
    val showOnboarding = remember { !app.userPreferences.onboardingShownNow() }
    LaunchedEffect(Unit) {
        if (showOnboarding) navController.navigate("onboarding")
    }

    CompositionLocalProvider(LocalOpenSignIn provides { navController.navigate("settings/signIn") }) {
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
                    onOpenSidebar = { navController.navigate("sidebar/$it") },
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
                    onOpenSidebar = { navController.navigate("sidebar/$it") },
                    // The single-pack app has no search destination.
                    onOpenSearch = {},
                    onOpenSettings = { navController.navigate("settings") },
                    onOpenNavigation = { navController.navigate("navigation") { launchSingleTop = true } },
                    initialStepId = entry.arguments?.getInt("step")?.takeIf { it > 0 },
                )
            }
        }
        composable("navigation") {
            NavigationScreen(onStopped = { navController.popBackStack() })
        }
        composable("sidebar/{sidebarId}") { entry ->
            val sidebarId = entry.arguments?.getString("sidebarId") ?: return@composable
            SidebarScreen(
                sidebarId = sidebarId,
                onBack = { navController.popBackStack() },
                onOpenItinerary = navController::navigateToItinerary,
                onOpenSidebar = { navController.navigate("sidebar/$it") },
                onOpenShopPack = { navController.navigate("pack/$it") },
            )
        }
        composable("settings") {
            // Single-pack app: Settings is pushed over the root, so it needs a
            // back button (the multi-pack app reaches it via a bottom tab and
            // needs none). The Scaffold's top bar carries the status-bar inset,
            // so this route isn't wrapped in BelowStatusBar.
            SettingsWithBack(onBack = { navController.popBackStack() }) {
                SettingsScreen(
                    onOpenSignIn = { navController.navigate("settings/signIn") },
                    onOpenAbout = { navController.navigate("settings/about") },
                    onOpenProfile = { navController.navigate("settings/profile") },
                    onOpenLanguage = { navController.navigate("settings/language") },
                    onOpenUnits = { navController.navigate("settings/units") },
                    onOpenTextSize = { navController.navigate("settings/textSize") },
                    onOpenDownloads = { navController.navigate("settings/downloads") },
                    onOpenHelp = { navController.navigate("settings/help") },
                    onOpenPrivacy = { navController.navigate("settings/privacy") },
                    onOpenTerms = { navController.navigate("settings/terms") },
                    onOpenDebug = { navController.navigate("settings/debug") },
                )
            }
        }
        composable("settings/downloads") {
            DownloadsScreen(onBack = { navController.popBackStack() })
        }
        composable("settings/about") {
            BelowStatusBar { AboutScreen(onBack = { navController.popBackStack() }) }
        }
        composable("settings/textSize") {
            BelowStatusBar { TextSizeScreen(onBack = { navController.popBackStack() }) }
        }
        composable("settings/help") {
            BelowStatusBar {
                HelpScreen(
                    onOpenSection = { navController.navigate("settings/help/$it") },
                    onClose = { navController.popBackStack() },
                )
            }
        }
        composable("settings/help/{sectionId}") { entry ->
            val sectionId = entry.arguments?.getString("sectionId") ?: return@composable
            BelowStatusBar {
                HelpSectionScreen(
                    sectionId = sectionId,
                    onBack = { navController.popBackStack() },
                    onClose = { navController.popBackStack() },
                )
            }
        }
        composable("settings/privacy") {
            BelowStatusBar {
                LegalDocScreen(doc = LegalDoc.PRIVACY, onBack = { navController.popBackStack() })
            }
        }
        composable("settings/terms") {
            BelowStatusBar {
                LegalDocScreen(doc = LegalDoc.TERMS, onBack = { navController.popBackStack() })
            }
        }
        composable("settings/debug") {
            BelowStatusBar {
                DebugScreen(
                    onBack = { navController.popBackStack() },
                    onOpenLatestCheckIns = { navController.navigate("settings/debug/checkIns") },
                )
            }
        }
        composable("settings/debug/checkIns") {
            BelowStatusBar {
                LatestCheckInsScreen(
                    onBack = { navController.popBackStack() },
                    onOpenItinerary = { docId, stepId ->
                        navController.navigateToItinerary(docId, stepId)
                    },
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
        // Forced first-launch onboarding: accept the policies before entering
        // the app. In single-pack mode, "try a sample" downloads the free
        // itinerary and opens the pack viewer directly.
        composable("onboarding") {
            BelowStatusBar {
                OnboardingScreen(
                    appName = "Ride 2 Ladakh",
                    logoRes = ch.overlandmap.ride2ladakh.R.drawable.app_logo,
                    onViewPrivacy = { navController.navigate("settings/privacy") },
                    onViewTerms = { navController.navigate("settings/terms") },
                    onAcceptTutorial = {
                        navController.navigate("settings/help") {
                            popUpTo("onboarding") { inclusive = true }
                        }
                    },
                    onAcceptSample = {
                        navController.navigate("home") {
                            popUpTo("onboarding") { inclusive = true }
                        }
                    },
                )
            }
        }
    }
    }
    // Restore the last screen after the app is killed, and keep it saved.
    RestoreAndPersistLastRoute(navController)
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
