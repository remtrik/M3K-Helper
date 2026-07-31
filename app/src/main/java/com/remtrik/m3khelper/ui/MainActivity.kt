package com.remtrik.m3khelper.ui

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo.SCREEN_ORIENTATION_FULL_USER
import android.content.pm.ActivityInfo.SCREEN_ORIENTATION_USER_PORTRAIT
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.animations.NavHostAnimatedDestinationStyle
import com.ramcosta.composedestinations.generated.NavGraphs
import com.ramcosta.composedestinations.generated.destinations.LinksScreenDestination
import com.ramcosta.composedestinations.generated.destinations.SettingsScreenDestination
import com.ramcosta.composedestinations.generated.destinations.ThemeEngineScreenDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.utils.isRouteOnBackStackAsState
import com.ramcosta.composedestinations.utils.rememberDestinationsNavigator
import com.remtrik.m3khelper.BuildConfig
import com.remtrik.m3khelper.prefs
import com.remtrik.m3khelper.ui.component.NoRoot
import com.remtrik.m3khelper.ui.component.UnknownDevice
import com.remtrik.m3khelper.ui.component.UpdateDialog
import com.remtrik.m3khelper.ui.theme.M3KHelperTheme
import com.remtrik.m3khelper.util.collapseTransition
import com.remtrik.m3khelper.util.expandTransition
import com.remtrik.m3khelper.util.fadeEnterTransition
import com.remtrik.m3khelper.util.fadeExitTransition
import com.remtrik.m3khelper.util.funcs.Download.checkNewVersion
import com.remtrik.m3khelper.util.funcs.LatestVersionInfo
import com.remtrik.m3khelper.util.slideFromRightEnterTransition
import com.remtrik.m3khelper.util.slideToLeftExitTransition
import com.remtrik.m3khelper.util.slideToRightExitTransition
import com.remtrik.m3khelper.util.variables.FontSize
import com.remtrik.m3khelper.util.variables.LineHeight
import com.remtrik.m3khelper.util.variables.PaddingValue
import com.remtrik.m3khelper.util.variables.device
import com.remtrik.m3khelper.util.variables.sdp
import com.remtrik.m3khelper.util.variables.showWarningCard
import com.remtrik.m3khelper.util.variables.ssp
import com.topjohnwu.superuser.Shell

class MainActivity : ComponentActivity() {

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        window.isNavigationBarContrastEnforced = false

        requestedOrientation = resolveOrientation()

        setContent {
            M3KHelperTheme {
                InitDimens()
                if (Shell.isAppGrantedRoot() == true) {
                    M3KRootContent()
                } else {
                    NoRoot()
                }
            }
        }
    }

    private fun resolveOrientation(): Int {
        val forceRotation = prefs.getBoolean("force_rotation", false)
        val isNabu = Build.DEVICE == "nabu"
        val isDebugEmulator = BuildConfig.DEBUG && Build.DEVICE == "emu64xa"

        return if (isNabu || isDebugEmulator || forceRotation) {
            SCREEN_ORIENTATION_FULL_USER
        } else {
            SCREEN_ORIENTATION_USER_PORTRAIT
        }
    }
}

@Composable
internal fun InitDimens() {
    LineHeight = 20.ssp()
    FontSize = 15.ssp()
    PaddingValue = 10.sdp()
}

