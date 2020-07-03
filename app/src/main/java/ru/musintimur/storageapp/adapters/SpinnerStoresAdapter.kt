package ru.musintimur.storageapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import ru.musintimur.storageapp.R
import ru.musintimur.storageapp.model.room.stores.Store

class SpinnerStoresAdapter : BaseAdapter() {

    val stores: MutableList<Store> = mutableListOf()

    override fun getItem(position: Int): Any = stores[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getCount(): Int = stores.size

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return getCustomView(position, parent, false)
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return getCustomView(position, parent, true)
    }

    private fun getCustomView(position: Int, parent: ViewGroup, hideFirst: Boolean): View {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_spinner_store, parent, false)
        val store = getItem(position) as Store
        view.findViewById<TextView>(R.id.textViewSpinnerStoreName).text = if (hideFirst && position == 0) "" else store.name
        return view
    }

    fun findPositionByUuid(uuid: String): Int = stores.indexOfFirst { it.id == uuid }

    fun getItemByUuid(uuid: String): Store = getItem(findPositionByUuid(uuid)) as Store

    fun updateStores(newStores: List<Store>, vararg strings: String) {
        stores.clear()
        stores.add(getDummyStore(strings[0]))
        stores.addAll(newStores)
        stores.add(getDummyStore(strings[1]))
        notifyDataSetChanged()
    }

    private fun getDummyStore(name: String): Store = Store("", name, "")

}