package ru.musintimur.storageapp.model.room.product

import androidx.room.*

@Dao
interface ProductDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: Product)

    @Update
    suspend fun updateProduct(product: Product)

    @Delete
    suspend fun deleteProduct(product: Product)

    @Query("SELECT * FROM products LIMIT :limit OFFSET ((:page - 1) * :limit)")
    suspend fun getProducts(page: Int, limit: Int): List<Product>

    @Query("SELECT * FROM products")
    suspend fun getAllProducts(): List<Product>

    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getProductById(id: String): Product

    @Query("SELECT * FROM products WHERE storeUuid = :storeUuid LIMIT :limit OFFSET ((:page - 1) * :limit)")
    suspend fun getProductsInStore(storeUuid: String, page: Int, limit: Int): List<Product>

    @Query("SELECT * FROM products WHERE lower(name) LIKE lower(:name) LIMIT :limit OFFSET ((:page - 1) * :limit)")
    suspend fun findProductsByName(name: String, page: Int, limit: Int): List<Product>

    @Query("UPDATE products SET imageUri = :uri WHERE id = :id")
    suspend fun updateProductImageUri(id: String, uri: String)

}