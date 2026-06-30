package com.familymedcabinet.data.repository

import com.familymedcabinet.data.local.PreferencesDataStore
import com.familymedcabinet.data.local.database.dao.MedicineDao
import com.familymedcabinet.data.local.database.dao.ProfileDao
import com.familymedcabinet.data.local.database.entity.MedicineEntity
import com.familymedcabinet.data.local.database.entity.ProfileEntity
import com.familymedcabinet.domain.model.FamilyRelation
import com.familymedcabinet.domain.model.Medicine
import com.familymedcabinet.domain.model.Profile
import com.familymedcabinet.domain.model.UserPreferences
import com.familymedcabinet.domain.repository.MedicineRepository
import com.familymedcabinet.domain.repository.PreferencesRepository
import com.familymedcabinet.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileRepositoryImpl @Inject constructor(
    private val profileDao: ProfileDao
) : ProfileRepository {
    override fun observeProfiles(): Flow<List<Profile>> =
        profileDao.observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun getProfile(id: Long): Profile? =
        profileDao.getById(id)?.toDomain()

    override suspend fun saveProfile(profile: Profile): Long {
        val entity = profile.toEntity()
        return if (profile.id == 0L) profileDao.insert(entity)
        else { profileDao.update(entity); profile.id }
    }

    override suspend fun deleteProfile(id: Long) = profileDao.delete(id)
}

@Singleton
class MedicineRepositoryImpl @Inject constructor(
    private val medicineDao: MedicineDao
) : MedicineRepository {
    override fun observeMedicinesForProfile(profileId: Long): Flow<List<Medicine>> =
        medicineDao.observeActiveForProfile(profileId).map { list -> list.map { it.toDomain() } }

    override fun observeExpiringBefore(beforeMillis: Long): Flow<List<Medicine>> =
        medicineDao.observeExpiringBefore(beforeMillis).map { list -> list.map { it.toDomain() } }

    override suspend fun getMedicine(id: Long): Medicine? =
        medicineDao.getById(id)?.toDomain()

    override suspend fun saveMedicine(medicine: Medicine): Long {
        val entity = medicine.toEntity()
        return if (medicine.id == 0L) medicineDao.insert(entity)
        else { medicineDao.update(entity); medicine.id }
    }

    override suspend fun deleteMedicine(id: Long) = medicineDao.delete(id)
}

@Singleton
class PreferencesRepositoryImpl @Inject constructor(
    private val dataStore: PreferencesDataStore
) : PreferencesRepository {
    override fun getUserPreferences(): Flow<UserPreferences> = dataStore.preferencesFlow
    override suspend fun updatePreferences(transform: (UserPreferences) -> UserPreferences) =
        dataStore.update(transform)
}

private fun ProfileEntity.toDomain() = Profile(
    id = id,
    name = name,
    relation = runCatching { FamilyRelation.valueOf(relation) }.getOrDefault(FamilyRelation.OTHER),
    age = age
)

private fun Profile.toEntity() = ProfileEntity(
    id = id,
    name = name,
    relation = relation.name,
    age = age
)

private fun MedicineEntity.toDomain() = Medicine(
    id = id,
    profileId = profileId,
    name = name,
    dosage = dosage,
    quantityRemaining = quantityRemaining,
    expiryDateMillis = expiryDateMillis,
    purpose = purpose,
    refillReminder = refillReminder,
    finished = finished
)

private fun Medicine.toEntity() = MedicineEntity(
    id = id,
    profileId = profileId,
    name = name,
    dosage = dosage,
    quantityRemaining = quantityRemaining,
    expiryDateMillis = expiryDateMillis,
    purpose = purpose,
    refillReminder = refillReminder,
    finished = finished
)
