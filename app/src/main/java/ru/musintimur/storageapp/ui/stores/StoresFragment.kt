package ru.musintimur.storageapp.ui.stores

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavDirections
import androidx.recyclerview.widget.GridLayoutManager
import kotlinx.android.synthetic.main.fragment_stores.*
import ru.musintimur.storageapp.R
import ru.musintimur.storageapp.adapters.RecyclerViewStoresAdapter
import ru.musintimur.storageapp.app.hideKeyboard
import ru.musintimur.storageapp.model.room.stores.Store
import ru.musintimur.storageapp.ui.FragmentListener
import ru.musintimur.storageapp.ui.products.ProductsMode

class StoresFragment : Fragment() {

    private lateinit var listener: FragmentListener
    private lateinit var storesViewModel: StoresViewModel
    private lateinit var storesAdapter: RecyclerViewStoresAdapter

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
        return inflater.inflate(R.layout.fragment_stores, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        storesViewModel = ViewModelProvider(this).get(StoresViewModel::class.java)
        storesAdapter = RecyclerViewStoresAdapter()
        recyclerViewStores.layoutManager =
            GridLayoutManager(requireContext(), listener.getRecyclerViewColumnCount(500F))
        recyclerViewStores.adapter = storesAdapter

        setupObservers()
        storesViewModel.loadStores()

        fabAddStore.setOnClickListener { openStoreEditor(null) }
        storesAdapter.onStoreClick = { store -> openStoreEditor(store) }
        storesAdapter.onProductsClick = { storeUuid -> openStoreProducts(storeUuid) }
    }

    override fun onResume() {
        super.onResume()
        listener.setBottomNavigationVisibility(FragmentListener.Visibility.VISIBLE)
        requireContext().hideKeyboard(recyclerViewStores)
    }

    private fun setupObservers() {
        storesViewModel.getStores().observe(viewLifecycleOwner, Observer { stores ->
            storesAdapter.updateStores(stores)
        })
    }

    private fun openFragment(directions: NavDirections) {
        listener.getNavController().navigate(directions)
    }

    private fun openStoreEditor(store: Store?) {
        openFragment(StoresFragmentDirections.actionStoresToStore(store))
    }

    private fun openStoreProducts(storeUuid: String) {
        openFragment(StoresFragmentDirections.actionStoresToProducts(ProductsMode.FILTER_BY_STORE, storeUuid))
    }
}