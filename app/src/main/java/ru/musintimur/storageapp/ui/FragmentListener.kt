package ru.musintimur.storageapp.ui

import androidx.navigation.NavController

interface FragmentListener {

    enum class Visibility {
        HIDDEN, VISIBLE
    }

    fun openGallery()
    fun openDocuments()
    fun getNavController(): NavController
    fun setBottomNavigationVisibility(visibility: Visibility)
    fun setProgressBarVisibility(visibility: Visibility)
    fun getRecyclerViewColumnCount(cardWidth: Float): Int
    fun setOnDeleteItemClick(func: () -> (Unit))
    fun setOnSearchItemClick(func: (String) -> (Unit))
    fun setOnExportItemClick(func: () -> Unit)
    fun setOnImportItemClick(func: () -> Unit)
    fun resetOnDeleteItemClick()
    fun resetOnSearchItemClick()
    fun resetOnExportItemClick()
    fun resetOnImportItemClick()
}