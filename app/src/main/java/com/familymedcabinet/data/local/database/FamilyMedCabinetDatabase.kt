package com.familymedcabinet.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.familymedcabinet.data.local.database.dao.MedicineDao
import com.familymedcabinet.data.local.database.dao.ProfileDao
import com.familymedcabinet.data.local.database.entity.MedicineEntity
import com.familymedcabinet.data.local.database.entity.ProfileEntity

@Database(
    entities = [ProfileEntity::class, MedicineEntity::class],
    version = 2,
    exportSchema = true
)
abstract class FamilyMedCabinetDatabase : RoomDatabase() {
    abstract fun profileDao(): ProfileDao
    abstract fun medicineDao(): MedicineDao
}