@Composable
internal fun M3KRootContent() {
    val navController = rememberNavController()
    val navigator = navController.rememberDestinationsNavigator()
    val orientation = LocalConfiguration.current.orientation

    var latestVersion by remember { mutableStateOf(LatestVersionInfo()) }
    LaunchedEffect(Unit) {
        if (prefs.getBoolean("check_update", true)) {
            latestVersion = checkNewVersion()
        }
    }

    val hasNewVersion = latestVersion.versionCode > BuildConfig.VERSION_CODE

    val bottomBarRoutes = remember {
        Destinations.entries.map { it.route.route }.toSet()
    }

    Scaffold(
        bottomBar = {
            AnimatedVisibility(
                visible = orientation != Configuration.ORIENTATION_LANDSCAPE,
                enter = slideInVertically { it },
                exit = slideOutVertically { it }
            ) {
                BottomNavigationBar(navController, navigator)
            }
        },
    ) { innerPadding ->
        Row {
            AnimatedVisibility(
                visible = orientation == Configuration.ORIENTATION_LANDSCAPE,
                enter = slideInHorizontally { -it },
                exit = slideOutHorizontally { -it }
            ) {
                LeftNavigationBar(navController, navigator)
            }

            Box(
                modifier = Modifier
                    .padding(bottom = innerPadding.calculateBottomPadding())
                    .fillMaxSize(),
            ) {
                DestinationsNavHost(
                    navGraph = NavGraphs.root,
                    navController = navController,
                    defaultTransitions = object : NavHostAnimatedDestinationStyle() {
                        override val enterTransition:
                                AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition =
                            {
                                if (targetState.destination.route !in bottomBarRoutes)
                                    slideFromRightEnterTransition
                                else fadeEnterTransition

                            }
                        override val exitTransition:
                                AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition =
                            {
                                if (targetState.destination.route !in bottomBarRoutes)
                                    if (targetState.destination.route == SettingsScreenDestination.route ||
                                        targetState.destination.route == ThemeEngineScreenDestination.route
                                    ) slideToLeftExitTransition
                                    else slideToRightExitTransition
                                else fadeExitTransition
                            }
                    }
                )
                val isWarningVisible by showWarningCard.collectAsStateWithLifecycle()
                if (isWarningVisible) {
                    UnknownDevice()
                }
            }
            AnimatedVisibility(
                visible = hasNewVersion,
                enter = expandTransition,
                exit = collapseTransition
            ) {
                UpdateDialog(latestVersion)
            }
        }
    }
}

@Composable
private fun getVisibleDestinations(): List<Destinations> {
    val currentDeviceCard by device.currentDeviceCard.collectAsStateWithLifecycle()
    return remember(currentDeviceCard) {
        Destinations.entries.filter { destination ->
            !(currentDeviceCard.noLinks && destination.route == LinksScreenDestination)
        }
    }
}

@Composable
private fun BottomNavigationBar(
    navController: NavHostController,
    navigator: DestinationsNavigator
) {
    NavigationBar(
        tonalElevation = 12.dp,
        windowInsets = WindowInsets.systemBars
            .union(WindowInsets.displayCutout)
            .only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom),
        modifier = Modifier.height(120.sdp()),
    ) {
        getVisibleDestinations()
            .filterNot { it.landscapeOnly }
            .forEach { destination ->
                val isCurrentDestOnBackStack by navController.isRouteOnBackStackAsState(
                    destination.route
                )
                NavigationBarItem(
                    selected = isCurrentDestOnBackStack,
                    onClick = {
                        navigateTo(destination, isCurrentDestOnBackStack, navigator)
                    },
                    icon = {
                        NavigationIcon(destination, isCurrentDestOnBackStack)
                    },
                    label = {
                        Text(
                            text = stringResource(destination.label),
                            fontSize = 10.ssp(),
                        )
                    },
                    alwaysShowLabel = false
                )
            }
    }
}

@Composable
private fun LeftNavigationBar(
    navController: NavHostController,
    navigator: DestinationsNavigator,
) {
    NavigationRail(
        modifier = Modifier.width(110.sdp()),
        windowInsets = WindowInsets.systemBars
            .only(WindowInsetsSides.Bottom + WindowInsetsSides.Top)
    ) {
        getVisibleDestinations().forEach { destination ->
            if (destination.route == SettingsScreenDestination) Spacer(
                Modifier.weight(
                    1f
                )
            )
            val isCurrentDestOnBackStack by navController.isRouteOnBackStackAsState(
                destination.route
            )
            NavigationRailItem(
                selected = isCurrentDestOnBackStack,
                onClick = {
                    navigateTo(destination, isCurrentDestOnBackStack, navigator)
                },
                icon = {
                    NavigationIcon(destination, isCurrentDestOnBackStack)
                },
                label = {
                    Text(
                        text = stringResource(destination.label),
                        fontSize = 10.ssp(),
                    )
                },
                alwaysShowLabel = false
            )
        }
    }
}

@Composable
private fun NavigationIcon(destination: Destinations, selected: Boolean) {
    val icon = if (selected) destination.iconSelected else destination.iconNotSelected

    Icon(
        imageVector = icon,
        contentDescription = stringResource(destination.label),
        modifier = Modifier.size(20.sdp())
    )
}

private fun navigateTo(
    destination: Destinations,
    isSelected: Boolean,
    navigator: DestinationsNavigator
) {
    if (isSelected) {
        navigator.popBackStack(destination.route, false)
    }
    navigator.navigate(destination.route) {
        popUpTo(NavGraphs.root) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}