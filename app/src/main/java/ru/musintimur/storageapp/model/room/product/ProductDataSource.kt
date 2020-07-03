package ru.musintimur.storageapp.model.room.product


import androidx.paging.PageKeyedDataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class ProductDataSource(
    private val scope: CoroutineScope,
    private val query: suspend (Int, Int) -> List<Product>
) : PageKeyedDataSource<Int, Product>() {

    companion object {
        const val INITIAL_PAGE = 1
        const val LOAD_LIMIT = 20
    }

    private var page = INITIAL_PAGE

    private fun nextPage(): Int {
        page = page.inc()
        return page
    }

    private fun prevPage(): Int {
        page = page.dec()
        return page
    }

    override fun loadInitial(params: LoadInitialParams<Int>, callback: LoadInitialCallback<Int, Product>) {
        scope.launch {
            query(INITIAL_PAGE, LOAD_LIMIT).let { products ->
                callback.onResult(products, null, nextPage())
            }
        }
    }

    override fun loadAfter(params: LoadParams<Int>, callback: LoadCallback<Int, Product>) {
        scope.launch {
            query(page, LOAD_LIMIT).let { products ->
                callback.onResult(products, nextPage())
            }
        }
    }

    override fun loadBefore(params: LoadParams<Int>, callback: LoadCallback<Int, Product>) {
        scope.launch {
            query(page, LOAD_LIMIT).let { products ->
                callback.onResult(products, prevPage())
            }
        }
    }
}