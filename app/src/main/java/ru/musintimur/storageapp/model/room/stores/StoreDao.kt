package ru.musintimur.storageapp.model.room.stores

import androidx.room.*

@Dao
interface StoreDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStore(store: Store)

    @Update
    suspend fun updateStore(store: Store)

    @Delete
    suspend fun deleteStore(store: Store)

    @Query("SELECT * FROM stores")
    suspend fun getAllStores(): List<Store>

    @Query("SELECT * FROM stores where id = :id")
    suspend fun getStoreById(id: String): Store
}