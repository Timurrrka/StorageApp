package ru.musintimur.storageapp.ui

import android.Manifest
import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.SearchView
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.findNavController
import androidx.navigation.get
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import kotlinx.android.synthetic.main.activity_main.*
import ru.musintimur.storageapp.R
import ru.musintimur.storageapp.app.showKeyboard
import ru.musintimur.storageapp.model.room.product.Product
import ru.musintimur.storageapp.model.room.stores.Store

class MainActivity : AppCompatActivity()
    , FragmentListener {

    companion object {
        const val REQUEST_CODE_GALLERY: Int = 100
        const val REQUEST_CODE_DOCUMENTS: Int = 101
        const val REQUEST_PERMISSIONS: Int = 102
    }

    private val mainNavController: NavController by lazy { findNavController(R.id.nav_host_fragment) }
    private val navView: BottomNavigationView by lazy { findViewById<BottomNavigationView>(R.id.nav_view) }
    private lateinit var mainMenu: Menu
    private lateinit var searchView: SearchView
    private lateinit var searchIcon: MenuItem
    private lateinit var searchBar: MenuItem
    private var onSearchItemClick: ((String) -> (Unit))? = null
    private var onDeleteItemClick: (() -> (Unit))? = null
    private var onExportItemClick: (() -> (Unit))? = null
    private var onImportItemClick: (() -> (Unit))? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_products,
                R.id.navigation_stores
            )
        )
        setupActionBarWithNavController(mainNavController, appBarConfiguration)
        navView.setupWithNavController(mainNavController)

        checkPermissions()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        mainMenu = menu
        menuInflater.inflate(R.menu.main_menu, mainMenu)

        mainNavController.addOnDestinationChangedListener { _, destination, bundle ->
            if (::searchBar.isInitialized) hideSearchBar()
            setupMenu(destination, bundle)
        }
        mainNavController.currentDestination?.let { setupMenu(it, null) }

        searchIcon = menu.findItem(R.id.itemSearch)
        searchBar = menu.findItem(R.id.appBarSearch)

        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        searchView = searchBar.actionView as SearchView
        val searchableInfo = searchManager.getSearchableInfo(componentName)
        searchView.run {
            setSearchableInfo(searchableInfo)
            isIconified = false
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    if (query == null) return false
                    onSearchItemClick?.invoke(query)
                    hideSearchBar()
                    return true
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    return false
                }
            })
            setOnCloseListener {
                hideSearchBar()
                true
            }
            clearFocus()
        }

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> onBackPressed()
            R.id.itemSearch -> showSearchBar()
            R.id.itemDelete -> onDeleteItemClick?.invoke()
            R.id.itemExport -> onExportItemClick?.invoke()
            R.id.itemImport -> onImportItemClick?.invoke()
        }
        return true
    }

    override fun onBackPressed() {
        supportFragmentManager.primaryNavigationFragment?.childFragmentManager?.fragments?.forEach { fragment ->
            if (fragment is FragmentContract) fragment.onBackPressed()
            else super.onBackPressed()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        supportFragmentManager.primaryNavigationFragment?.childFragmentManager?.fragments?.forEach {
            it.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun openGallery() {
        startActivityForResult(
            Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI).apply {
                type = "image/*"
            },
            REQUEST_CODE_GALLERY
        )
    }

    override fun openDocuments() {
        startActivityForResult(
            Intent(Intent.ACTION_OPEN_DOCUMENT, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI).apply {
                type = "*/*"
            },
            REQUEST_CODE_DOCUMENTS
        )
    }

    override fun getNavController(): NavController = mainNavController

    override fun setBottomNavigationVisibility(visibility: FragmentListener.Visibility) {
        if (visibility == FragmentListener.Visibility.VISIBLE) {
            navView.visibility = View.VISIBLE
        } else {
            navView.visibility = View.GONE
        }
    }

    override fun setProgressBarVisibility(visibility: FragmentListener.Visibility) {
        if (visibility == FragmentListener.Visibility.VISIBLE) {
            mainProgressBar.visibility = View.VISIBLE
        } else {
            mainProgressBar.visibility = View.GONE
        }
    }

    override fun getRecyclerViewColumnCount(cardWidth: Float): Int {
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val width = displayMetrics.widthPixels / displayMetrics.density
        val columnCount = (width / cardWidth).toInt()
        return if (columnCount > 1) columnCount else 1
    }

    private fun checkPermissions() {
        val isGranted = PackageManager.PERMISSION_GRANTED
        val permissions = ArrayList<String>()

        val permissionStorage = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        if (permissionStorage != isGranted) {
            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }

        if (permissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                permissions.toTypedArray(),
                REQUEST_PERMISSIONS
            )
        }
    }

    private fun setupMenu(destination: NavDestination, bundle: Bundle?) {
        if (::mainMenu.isInitialized) {
            mainMenu.findItem(R.id.itemSearch).isVisible =
                destination == mainNavController.graph[R.id.navigation_products]

            mainMenu.findItem(R.id.itemExport).isVisible =
                destination == mainNavController.graph[R.id.navigation_products] &&
                        ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                        PackageManager.PERMISSION_GRANTED

            mainMenu.findItem(R.id.itemImport).isVisible =
                destination == mainNavController.graph[R.id.navigation_products]

            mainMenu.findItem(R.id.itemDelete).isVisible =
                (destination == mainNavController.graph[R.id.navigation_product] && bundle?.getParcelable<Product>("argProduct") != null) ||
                        (destination == mainNavController.graph[R.id.navigation_store] && bundle?.getParcelable<Store>("argStore") != null)
        }
    }

    private fun showSearchBar() {
        searchView.visibility = View.VISIBLE
        searchBar.isVisible = true
        searchIcon.isVisible = false
        searchView.queryHint = getString(R.string.search_hint)
        applicationContext.showKeyboard(searchView)
    }

    private fun hideSearchBar() {
        searchView.run {
            setQuery("", false)
            clearFocus()
            visibility = View.GONE
        }
        searchBar.isVisible = false
        searchIcon.isVisible = true
    }

    override fun setOnDeleteItemClick(func: () -> Unit) {
        onDeleteItemClick = func
    }

    override fun setOnSearchItemClick(func: (String) -> Unit) {
        onSearchItemClick = func
    }

    override fun setOnExportItemClick(func: () -> Unit) {
        onExportItemClick = func
    }

    override fun setOnImportItemClick(func: () -> Unit) {
        onImportItemClick = func
    }

    override fun resetOnDeleteItemClick() {
        onDeleteItemClick = null
    }

    override fun resetOnSearchItemClick() {
        onSearchItemClick = null
    }

    override fun resetOnExportItemClick() {
        onExportItemClick = null
    }

    override fun resetOnImportItemClick() {
        onImportItemClick = null
    }

}
