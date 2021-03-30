package com.mike.pexelsdemo.ui.photoslist

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.paging.PagedList
import androidx.recyclerview.widget.LinearLayoutManager
import com.mike.pexelsdemo.R
import com.mike.pexelsdemo.databinding.ActivityPhotosListBinding
import com.mike.pexelsdemo.helper.SnackbarHelper
import com.mike.pexelsdemo.helper.ViewHelper.setVisible
import com.mike.pexelsdemo.model.Photo
import com.mike.pexelsdemo.ui.photodetail.PhotoDetailActivity
import com.mike.pexelsdemo.ui.photodetail.PhotoDetailFragment
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import java.util.concurrent.TimeUnit

/**
 * Displays a list of [Photo]s in grid layout, with search available in the menu.
 * On larger devices in landscape mode, the view is split between the list (left)
 * and the detail view (right).
 *
 * Selecting a photo will navigate to a [PhotoDetailActivity] where the image
 * can be shown in full.
 */
@AndroidEntryPoint
class PhotosListActivity : AppCompatActivity() {

    val viewModel: PhotosListViewModel by viewModels()

    // keep track of whether we're in split view mode
    private var splitView: Boolean = false

    private lateinit var binding: ActivityPhotosListBinding
    private lateinit var adapter: PhotosListAdapter
    private lateinit var searchView: SearchView

    private val disposables = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPhotosListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // The detail container view will be present only in the
        // large-screen layouts (res/values-w900dp-land).
        splitView = findViewById<ViewGroup>(R.id.detail_container) != null

        // setup views
        setSupportActionBar(binding.toolbar)
        binding.toolbar.title = title
        setupRecyclerView()

        // observe the view model
        viewModel.livePhotos.observe(this, { onPhotos(it) })
        viewModel.liveError.observe(this, { onError(it) })
        viewModel.liveBusy.observe(this, { onBusy(it) })

        // fetch the photos
        viewModel.fetchPhotos()
    }

    override fun onDestroy() {
        disposables.clear()
        super.onDestroy()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_photos_list, menu)
        setupSearchView(menu.findItem(R.id.search_photos))
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.pexels_link -> {
                // Pexels attribution - see: https://www.pexels.com/api/documentation/#guidelines
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(PEXELS_URL)))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
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

        // listen for updates to the search view query text, using RxJava to debounce the queries
        val queryRx = Observable.create<String> { emit ->
            searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    // we already queried the API, so just close the keyboard here
                    searchView.clearFocus()
                    return false
                }

                override fun onQueryTextChange(query: String?): Boolean {
                    // emit the update to our subscriber
                    emit.onNext(query ?: "")
                    return false
                }
            })
        }

        // use RxJava to avoid API spamming (esp. with an attached keyboard)
        // only the last query is honored (in between queries are dropped).
        disposables.add(queryRx.debounce(300L, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { query -> viewModel.searchPhotos(query ?: "") }
        )
    }

    private fun setupRecyclerView() {
        val gridView = binding.photosContainer.photosLayout.photosList

        // setup the click listener for our list items; clicking will display the detail view
        val listener = object : PhotosListAdapter.ItemClickListener {
            override fun onClicked(holder: PhotosListAdapter.ViewHolder) {
                val item = holder.data ?: return
                val activity = this@PhotosListActivity
                if (splitView) {
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
        // handle empty search results
        val emptySearch = items.isEmpty() && !viewModel.searchQuery.isNullOrEmpty()
        binding.photosContainer.photosLayout.emptyView.setVisible(emptySearch)

        // check to see if our top item has changed. if so, we should scroll the list to top
        val existingItem = adapter.currentList.getOrNull(0)
        val newItem = items.getOrNull(0)
        val shouldScrollToTop = existingItem?.id != newItem?.id

        // pause boundary events if we need to scroll to top. this prevents us firing
        // off premature requests for the next page due to transition scrolling
        adapter.pauseCallback.set(shouldScrollToTop)

        // update the adapter with the new list. ListAdapter will automatically handle
        // animations for inserts, moves, deletes, etc.
        adapter.submitList(items) {
            if (!shouldScrollToTop) return@submitList // no-op early return

            // scroll to top (on the next run loop) and un-pause the boundary callback
            val gridView = binding.photosContainer.photosLayout.photosList
            val layoutManager = gridView.layoutManager as LinearLayoutManager
            gridView.post {
                layoutManager.scrollToPosition(0)
                adapter.pauseCallback.set(false)
            }
        }
    }

    private fun onError(error: Throwable?) {
        error ?: return

        // clear the error value so that it doesn't get re-processed
        viewModel.clearError()

        // notify the user of the problem
        SnackbarHelper.showErrorSnackbar(
            R.string.fetch_photos_error,
            binding.coordinator
        ) { viewModel.fetchMore() }
    }

    private fun onBusy(busy: Boolean) {
        val bar = binding.progressLayout.horizontalProgressBar
        if (busy) bar.show() else bar.hide()
    }

    @Suppress("unused")
    companion object {
        private val TAG = PhotosListActivity::class.simpleName
        private const val PEXELS_URL = "https://www.pexels.com"
    }

}