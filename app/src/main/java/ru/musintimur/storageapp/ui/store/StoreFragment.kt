package ru.musintimur.storageapp.ui.store


import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import com.google.android.gms.maps.MapsInitializer
import kotlinx.android.synthetic.main.fragment_store.*

import ru.musintimur.storageapp.R
import ru.musintimur.storageapp.app.*
import ru.musintimur.storageapp.model.room.stores.Store
import ru.musintimur.storageapp.ui.FragmentContract
import ru.musintimur.storageapp.ui.FragmentListener
import java.util.*

class StoreFragment : Fragment()
    , FragmentContract {

    private val args: StoreFragmentArgs by navArgs()
    private val store: Store? by lazy { args.argStore }
    private lateinit var listener: FragmentListener
    private lateinit var storeViewModel: StoreViewModel

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
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_store, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        storeViewModel = ViewModelProvider(this).get(StoreViewModel::class.java)

        store?.let {
            editTextStoreName.setText(it.name)
            editTextStoreAddress.setText(it.address)
        }

        setupListeners()
        setupObservers()

        mapViewStore.onCreate(savedInstanceState)
        mapViewStore.getMapAsync { googleMap ->
            MapsInitializer.initialize(requireContext())
            store?.let {
                googleMap.setupMapByAddress(requireContext(), it.address)
            } ?: googleMap.setupDefaultLocation()
        }

        if (store == null)
            requireContext().showKeyboard(editTextStoreName)
        else
            requireContext().hideKeyboard(editTextStoreName)
    }

    override fun onResume() {
        super.onResume()
        mapViewStore.onResume()
        listener.setBottomNavigationVisibility(FragmentListener.Visibility.HIDDEN)
        setOnDeleteItemClick()
    }

    private fun setupObservers() {
        val owner = viewLifecycleOwner
        storeViewModel.isComplete().observe(owner, Observer {
            if (it) listener.getNavController().popBackStack()
        })
    }

    private fun setupListeners() {
        fabSaveStore.setOnClickListener {
            if (checkInputPassed()) {
                if (store == null) {
                    storeViewModel.insertStore(getStoreFromInput())
                } else {
                    storeViewModel.updateStore(getStoreFromInput())
                }
            }
        }

        buttonFindAddress.setOnClickListener {
            val address = editTextStoreAddress.text
            if (address.isNotBlank()) {
                mapViewStore.getMapAsync { googleMap ->
                    googleMap.setupMapByAddress(requireContext(), address.toString())
                }
            } else {
                Toast.makeText(requireContext(), getString(R.string.store_address_is_empty), Toast.LENGTH_SHORT).show()
            }
            requireContext().hideKeyboard(it)
        }
    }

    private fun setOnDeleteItemClick() {
        listener.setOnDeleteItemClick {
            AlertDialog.Builder(requireContext())
                .setMessage(getString(R.string.warning_delete_store))
                .setPositiveButton(R.string.yes) { _, _ ->
                    store?.let { s -> storeViewModel.deleteStore(s) }
                }
                .setNegativeButton(R.string.no) { _, _ -> }
                .create()
                .show()
        }
    }

    private fun checkInputPassed(): Boolean {
        if (editTextStoreName.text.isBlank()) {
            Toast.makeText(requireContext(), resources.getString(R.string.store_name_is_blank), Toast.LENGTH_SHORT)
                .show()
            return false
        }
        if (editTextStoreAddress.text.isBlank()) {
            Toast.makeText(requireContext(), resources.getString(R.string.store_address_is_blank), Toast.LENGTH_SHORT)
                .show()
            return false
        }
        return true
    }

    private fun getStoreFromInput(): Store =
        Store(
            id = store?.id ?: UUID.randomUUID().toString(),
            name = editTextStoreName.text.toString(),
            address = editTextStoreAddress.text.toString()
        )

    private fun checkChanges(): Boolean =
        if (store == null) {
            editTextStoreName.text.isNotBlank() ||
                    editTextStoreAddress.text.isNotBlank()
        } else {
            store != getStoreFromInput()
        }

    override fun onBackPressed() {
        if (checkChanges()) {
            AlertDialog.Builder(requireContext())
                .setMessage(getString(R.string.warning_close))
                .setPositiveButton(R.string.yes) { _, _ ->
                    listener.getNavController().popBackStack()
                }
                .setNegativeButton(R.string.no) { _, _ -> }
                .create()
                .show()
        } else {
            listener.getNavController().popBackStack()
        }
    }
}
