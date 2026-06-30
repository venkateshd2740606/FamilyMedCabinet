package com.familymedcabinet.domain.repository

import com.familymedcabinet.domain.model.Medicine
import com.familymedcabinet.domain.model.Profile
import com.familymedcabinet.domain.model.UserPreferences
import kotlinx.coroutines.flow.Flow

interface ProfileRepository {
    fun observeProfiles(): Flow<List<Profile>>
    suspend fun getProfile(id: Long): Profile?
    suspend fun saveProfile(profile: Profile): Long
    suspend fun deleteProfile(id: Long)
}

interface MedicineRepository {
    fun observeMedicinesForProfile(profileId: Long): Flow<List<Medicine>>
    fun observeExpiringBefore(beforeMillis: Long): Flow<List<Medicine>>
    suspend fun getMedicine(id: Long): Medicine?
    suspend fun saveMedicine(medicine: Medicine): Long
    suspend fun deleteMedicine(id: Long)
}

interface PreferencesRepository {
    fun getUserPreferences(): Flow<UserPreferences>
    suspend fun updatePreferences(transform: (UserPreferences) -> UserPreferences)
}
