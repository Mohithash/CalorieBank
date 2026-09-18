@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package com.mohithash.caloriebank.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.mohithash.caloriebank.ui.screens.HistoryScreen
import com.mohithash.caloriebank.ui.screens.HomeScreen
import com.mohithash.caloriebank.ui.screens.LogScreen
import com.mohithash.caloriebank.ui.screens.OnboardingScreen
import com.mohithash.caloriebank.ui.screens.SettingsScreen
import com.mohithash.caloriebank.ui.screens.WaterScreen

enum class Tab(val route: String, val label: String, val icon: ImageVector, val selected: ImageVector) {
    HOME("home", "Bank", Icons.Outlined.AccountBalance, Icons.Filled.AccountBalance),
    LOG("log", "Log", Icons.Outlined.Restaurant, Icons.Filled.Restaurant),
    WATER("water", "Water", Icons.Outlined.WaterDrop, Icons.Filled.WaterDrop),
    HISTORY("history", "History", Icons.Outlined.History, Icons.Filled.History),
}

const val ROUTE_SETTINGS = "settings"

@Composable
fun CalorieBankNav(vm: AppViewModel) {
    val profile by vm.profile.collectAsState()
    LifecycleResumeEffect(Unit) { vm.onResume(); onPauseOrDispose { } }

    if (!profile.onboarded) {
        OnboardingScreen(onOpen = vm::openAccount)
        return
    }

    val nav = rememberNavController()
    val backStack by nav.currentBackStackEntryAsState()
    val current = backStack?.destination
    val showBar = Tab.entries.any { t -> current?.hierarchy?.any { it.route == t.route } == true }

    Scaffold(
        bottomBar = {
            if (showBar) NavigationBar {
                Tab.entries.forEach { tab ->
                    val selected = current?.hierarchy?.any { it.route == tab.route } == true
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            nav.navigate(tab.route) {
                                popUpTo(nav.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true; restoreState = true
                            }
                        },
                        icon = { Icon(if (selected) tab.selected else tab.icon, tab.label) },
                        label = { Text(tab.label) },
                    )
                }
            }
        }
    ) { pad ->
        NavHost(nav, startDestination = Tab.HOME.route, modifier = Modifier.padding(bottom = pad.calculateBottomPadding())) {
            composable(Tab.HOME.route) {
                HomeScreen(vm, onLog = { nav.navigate(Tab.LOG.route) }, onWater = { nav.navigate(Tab.WATER.route) },
                    onSettings = { nav.navigate(ROUTE_SETTINGS) })
            }
            composable(Tab.LOG.route) { LogScreen(vm, onSettings = { nav.navigate(ROUTE_SETTINGS) }) }
            composable(Tab.WATER.route) { WaterScreen(vm) }
            composable(Tab.HISTORY.route) { HistoryScreen(vm) }
            composable(ROUTE_SETTINGS) { SettingsScreen(vm, onBack = { nav.popBackStack() }) }
        }
    }
}
