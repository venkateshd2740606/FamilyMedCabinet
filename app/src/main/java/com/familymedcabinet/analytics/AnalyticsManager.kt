package com.familymedcabinet.analytics

import android.os.Bundle
import com.google.firebase.Firebase
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.analytics
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnalyticsManager @Inject constructor() {
    private val analytics: FirebaseAnalytics = Firebase.analytics

    fun setCollectionEnabled(enabled: Boolean) {
        analytics.setAnalyticsCollectionEnabled(enabled)
    }

    fun logScreenView(screenName: String) {
        analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, Bundle().apply {
            putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
        })
    }

    fun logMedicationSaved(name: String) {
        analytics.logEvent("medication_saved", Bundle().apply { putString("name", name) })
    }

    fun logDoseLogged(status: String) {
        analytics.logEvent("dose_logged", Bundle().apply { putString("status", status) })
    }

    fun logAdWatched(adType: String) {
        analytics.logEvent("ad_watched", Bundle().apply { putString("ad_type", adType) })
    }
}
