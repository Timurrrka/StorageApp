package ru.musintimur.storageapp.model.room

import android.util.Base64
import ru.musintimur.storageapp.model.room.product.Product
import ru.musintimur.storageapp.model.room.stores.Store

data class ExchangeSchema(
    val stores: List<Store>,
    val products: List<Product>,
    val images: List<ImageData>
)

enum class ImageSource {
    LOCAL, WEB
}

data class ImageData(
    val source: ImageSource,
    val productUuid: String,
    val name: String,
    val base64: String?
)