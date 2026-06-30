package com.familymedcabinet.domain.model

enum class AppTheme(val displayName: String) {
    SYSTEM("System"), LIGHT("Light"), DARK("Dark")
}

enum class FamilyRelation(val displayName: String) {
    SELF("Self"),
    SPOUSE("Spouse"),
    CHILD("Child"),
    PARENT("Parent"),
    OTHER("Other")
}

data class UserPreferences(
    val appTheme: AppTheme = AppTheme.SYSTEM,
    val hapticFeedback: Boolean = true,
    val soundEnabled: Boolean = true,
    val reducedMotion: Boolean = false,
    val highContrastMode: Boolean = false,
    val fontScale: Float = 1.0f,
    val adsEnabled: Boolean = true,
    val onboardingCompleted: Boolean = false,
    val consentGiven: Boolean = false,
    val analyticsEnabled: Boolean = true,
    val personalizedAds: Boolean = false,
    val language: String = "system",
    val expiryNotificationsEnabled: Boolean = true
)

data class Profile(
    val id: Long = 0,
    val name: String,
    val relation: FamilyRelation,
    val age: Int? = null
)

data class Medicine(
    val id: Long = 0,
    val profileId: Long,
    val name: String,
    val dosage: String,
    val quantityRemaining: Int,
    val expiryDateMillis: Long,
    val purpose: String,
    val refillReminder: Boolean = false,
    val finished: Boolean = false
) {
    val isLowStock: Boolean get() = !finished && quantityRemaining <= 5
}
