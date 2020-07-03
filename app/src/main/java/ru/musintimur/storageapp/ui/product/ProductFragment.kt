package ru.musintimur.storageapp.ui.product


import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import com.google.android.gms.maps.MapsInitializer
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_product.*

import ru.musintimur.storageapp.R
import ru.musintimur.storageapp.adapters.SpinnerStoresAdapter
import ru.musintimur.storageapp.app.*
import ru.musintimur.storageapp.model.room.product.Product
import ru.musintimur.storageapp.model.room.stores.Store
import ru.musintimur.storageapp.ui.FragmentContract
import ru.musintimur.storageapp.ui.FragmentListener
import ru.musintimur.storageapp.ui.MainActivity
import java.util.*

class ProductFragment : Fragment()
    , FragmentContract {

    private lateinit var listener: FragmentListener
    private lateinit var productViewModel: ProductViewModel
    private val spinnerStoresAdapter = SpinnerStoresAdapter()
    private val args: ProductFragmentArgs by navArgs()
    private val product: Product? by lazy { args.argProduct }
    private val productUuid: String by lazy { args.argProduct?.id ?: UUID.randomUUID().toString() }
    private var productImageUri: Uri = Uri.EMPTY
    private var isNewAddition = false

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
        return inflater.inflate(R.layout.fragment_product, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        productViewModel = ViewModelProvider(this).get(ProductViewModel::class.java)

        product?.let {
            editTextProductName.setText(it.name)
            editTextProductPrice.setText(it.price.toString())
            editTextImageUrl.setText(it.imageUri)
            setProductImage(Uri.parse(it.imageUri))
        }

        spinnerProductStore.adapter = spinnerStoresAdapter
        setupListeners()
        setupObservers()
        mapViewProductStore.onCreate(savedInstanceState)
        productViewModel.loadStores()

        if (product == null)
            requireContext().showKeyboard(editTextProductName)
        else
            requireContext().hideKeyboard(editTextProductName)
    }

    override fun onResume() {
        super.onResume()
        mapViewProductStore.onResume()
        isNewAddition = false
        listener.setBottomNavigationVisibility(FragmentListener.Visibility.HIDDEN)
        setOnDeleteItemClick()
    }

    override fun onPause() {
        super.onPause()
        listener.resetOnDeleteItemClick()
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

    private fun setOnDeleteItemClick() {
        listener.setOnDeleteItemClick {
            AlertDialog.Builder(requireContext())
                .setMessage(getString(R.string.warning_delete_product))
                .setPositiveButton(R.string.yes) { _, _ ->
                    product?.let { p -> productViewModel.deleteProduct(p) }
                }
                .setNegativeButton(R.string.no) { _, _ -> }
                .create()
                .show()
        }
    }

    private fun setupListeners() {
        fabSaveProduct.setOnClickListener {
            if (checkInputPassed()) {
                if (product == null) {
                    productViewModel.saveNewProduct(getProductFromInput())
                } else {
                    productViewModel.updateProduct(getProductFromInput())
                }
            }
        }

        imageButtonApplyLink.setOnClickListener {
            val imageUri = editTextImageUrl.text.toString()
            setProductImage(Uri.parse(imageUri))
        }
        imageViewProductImage.setOnClickListener { listener.openGallery() }

        editTextImageUrl.setOnTouchListener { _, _ -> editTextImageUrl.performLongClick() }

        spinnerProductStore.setOnTouchListener { _, _ ->
            isNewAddition = true
            requireContext().hideKeyboard(spinnerProductStore)
            false
        }

        spinnerProductStore.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                when {
                    position == spinnerStoresAdapter.count - 1 && isNewAddition -> {
                        isNewAddition = false
                        val direction = ProductFragmentDirections.actionProductToStore()
                        listener.getNavController().navigate(direction)
                    }
                    position in (1..(spinnerStoresAdapter.count - 2)) -> {
                        mapViewProductStore.getMapAsync { googleMap ->
                            googleMap.setupMapByAddress(
                                requireContext(),
                                (spinnerStoresAdapter.getItem(position) as Store).address
                            )
                        }
                    }
                }
            }
        }
    }

    private fun setupObservers() {
        val owner = viewLifecycleOwner

        productViewModel.run {
            isComplete().observe(owner, Observer {
                if (it) listener.getNavController().popBackStack()
            })

            getStores().observe(owner, Observer {
                spinnerStoresAdapter.updateStores(it, getString(R.string.spinner_pick), getString(R.string.spinner_add))
                product?.let { product ->
                    spinnerProductStore.setSelection(spinnerStoresAdapter.findPositionByUuid(product.storeUuid))
                    mapViewProductStore.getMapAsync { googleMap ->
                        MapsInitializer.initialize(requireContext())
                        googleMap.setupMapByAddress(
                            requireContext(),
                            spinnerStoresAdapter.getItemByUuid(product.storeUuid).address
                        )
                    }
                }
            })
        }
    }

    private fun getProductFromInput(): Product =
        Product(
            id = productUuid,
            name = editTextProductName.text.toString(),
            price = editTextProductPrice.text.toString().toFloatOrNull() ?: 0F,
            storeUuid = getStoreId(),
            imageUri = productImageUri.toString()
        )

    private fun getStoreId(): String =
        (spinnerStoresAdapter.getItem(spinnerProductStore.selectedItemPosition) as Store).id

    private fun checkInputPassed(): Boolean {
        if (editTextProductName.text.isBlank()) {
            Toast.makeText(requireContext(), resources.getString(R.string.product_name_is_blank), Toast.LENGTH_SHORT)
                .show()
            return false
        }
        if (editTextProductPrice.text.isBlank()) {
            Toast.makeText(requireContext(), resources.getString(R.string.product_price_is_blank), Toast.LENGTH_SHORT)
                .show()
            return false
        }
        if ((editTextProductPrice.text.toString().toFloatOrNull() ?: 0F) <= 0F) {
            Toast.makeText(requireContext(), resources.getString(R.string.product_price_is_zero), Toast.LENGTH_SHORT)
                .show()
            return false
        }
        if (spinnerProductStore.selectedItemPosition in setOf(0, spinnerStoresAdapter.count - 1)) {
            Toast.makeText(requireContext(), resources.getString(R.string.product_store_is_empty), Toast.LENGTH_SHORT)
                .show()
            return false
        }
        if (productImageUri.toString().isEmpty()) {
            Toast.makeText(requireContext(), resources.getString(R.string.product_image_is_blank), Toast.LENGTH_SHORT)
                .show()
            return false
        }
        return true
    }

    private fun checkChanges(): Boolean =
        if (product == null) {
            editTextProductName.text.isNotBlank() ||
                    editTextProductPrice.text.isNotBlank() ||
                    productImageUri.toString().isNotBlank()
        } else {
            product != getProductFromInput()
        }

    private fun setProductImage(imageUri: Uri) {
        productImageUri = imageUri
        Picasso.get()
            .load(productImageUri)
            .error(R.drawable.ic_insert_photo_black_24dp)
            .into(imageViewProductImage)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            MainActivity.REQUEST_CODE_GALLERY -> {
                if (resultCode == Activity.RESULT_OK) {
                    data?.data?.let { uri ->
                        editTextImageUrl.setText("")
                        val savePath = try {
                            saveCompressedImage(requireContext(), uri, productUuid, 30)
                        } catch (e: Exception) {
                            Log.e(
                                this@ProductFragment::class.java.simpleName,
                                getString(R.string.exception_saving_picture)
                            )
                            null
                        }
                        savePath?.let { sp ->
                            editTextImageUrl.setText(sp.toString())
                            setProductImage(sp)
                        }
                    }
                }
            }
            else -> {
            }
        }
    }
}
