package com.familymedcabinet.presentation

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.familymedcabinet.ads.AdManager
import com.familymedcabinet.analytics.AnalyticsManager
import com.familymedcabinet.domain.model.UserPreferences
import com.familymedcabinet.domain.repository.PreferencesRepository
import com.familymedcabinet.presentation.navigation.FamilyMedCabinetNavHost
import com.familymedcabinet.presentation.navigation.Screen
import com.familymedcabinet.presentation.ui.theme.FamilyMedCabinetTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject lateinit var adManager: AdManager
    @Inject lateinit var analyticsManager: AnalyticsManager
    @Inject lateinit var preferencesRepository: PreferencesRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        adManager.initialize()

        setContent {
            val prefs by preferencesRepository.getUserPreferences()
                .collectAsStateWithLifecycle(initialValue = null)

            if (prefs == null) {
                LoadingShell()
                return@setContent
            }

            FamilyMedCabinetRoot(
                prefs = prefs!!,
                adManager = adManager,
                analyticsManager = analyticsManager
            )
        }
    }
}

@Composable
private fun LoadingShell() {
    FamilyMedCabinetTheme { Box(Modifier.fillMaxSize()) }
}

@Composable
private fun FamilyMedCabinetRoot(
    prefs: UserPreferences,
    adManager: AdManager,
    analyticsManager: AnalyticsManager
) {
    LaunchedEffect(prefs.analyticsEnabled) {
        analyticsManager.setCollectionEnabled(prefs.analyticsEnabled)
    }
    LaunchedEffect(prefs.adsEnabled, prefs.personalizedAds) {
        adManager.updateAdPolicy(prefs.adsEnabled, prefs.personalizedAds)
    }

    FamilyMedCabinetTheme(
        appTheme = prefs.appTheme,
        highContrast = prefs.highContrastMode,
        fontScale = prefs.fontScale
    ) {
        val navController = rememberNavController()
        FamilyMedCabinetNavHost(
            navController = navController,
            adManager = adManager,
            analyticsManager = analyticsManager,
            preferences = prefs,
            startDestination = Screen.Home.route
        )
    }
}
