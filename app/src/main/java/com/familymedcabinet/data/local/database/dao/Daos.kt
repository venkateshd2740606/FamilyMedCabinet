package com.familymedcabinet.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.familymedcabinet.data.local.database.entity.MedicineEntity
import com.familymedcabinet.data.local.database.entity.ProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfileDao {
    @Query("SELECT * FROM profiles ORDER BY name ASC")
    fun observeAll(): Flow<List<ProfileEntity>>

    @Query("SELECT * FROM profiles WHERE id = :id")
    suspend fun getById(id: Long): ProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: ProfileEntity): Long

    @Update
    suspend fun update(entity: ProfileEntity)

    @Query("DELETE FROM profiles WHERE id = :id")
    suspend fun delete(id: Long)
}

@Dao
interface MedicineDao {
    @Query("SELECT * FROM medicines WHERE profileId = :profileId AND finished = 0 ORDER BY expiryDateMillis ASC")
    fun observeActiveForProfile(profileId: Long): Flow<List<MedicineEntity>>

    @Query("SELECT * FROM medicines WHERE finished = 0 AND expiryDateMillis <= :beforeMillis ORDER BY expiryDateMillis ASC")
    fun observeExpiringBefore(beforeMillis: Long): Flow<List<MedicineEntity>>

    @Query("SELECT * FROM medicines WHERE id = :id")
    suspend fun getById(id: Long): MedicineEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: MedicineEntity): Long

    @Update
    suspend fun update(entity: MedicineEntity)

    @Query("DELETE FROM medicines WHERE id = :id")
    suspend fun delete(id: Long)
}
