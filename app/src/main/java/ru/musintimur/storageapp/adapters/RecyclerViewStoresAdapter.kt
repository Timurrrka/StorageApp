package ru.musintimur.storageapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.MapsInitializer
import kotlinx.android.synthetic.main.item_store.view.*
import ru.musintimur.storageapp.R
import ru.musintimur.storageapp.app.setupMapByAddress
import ru.musintimur.storageapp.model.room.stores.Store

class RecyclerViewStoresAdapter : RecyclerView.Adapter<RecyclerViewStoresAdapter.Companion.StoreViewHolder>() {

    private val stores: MutableList<Store> = mutableListOf()
    lateinit var onStoreClick: ((Store) -> Unit)
    lateinit var onProductsClick: ((String) -> Unit)

    companion object {
        class StoreViewHolder(view: View) : RecyclerView.ViewHolder(view)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoreViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_store, parent, false)
        return StoreViewHolder(view)
    }

    override fun getItemCount(): Int = stores.size

    override fun onBindViewHolder(holder: StoreViewHolder, position: Int) {
        val store = stores[position]
        holder.itemView.apply {
            textViewStoreName.text = store.name
            textViewStoreAddress.text = store.address
            setOnClickListener { onStoreClick.invoke(store) }
            fragmentStoreProducts.setOnClickListener { onProductsClick.invoke(store.id) }

            mapViewStoreItem.onCreate(null)
            mapViewStoreItem.getMapAsync { googleMap ->
                MapsInitializer.initialize(context)
                googleMap.setupMapByAddress(context, store.address)
            }
            mapViewStoreItem.onResume()
        }
    }

    fun updateStores(newStores: List<Store>) {
        stores.clear()
        stores.addAll(newStores)
        notifyDataSetChanged()
    }
}