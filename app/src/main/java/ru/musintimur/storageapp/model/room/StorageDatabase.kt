package ru.musintimur.storageapp.model.room

import androidx.room.Database
import androidx.room.RoomDatabase
import ru.musintimur.storageapp.model.room.product.Product
import ru.musintimur.storageapp.model.room.product.ProductDao
import ru.musintimur.storageapp.model.room.stores.Store
import ru.musintimur.storageapp.model.room.stores.StoreDao

@Database(entities = [(Product::class), (Store::class)], version = 1)
abstract class StorageDatabase : RoomDatabase() {

    abstract fun productDao(): ProductDao

    abstract fun storeDao(): StoreDao

}