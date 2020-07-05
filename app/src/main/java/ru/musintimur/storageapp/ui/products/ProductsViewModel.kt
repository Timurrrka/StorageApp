package ru.musintimur.storageapp.ui.products

import android.app.Application
import android.os.Parcelable
import android.widget.Toast
import androidx.lifecycle.*
import androidx.paging.DataSource
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.google.gson.Gson
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.musintimur.storageapp.app.*
import ru.musintimur.storageapp.model.room.ExchangeSchema
import ru.musintimur.storageapp.model.room.ImageSource
import ru.musintimur.storageapp.model.room.product.Product
import ru.musintimur.storageapp.R
import ru.musintimur.storageapp.model.room.product.ProductDataSource

class ProductsViewModel(
    application: Application,
    queryMode: ProductsMode,
    private val storeUuid: String,
    private val productSearchName: String
) : AndroidViewModel(application) {

    private val repository = Injection.provideStorageRepository()
    private val query: suspend ((Int, Int) -> List<Product>) = when (queryMode) {
        ProductsMode.ALL_PRODUCTS -> { page, limit -> repository.getProducts(page, limit) }
        ProductsMode.FILTER_BY_STORE -> { page, limit -> repository.getProductsInStore(storeUuid, page, limit) }
        ProductsMode.SEARCH_NAME -> { page, limit -> repository.findProductsByName("%$productSearchName%", page, limit) }
    }
    private val pagedListConfig = PagedList.Config.Builder()
        .setPageSize(ProductDataSource.LOAD_LIMIT)
        .setEnablePlaceholders(false)
        .build()

    private val _pagedProducts: LiveData<PagedList<Product>> =
        getPagedListBuilder(pagedListConfig).build()
    private val _jsonData = MutableLiveData<String>()
    private val _exchangeSchema = MutableLiveData<ExchangeSchema?>()
    private val _isImported = MutableLiveData<Boolean>(false)
    private val _layoutManagerState = MutableLiveData<Parcelable?>()
    private var layoutManagerState: Parcelable? = null

    fun getPagedProducts(): LiveData<PagedList<Product>> = _pagedProducts
    fun getJsonData(): LiveData<String> = _jsonData
    fun getExchangeSchema(): LiveData<ExchangeSchema?> = _exchangeSchema
    fun isImported(): LiveData<Boolean> = _isImported
    fun getLayoutManagerState(): LiveData<Parcelable?> = _layoutManagerState

    private fun getPagedListBuilder(config: PagedList.Config):
            LivePagedListBuilder<Int, Product> {

        val dataSourceFactory = object : DataSource.Factory<Int, Product>() {
            override fun create(): DataSource<Int, Product> =
                ProductDataSource(viewModelScope, query)
        }
        return LivePagedListBuilder(dataSourceFactory, config)
    }

    fun saveLayoutManagerState(parcelable: Parcelable?) {
        layoutManagerState = parcelable
    }

    fun refreshContent() = viewModelScope.launch {
        val state = layoutManagerState
        _pagedProducts.value?.dataSource?.invalidate()
        _isImported.postValue(false)
        delay(500)
        _layoutManagerState.postValue(state)
        layoutManagerState = null
    }

    fun exportRepository() {
        val stores = viewModelScope.async {
            repository.getAllStores()
        }
        val products = viewModelScope.async {
            repository.getAllProducts()
        }
        viewModelScope.launch {
            val exchangeSchema = ExchangeSchema(stores.await(), products.await(), encodeImages(products.await()))
            val jsonData = Gson().toJson(exchangeSchema)
            _jsonData.postValue(jsonData)
        }
    }

    fun importRepository(json: String) {
        val context = getApplication<Application>().applicationContext
        val data = importData(context, json)
        if (data != null) {
            _exchangeSchema.postValue(data)
        } else {
            Toast.makeText(context, context.getString(R.string.invalid_data), Toast.LENGTH_SHORT).show()
            _isImported.postValue(true)
        }
    }

    fun importData(exchangeSchema: ExchangeSchema) {
        val stores = exchangeSchema.stores
        val products = exchangeSchema.products
        val images = exchangeSchema.images
        viewModelScope.launch {
            stores.forEach { store ->
                repository.insertStore(store)
            }
            products.forEach { product ->
                repository.insertProduct(product)
            }
            images.forEach { image ->
                if (image.source == ImageSource.LOCAL) {
                    decodeImage(getApplication<Application>().applicationContext, image)?.let { uri ->
                        repository.updateProductImageUri(image.productUuid, uri)
                    }
                }
            }
            _exchangeSchema.postValue(null)
            _isImported.postValue(true)
        }
    }
}