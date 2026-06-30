package com.familymedcabinet.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.familymedcabinet.domain.model.AppTheme
import com.familymedcabinet.domain.model.FamilyRelation
import com.familymedcabinet.domain.model.Medicine
import com.familymedcabinet.domain.model.Profile
import com.familymedcabinet.domain.model.UserPreferences
import com.familymedcabinet.domain.repository.MedicineRepository
import com.familymedcabinet.domain.repository.PreferencesRepository
import com.familymedcabinet.domain.repository.ProfileRepository
import com.familymedcabinet.engine.CabinetEngine
import com.familymedcabinet.worker.ExpiryWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import android.content.Context
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class CabinetViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val medicineRepository: MedicineRepository,
    private val engine: CabinetEngine,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _selectedProfileId = MutableStateFlow<Long?>(null)
    val selectedProfileId: StateFlow<Long?> = _selectedProfileId.asStateFlow()

    val profiles: StateFlow<List<Profile>> = profileRepository.observeProfiles()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val medicines: StateFlow<List<Medicine>> = combine(
        profiles,
        _selectedProfileId
    ) { profileList, selectedId ->
        selectedId ?: profileList.firstOrNull()?.id
    }.flatMapLatest { profileId ->
        if (profileId == null) flowOf(emptyList())
        else medicineRepository.observeMedicinesForProfile(profileId)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val expiringSoon: StateFlow<List<Medicine>> = medicineRepository
        .observeExpiringBefore(engine.expiryMillisInDays(30))
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _editingMedicine = MutableStateFlow<Medicine?>(null)
    val editingMedicine: StateFlow<Medicine?> = _editingMedicine.asStateFlow()

    init {
        viewModelScope.launch {
            profiles.collect { list ->
                if (_selectedProfileId.value == null && list.isNotEmpty()) {
                    _selectedProfileId.value = list.first().id
                }
            }
        }
    }

    fun selectProfile(profileId: Long) {
        _selectedProfileId.value = profileId
    }

    fun saveProfile(name: String, relation: FamilyRelation, age: Int?) {
        viewModelScope.launch {
            val id = profileRepository.saveProfile(Profile(name = name.trim(), relation = relation, age = age))
            _selectedProfileId.value = id
        }
    }

    fun loadMedicine(id: Long) {
        viewModelScope.launch {
            _editingMedicine.value = medicineRepository.getMedicine(id)
        }
    }

    fun startNewMedicine(profileId: Long) {
        _editingMedicine.value = Medicine(
            profileId = profileId,
            name = "",
            dosage = "",
            quantityRemaining = 0,
            expiryDateMillis = engine.expiryMillisInDays(90),
            purpose = ""
        )
    }

    fun saveMedicine(
        profileId: Long,
        name: String,
        dosage: String,
        quantity: Int,
        expiryDateInput: String,
        purpose: String,
        refillReminder: Boolean
    ) {
        viewModelScope.launch {
            val expiry = engine.parseExpiryDate(expiryDateInput) ?: engine.expiryMillisInDays(90)
            val current = _editingMedicine.value
            val med = (current ?: Medicine(
                profileId = profileId,
                name = name,
                dosage = dosage,
                quantityRemaining = quantity,
                expiryDateMillis = expiry,
                purpose = purpose
            )).copy(
                profileId = profileId,
                name = name.trim(),
                dosage = dosage.trim(),
                quantityRemaining = quantity.coerceAtLeast(0),
                expiryDateMillis = expiry,
                purpose = purpose.trim(),
                refillReminder = refillReminder,
                finished = false
            )
            medicineRepository.saveMedicine(med)
            ExpiryWorker.schedule(context)
            _editingMedicine.value = null
        }
    }

    fun markFinished(id: Long) {
        viewModelScope.launch {
            val med = medicineRepository.getMedicine(id) ?: return@launch
            medicineRepository.saveMedicine(med.copy(finished = true))
        }
    }

    fun deleteMedicine(id: Long) {
        viewModelScope.launch { medicineRepository.deleteMedicine(id) }
    }

    fun formatExpiry(millis: Long) = engine.formatExpiry(millis)

    fun daysUntilExpiry(medicine: Medicine) = engine.daysUntilExpiry(medicine)

    fun isExpiringSoon(medicine: Medicine) = engine.isExpiringSoon(medicine)

    fun isExpired(medicine: Medicine) = engine.isExpired(medicine)
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {
    val prefs: StateFlow<UserPreferences> = preferencesRepository.getUserPreferences()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserPreferences())

    fun setTheme(theme: AppTheme) = update { it.copy(appTheme = theme) }
    fun setExpiryNotifications(enabled: Boolean) {
        update { it.copy(expiryNotificationsEnabled = enabled) }
        if (enabled) ExpiryWorker.schedule(context) else ExpiryWorker.cancel(context)
    }
    fun setAds(enabled: Boolean) = update { it.copy(adsEnabled = enabled) }
    fun setAnalytics(enabled: Boolean) = update { it.copy(analyticsEnabled = enabled) }

    private fun update(transform: (UserPreferences) -> UserPreferences) {
        viewModelScope.launch { preferencesRepository.updatePreferences(transform) }
    }
}
