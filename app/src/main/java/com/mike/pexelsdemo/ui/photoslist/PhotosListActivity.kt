package com.mike.pexelsdemo.ui.photoslist

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.paging.PagedList
import androidx.recyclerview.widget.RecyclerView
import com.mike.pexelsdemo.R
import com.mike.pexelsdemo.helper.SnackbarHelper
import com.mike.pexelsdemo.model.Photo
import com.mike.pexelsdemo.ui.photodetail.PhotoDetailActivity
import com.mike.pexelsdemo.ui.photodetail.PhotoDetailFragment
import dagger.hilt.android.AndroidEntryPoint

/**
 * Displays a list of photos in grid layout, with search available in the menu.
 * On larger devices in landscape mode, the view is split between the list (left)
 * and the detail view (right).
 *
 * Selecting a photo will navigate to a [PhotoDetailActivity] where the image
 * can be shown in full.
 */
@AndroidEntryPoint
class PhotosListActivity : AppCompatActivity() {

    // keep track of whether we're in split view mode
    private var twoPane: Boolean = false

    private lateinit var adapter: PhotosListAdapter
    private lateinit var gridView: RecyclerView
    private lateinit var searchView: SearchView
    private lateinit var progressView: View

    val viewModel: PhotosListViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photos_list)

        if (findViewById<ViewGroup>(R.id.detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp-land).
            twoPane = true
        }

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.title = title

        progressView = findViewById(R.id.horizontal_progress)

        setupRecyclerView(findViewById(R.id.item_list))

        viewModel.livePhotos.observe(this, { onPhotos(it) })
        viewModel.liveError.observe(this, { onError(it) })
        viewModel.liveBusy.observe(this, { onBusy(it) })

        viewModel.fetchPhotos()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_photos_list, menu)

        setupSearchView(menu.findItem(R.id.search_photos))

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.pexels_link -> startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://www.pexels.com")
                )
            )
        }

        return super.onOptionsItemSelected(item)
    }

    private fun setupSearchView(searchItem: MenuItem) {
        this.searchView = searchItem.actionView as SearchView
        searchView.queryHint = getString(R.string.search_photos_hint)

        // check to see if there is an existing query (if the activity was re-created)
        // if there is, populate and expand the search view
        val existingQuery = viewModel.searchQuery
        if (!existingQuery.isNullOrEmpty()) {
            searchItem.expandActionView()
            searchView.isIconified = false
            searchView.setQuery(existingQuery, true)
            searchView.clearFocus()
        }

        // listen for updates to the query text, so that we can make API requests as appropriate
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                searchView.clearFocus()
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.searchPhotos(newText ?: "")
                return false
            }
        })
    }

    private fun setupRecyclerView(recyclerView: RecyclerView) {
        this.gridView = recyclerView

        // setup the click listener for our list items; clicking will display the detail view
        val listener = object : PhotosListAdapter.ItemClickListener {
            override fun onClicked(holder: PhotosListAdapter.ViewHolder) {
                val item = holder.data ?: return
                val activity = this@PhotosListActivity
                if (twoPane) {
                    val fragment = PhotoDetailFragment().apply {
                        arguments = Bundle().apply {
                            putString(PhotoDetailFragment.ARG_PHOTO, item.toJson())
                        }
                    }
                    activity.supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.detail_container, fragment)
                        .commit()
                } else {
                    val intent = Intent(activity, PhotoDetailActivity::class.java).apply {
                        putExtra(PhotoDetailFragment.ARG_PHOTO, item.toJson())
                    }
                    activity.startActivity(intent)
                }
            }
        }

        // when the list nears the end, this callback is fired so that we can load
        // more items if available
        val callback = object : PagedList.BoundaryCallback<Photo>() {
            override fun onItemAtEndLoaded(itemAtEnd: Photo) {
                super.onItemAtEndLoaded(itemAtEnd)
                viewModel.fetchMore()
            }
        }

        // create and set the adapter on the grid view
        this.adapter = PhotosListAdapter(listener, callback)
        gridView.adapter = this.adapter
    }

    private fun onPhotos(items: List<Photo>) {
        // TODO -- handle empty results

        adapter.submitList(items)
    }

    private fun onError(error: Throwable?) {
        error ?: return

        // clear the error value so that it doesn't get re-processed
        viewModel.clearError()

        // notify the user of the problem
        val coordinator = findViewById<CoordinatorLayout>(R.id.coordinator)
        SnackbarHelper.showErrorSnackbar(
            R.string.fetch_photos_error,
            coordinator
        ) { viewModel.fetchMore() }
    }

    private fun onBusy(busy: Boolean) {
        progressView.visibility = if (busy) View.VISIBLE else View.GONE
    }

}