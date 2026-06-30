package com.familymedcabinet.di

import android.content.Context
import androidx.room.Room
import com.familymedcabinet.data.local.database.FamilyMedCabinetDatabase
import com.familymedcabinet.data.local.database.dao.MedicineDao
import com.familymedcabinet.data.local.database.dao.ProfileDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): FamilyMedCabinetDatabase =
        Room.databaseBuilder(context, FamilyMedCabinetDatabase::class.java, "familymedcabinet.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides fun provideProfileDao(db: FamilyMedCabinetDatabase): ProfileDao = db.profileDao()
    @Provides fun provideMedicineDao(db: FamilyMedCabinetDatabase): MedicineDao = db.medicineDao()
}
