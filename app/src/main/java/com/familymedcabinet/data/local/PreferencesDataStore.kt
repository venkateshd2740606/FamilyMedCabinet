package com.familymedcabinet.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.familymedcabinet.domain.model.AppTheme
import com.familymedcabinet.domain.model.UserPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "familymedcabinet_prefs")

@Singleton
class PreferencesDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val APP_THEME = stringPreferencesKey("app_theme")
        val HAPTIC = booleanPreferencesKey("haptic")
        val SOUND = booleanPreferencesKey("sound")
        val REDUCED_MOTION = booleanPreferencesKey("reduced_motion")
        val HIGH_CONTRAST = booleanPreferencesKey("high_contrast")
        val FONT_SCALE = floatPreferencesKey("font_scale")
        val ADS = booleanPreferencesKey("ads")
        val ONBOARDING = booleanPreferencesKey("onboarding")
        val CONSENT = booleanPreferencesKey("consent")
        val ANALYTICS = booleanPreferencesKey("analytics")
        val PERSONALIZED_ADS = booleanPreferencesKey("personalized_ads")
        val LANGUAGE = stringPreferencesKey("language")
        val REMINDERS = booleanPreferencesKey("reminders")
    }

    val preferencesFlow: Flow<UserPreferences> = context.dataStore.data.map { prefs ->
        UserPreferences(
            appTheme = runCatching { AppTheme.valueOf(prefs[Keys.APP_THEME] ?: AppTheme.SYSTEM.name) }
                .getOrDefault(AppTheme.SYSTEM),
            hapticFeedback = prefs[Keys.HAPTIC] ?: true,
            soundEnabled = prefs[Keys.SOUND] ?: true,
            reducedMotion = prefs[Keys.REDUCED_MOTION] ?: false,
            highContrastMode = prefs[Keys.HIGH_CONTRAST] ?: false,
            fontScale = prefs[Keys.FONT_SCALE] ?: 1.0f,
            adsEnabled = prefs[Keys.ADS] ?: true,
            onboardingCompleted = prefs[Keys.ONBOARDING] ?: true,
            consentGiven = prefs[Keys.CONSENT] ?: true,
            analyticsEnabled = prefs[Keys.ANALYTICS] ?: true,
            personalizedAds = prefs[Keys.PERSONALIZED_ADS] ?: false,
            language = prefs[Keys.LANGUAGE] ?: "system",
            expiryNotificationsEnabled = prefs[Keys.REMINDERS] ?: true
        )
    }

    suspend fun update(transform: (UserPreferences) -> UserPreferences) {
        context.dataStore.edit { prefs ->
            val current = UserPreferences(
                appTheme = runCatching { AppTheme.valueOf(prefs[Keys.APP_THEME] ?: AppTheme.SYSTEM.name) }
                    .getOrDefault(AppTheme.SYSTEM),
                hapticFeedback = prefs[Keys.HAPTIC] ?: true,
                soundEnabled = prefs[Keys.SOUND] ?: true,
                reducedMotion = prefs[Keys.REDUCED_MOTION] ?: false,
                highContrastMode = prefs[Keys.HIGH_CONTRAST] ?: false,
                fontScale = prefs[Keys.FONT_SCALE] ?: 1.0f,
                adsEnabled = prefs[Keys.ADS] ?: true,
                onboardingCompleted = prefs[Keys.ONBOARDING] ?: true,
                consentGiven = prefs[Keys.CONSENT] ?: true,
                analyticsEnabled = prefs[Keys.ANALYTICS] ?: true,
                personalizedAds = prefs[Keys.PERSONALIZED_ADS] ?: false,
                language = prefs[Keys.LANGUAGE] ?: "system",
                expiryNotificationsEnabled = prefs[Keys.REMINDERS] ?: true
            )
            val updated = transform(current)
            prefs[Keys.APP_THEME] = updated.appTheme.name
            prefs[Keys.HAPTIC] = updated.hapticFeedback
            prefs[Keys.SOUND] = updated.soundEnabled
            prefs[Keys.REDUCED_MOTION] = updated.reducedMotion
            prefs[Keys.HIGH_CONTRAST] = updated.highContrastMode
            prefs[Keys.FONT_SCALE] = updated.fontScale
            prefs[Keys.ADS] = updated.adsEnabled
            prefs[Keys.ONBOARDING] = updated.onboardingCompleted
            prefs[Keys.CONSENT] = updated.consentGiven
            prefs[Keys.ANALYTICS] = updated.analyticsEnabled
            prefs[Keys.PERSONALIZED_ADS] = updated.personalizedAds
            prefs[Keys.LANGUAGE] = updated.language
            prefs[Keys.REMINDERS] = updated.expiryNotificationsEnabled
        }
    }
}
