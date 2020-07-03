package ru.musintimur.storageapp.ui.store

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.musintimur.storageapp.app.Injection
import ru.musintimur.storageapp.model.room.stores.Store

class StoreViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = Injection.provideStorageRepository()

    private val _isComplete = MutableLiveData<Boolean>()

    fun isComplete(): LiveData<Boolean> = _isComplete

    fun insertStore(store: Store) = viewModelScope.launch {
        repository.insertStore(store)
        _isComplete.postValue(true)
    }

    fun updateStore(store: Store) = viewModelScope.launch {
        repository.updateStore(store)
        _isComplete.postValue(true)
    }

    fun deleteStore(store: Store) = viewModelScope.launch {
        repository.deleteStore(store)
        _isComplete.postValue(true)
    }

}