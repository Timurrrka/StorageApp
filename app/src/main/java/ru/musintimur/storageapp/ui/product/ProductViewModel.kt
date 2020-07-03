package ru.musintimur.storageapp.ui.product

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.musintimur.storageapp.app.Injection
import ru.musintimur.storageapp.model.room.product.Product
import ru.musintimur.storageapp.model.room.RoomRepository
import ru.musintimur.storageapp.model.room.stores.Store

class ProductViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = Injection.provideStorageRepository()

    private val _stores = MutableLiveData<List<Store>>()
    private val _isComplete = MutableLiveData<Boolean>()

    fun getStores(): LiveData<List<Store>> = _stores
    fun isComplete(): LiveData<Boolean> = _isComplete

    fun saveNewProduct(product: Product) = viewModelScope.launch {
        repository.insertProduct(product)
        _isComplete.postValue(true)
    }

    fun updateProduct(product: Product) = viewModelScope.launch {
        repository.updateProduct(product)
        _isComplete.postValue(true)
    }

    fun deleteProduct(product: Product) = viewModelScope.launch {
        repository.deleteProduct(product)
        _isComplete.postValue(true)
    }

    fun loadStores() = viewModelScope.launch {
        _stores.postValue(repository.getAllStores())
    }

}