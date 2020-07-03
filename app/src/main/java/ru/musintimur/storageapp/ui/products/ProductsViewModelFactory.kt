package ru.musintimur.storageapp.ui.products

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider


class ProductsViewModelFactory(
    private val application: Application,
    private val queryMode: ProductsMode,
    private val storeUuid: String,
    private val productSearchName: String
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T =
        ProductsViewModel(application, queryMode, storeUuid, productSearchName) as T
}