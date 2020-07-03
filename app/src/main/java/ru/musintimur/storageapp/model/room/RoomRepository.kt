package ru.musintimur.storageapp.model.room


import ru.musintimur.storageapp.app.StorageApplication
import ru.musintimur.storageapp.model.StorageAppRepository
import ru.musintimur.storageapp.model.room.product.Product
import ru.musintimur.storageapp.model.room.product.ProductDao
import ru.musintimur.storageapp.model.room.stores.Store
import ru.musintimur.storageapp.model.room.stores.StoreDao

class RoomRepository : StorageAppRepository {

    private val productDao: ProductDao = StorageApplication.database.productDao()
    private val storeDao: StoreDao = StorageApplication.database.storeDao()

    override suspend fun insertProduct(product: Product) {
        productDao.insertProduct(product)
    }

    override suspend fun updateProduct(product: Product) {
        productDao.updateProduct(product)
    }

    override suspend fun deleteProduct(product: Product) {
        productDao.deleteProduct(product)
    }

    override suspend fun getProducts(page: Int, limit: Int): List<Product> =
        productDao.getProducts(page, limit)

    override suspend fun getAllProducts(): List<Product> = productDao.getAllProducts()

    override suspend fun getProductById(id: String): Product = productDao.getProductById(id)

    override suspend fun getProductsInStore(storeUuid: String, page: Int, limit: Int): List<Product> =
        productDao.getProductsInStore(storeUuid, page, limit)

    override suspend fun findProductsByName(name: String, page: Int, limit: Int): List<Product> =
        productDao.findProductsByName(name, page, limit)

    override suspend fun updateProductImageUri(id: String, uri: String) {
        productDao.updateProductImageUri(id, uri)
    }

    override suspend fun insertStore(store: Store) {
        storeDao.insertStore(store)
    }

    override suspend fun updateStore(store: Store) {
        storeDao.updateStore(store)
    }

    override suspend fun deleteStore(store: Store) {
        storeDao.deleteStore(store)
    }

    override suspend fun getAllStores(): List<Store> = storeDao.getAllStores()

    override suspend fun getStoreById(id: String): Store = storeDao.getStoreById(id)
}