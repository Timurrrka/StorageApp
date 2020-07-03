package ru.musintimur.storageapp.app

import android.content.Context
import android.location.Geocoder
import android.util.Log
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import ru.musintimur.storageapp.R
import ru.musintimur.storageapp.model.room.stores.Store

const val MAP_ZOOM_LEVEL_INITIAL = 10f
const val MAP_ZOOM_LEVEL = 15f

private const val TAG = "Location"
private val SAMARA = LatLng(53.11, 50.07)

fun getDefaultLocation(): LatLng = SAMARA

fun getLocationByAddress(context: Context, address: String): LatLng? {
    return try {
        Geocoder(context).getFromLocationName(address, 1).first().let {
            LatLng(it.latitude, it.longitude)
        }
    } catch (e: Exception) {
        Log.e(TAG, context.getString(R.string.exception_getting_location))
        null
    }
}

fun GoogleMap.setupMapByAddress(context: Context, address: String) {
    val location = getLocationByAddress(context, address)

    if (location == null || location == getDefaultLocation()) {
        this.setupDefaultLocation()
    } else {
        this.apply {
            uiSettings.isMyLocationButtonEnabled = false
            mapType = GoogleMap.MAP_TYPE_NORMAL
            clear()
            moveCamera(CameraUpdateFactory.newLatLngZoom(location, MAP_ZOOM_LEVEL))
            addMarker(
                MarkerOptions()
                    .position(location)
                    .title(context.getString(R.string.store_coordinates, location.latitude, location.longitude))
            ).showInfoWindow()
        }
    }
}

fun GoogleMap.setupDefaultLocation() {
    this.apply {
        uiSettings.isMyLocationButtonEnabled = false
        mapType = GoogleMap.MAP_TYPE_NORMAL
        moveCamera(CameraUpdateFactory.newLatLngZoom(getDefaultLocation(), MAP_ZOOM_LEVEL_INITIAL))
    }
}