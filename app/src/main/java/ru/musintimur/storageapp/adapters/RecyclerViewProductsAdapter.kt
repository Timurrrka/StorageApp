package ru.musintimur.storageapp.adapters

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.item_product.view.*
import ru.musintimur.storageapp.R
import ru.musintimur.storageapp.app.asPrice
import ru.musintimur.storageapp.model.room.product.Product
import ru.musintimur.storageapp.model.room.product.ProductDiffCallback

class RecyclerViewProductsAdapter :
    PagedListAdapter<Product, RecyclerViewProductsAdapter.Companion.ProductsViewHolder>(ProductDiffCallback()) {

    var onItemClick: ((Product) -> Unit)? = null

    companion object {
        class ProductsViewHolder(view: View) : RecyclerView.ViewHolder(view)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductsViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_product, parent, false)
        return ProductsViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductsViewHolder, position: Int) {
        getItem(position)?.let { product ->
            holder.itemView.apply {
                textViewProductName.text = product.name
                textViewProductPrice.text = product.price.asPrice()
                Picasso.get()
                    .load(Uri.parse(product.imageUri))
                    .error(R.drawable.ic_insert_photo_black_24dp)
                    .into(imageViewProduct)
                setOnClickListener { onItemClick?.invoke(product) }
            }
        }
    }
}