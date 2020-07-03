package ru.musintimur.storageapp.model

import ru.musintimur.storageapp.model.room.product.Product
import ru.musintimur.storageapp.model.room.stores.Store

interface StorageAppRepository {

    suspend fun insertProduct(product: Product)
    suspend fun updateProduct(product: Product)
    suspend fun deleteProduct(product: Product)
    suspend fun getProducts(page: Int, limit: Int): List<Product>
    suspend fun getAllProducts(): List<Product>
    suspend fun getProductById(id: String): Product
    suspend fun getProductsInStore(storeUuid: String, page: Int, limit: Int): List<Product>
    suspend fun findProductsByName(name: String, page: Int, limit: Int): List<Product>
    suspend fun updateProductImageUri(id: String, uri: String)

    suspend fun insertStore(store: Store)
    suspend fun updateStore(store: Store)
    suspend fun deleteStore(store: Store)
    suspend fun getAllStores(): List<Store>
    suspend fun getStoreById(id: String): Store
}