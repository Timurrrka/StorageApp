package ru.musintimur.storageapp.model.room.product

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize
import ru.musintimur.storageapp.model.room.stores.Store
import java.util.*

@Parcelize
@Entity(
    tableName = "products",
    foreignKeys = [ForeignKey(
        entity = Store::class,
        parentColumns = ["id"],
        childColumns = ["storeUuid"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class Product(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val price: Float,
    val storeUuid: String,
    val imageUri: String
) : Parcelable