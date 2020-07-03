package ru.musintimur.storageapp.model.room.stores

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize
import java.util.*

@Parcelize
@Entity(tableName = "stores")
data class Store(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val address: String
) : Parcelable