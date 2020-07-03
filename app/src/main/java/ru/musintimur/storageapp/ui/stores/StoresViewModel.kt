package ru.musintimur.storageapp.ui.stores

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.musintimur.storageapp.app.Injection
import ru.musintimur.storageapp.model.room.stores.Store

class StoresViewModel : ViewModel() {

    private val repository = Injection.provideStorageRepository()

    private val _stores = MutableLiveData<List<Store>>()

    fun getStores(): LiveData<List<Store>> = _stores

    fun loadStores() = viewModelScope.launch {
        _stores.postValue(repository.getAllStores())
    }

}