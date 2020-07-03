package ru.musintimur.storageapp.app

import ru.musintimur.storageapp.model.StorageAppRepository
import ru.musintimur.storageapp.model.room.RoomRepository

object Injection {

    fun provideStorageRepository(): StorageAppRepository = RoomRepository()

}