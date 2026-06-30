package com.familymedcabinet.di

import com.familymedcabinet.data.repository.MedicineRepositoryImpl
import com.familymedcabinet.data.repository.PreferencesRepositoryImpl
import com.familymedcabinet.data.repository.ProfileRepositoryImpl
import com.familymedcabinet.domain.repository.MedicineRepository
import com.familymedcabinet.domain.repository.PreferencesRepository
import com.familymedcabinet.domain.repository.ProfileRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds @Singleton abstract fun bindProfileRepository(impl: ProfileRepositoryImpl): ProfileRepository
    @Binds @Singleton abstract fun bindMedicineRepository(impl: MedicineRepositoryImpl): MedicineRepository
    @Binds @Singleton abstract fun bindPreferencesRepository(impl: PreferencesRepositoryImpl): PreferencesRepository
}
