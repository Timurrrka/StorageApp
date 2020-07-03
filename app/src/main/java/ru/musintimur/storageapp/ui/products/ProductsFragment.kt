package ru.musintimur.storageapp.ui.products

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavDirections
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import kotlinx.android.synthetic.main.fragment_products.*
import ru.musintimur.storageapp.R
import ru.musintimur.storageapp.adapters.RecyclerViewProductsAdapter
import ru.musintimur.storageapp.app.exportData
import ru.musintimur.storageapp.app.EXPORT_FILE_EXTENSION
import ru.musintimur.storageapp.app.hideKeyboard
import ru.musintimur.storageapp.app.readDocument
import ru.musintimur.storageapp.model.room.product.Product
import ru.musintimur.storageapp.ui.FragmentListener
import ru.musintimur.storageapp.ui.MainActivity
import java.io.File

class ProductsFragment : Fragment() {

    private val args: ProductsFragmentArgs by navArgs()
    private val fragmentMode: ProductsMode by lazy { args.argMode }
    private val storeUuid: String by lazy { args.argStoreUuid }
    private val productSearchName: String by lazy { args.argName }
    private lateinit var listener: FragmentListener
    private lateinit var productsViewModel: ProductsViewModel
    private lateinit var productsAdapter: RecyclerViewProductsAdapter

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is FragmentListener) {
            listener = context
        } else {
            throw RuntimeException(
                getString(
                    R.string.exception_implement,
                    context::class.java.simpleName,
                    "FragmentListener"
                )
            )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_products, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        productsViewModel =
            ViewModelProvider(this, ProductsViewModelFactory(requireActivity().application, fragmentMode, storeUuid, productSearchName))
                .get(ProductsViewModel::class.java)
        productsAdapter = RecyclerViewProductsAdapter()
        recyclerViewProducts.layoutManager =
                StaggeredGridLayoutManager(listener.getRecyclerViewColumnCount(400F), StaggeredGridLayoutManager.VERTICAL)
        recyclerViewProducts.adapter = productsAdapter

        setupObservers()

        fabAddProduct.setOnClickListener { openProductEditor(null) }
        productsAdapter.onItemClick = { product -> openProductEditor(product) }
    }

    override fun onResume() {
        super.onResume()
        listener.setBottomNavigationVisibility(FragmentListener.Visibility.VISIBLE)
        listener.setOnSearchItemClick { productName -> openFoundedProducts(productName) }
        listener.setOnExportItemClick { productsViewModel.exportRepository() }
        listener.setOnImportItemClick { listener.openDocuments() }
        productsViewModel.refreshContent()
        requireContext().hideKeyboard(recyclerViewProducts)
    }

    private fun setupObservers() {
        val owner = viewLifecycleOwner
        productsViewModel.run {
            getPagedProducts().observe(owner, Observer {
                productsAdapter.submitList(it)
            })
            getJsonData().observe(owner, Observer { jsonData ->
                exportData(requireContext(), jsonData)
            })
            getExchangeSchema().observe(owner, Observer { exchangeSchema ->
                exchangeSchema?.let { productsViewModel.importData(it) }
            })
            isImported().observe(owner, Observer {
                if (it) {
                    productsViewModel.refreshContent()
                    listener.setProgressBarVisibility(FragmentListener.Visibility.HIDDEN)
                }
            })
        }
    }

    private fun openFragment(directions: NavDirections) {
        listener.getNavController().navigate(directions)
    }

    private fun openProductEditor(product: Product?) {
        openFragment(ProductsFragmentDirections.actionProductsToProduct(product))
    }

    private fun openFoundedProducts(productName: String) {
        openFragment(ProductsFragmentDirections.actionProductsSearch(ProductsMode.SEARCH_NAME, storeUuid, productName))
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            MainActivity.REQUEST_CODE_DOCUMENTS -> {
                if (resultCode == Activity.RESULT_OK) {
                    data?.data?.let { uri ->
                        uri.path?.let { path ->
                            val file = File(path)
                            if (file.exists() && file.extension != EXPORT_FILE_EXTENSION) {
                                Toast.makeText(
                                    requireContext(),
                                    getString(R.string.unsupported_file),
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                val content = try {
                                    readDocument(requireContext(), uri)
                                } catch (e: Exception) {
                                    Log.e(
                                        this@ProductsFragment::class.java.simpleName,
                                        e.localizedMessage ?: getString(R.string.exception_reading_document)
                                    )
                                    null
                                }
                                content?.let {
                                    listener.setProgressBarVisibility(FragmentListener.Visibility.VISIBLE)
                                    productsViewModel.importRepository(it)
                                }
                            }
                        }
                    }
                }
            }
            else -> {
            }
        }
    }

}