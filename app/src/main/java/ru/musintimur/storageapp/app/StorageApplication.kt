package ru.musintimur.storageapp.app

import android.app.Application
import android.content.Context
import androidx.room.Room
import ru.musintimur.storageapp.model.room.StorageDatabase

class StorageApplication : Application() {

    companion object {

        lateinit var database: StorageDatabase

        private lateinit var instance: StorageApplication

    }

    override fun onCreate() {
        instance = this
        super.onCreate()

        database = Room.databaseBuilder(this, StorageDatabase::class.java, "storage_database").build()
    }
}