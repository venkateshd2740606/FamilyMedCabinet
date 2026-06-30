package com.familymedcabinet.presentation.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.familymedcabinet.ads.AdManager
import com.familymedcabinet.analytics.AnalyticsManager
import com.familymedcabinet.domain.model.UserPreferences
import com.familymedcabinet.presentation.ui.screens.cabinet.AddEditMedicineScreen
import com.familymedcabinet.presentation.ui.screens.cabinet.CabinetHomeScreen
import com.familymedcabinet.presentation.ui.screens.cabinet.MedicineDetailScreen
import com.familymedcabinet.presentation.ui.screens.settings.SettingsScreen

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object AddMedicine : Screen("add_medicine/{profileId}") {
        fun create(profileId: Long) = "add_medicine/$profileId"
    }
    data object EditMedicine : Screen("edit_medicine/{id}") {
        fun create(id: Long) = "edit_medicine/$id"
    }
    data object MedicineDetail : Screen("medicine/{id}") {
        fun create(id: Long) = "medicine/$id"
    }
    data object Settings : Screen("settings")
}

@Composable
fun FamilyMedCabinetNavHost(
    navController: NavHostController,
    adManager: AdManager,
    analyticsManager: AnalyticsManager,
    preferences: UserPreferences,
    startDestination: String = Screen.Home.route
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val bottomRoutes = setOf(Screen.Home.route, Screen.Settings.route)

    Scaffold(
        bottomBar = {
            if (currentRoute in bottomRoutes) {
                NavigationBar {
                    NavigationBarItem(
                        selected = currentRoute == Screen.Home.route,
                        onClick = { navController.navigateToTab(Screen.Home.route) },
                        icon = { Icon(Icons.Default.Home, null) },
                        label = { Text("Cabinet") }
                    )
                    NavigationBarItem(
                        selected = currentRoute == Screen.Settings.route,
                        onClick = { navController.navigateToTab(Screen.Settings.route) },
                        icon = { Icon(Icons.Default.Settings, null) },
                        label = { Text("Settings") }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(padding)
        ) {
            composable(Screen.Home.route) {
                CabinetHomeScreen(
                    onAddMedicine = { profileId -> navController.navigate(Screen.AddMedicine.create(profileId)) },
                    onMedicineDetail = { id -> navController.navigate(Screen.MedicineDetail.create(id)) },
                    adManager = adManager,
                    adsEnabled = preferences.adsEnabled
                )
            }
            composable(
                Screen.AddMedicine.route,
                arguments = listOf(navArgument("profileId") { type = NavType.LongType })
            ) { entry ->
                val profileId = entry.arguments?.getLong("profileId") ?: return@composable
                AddEditMedicineScreen(profileId = profileId, medicineId = null, onBack = { navController.popBackStack() })
            }
            composable(
                Screen.EditMedicine.route,
                arguments = listOf(navArgument("id") { type = NavType.LongType })
            ) { entry ->
                val id = entry.arguments?.getLong("id") ?: return@composable
                AddEditMedicineScreen(
                    profileId = 0,
                    medicineId = id,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(
                Screen.MedicineDetail.route,
                arguments = listOf(navArgument("id") { type = NavType.LongType })
            ) { entry ->
                val id = entry.arguments?.getLong("id") ?: return@composable
                MedicineDetailScreen(
                    medicineId = id,
                    onBack = { navController.popBackStack() },
                    onEdit = { medId -> navController.navigate(Screen.EditMedicine.create(medId)) }
                )
            }
            composable(Screen.Settings.route) { SettingsScreen() }
        }
    }
}

private fun NavHostController.navigateToTab(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
