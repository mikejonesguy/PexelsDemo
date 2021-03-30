package com.mike.pexelsdemo.ui.photoslist

import android.util.Log
import androidx.annotation.MainThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mike.pexelsdemo.data.PexelsDataSource
import com.mike.pexelsdemo.model.Photo
import com.mike.pexelsdemo.model.PhotosResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

@HiltViewModel
class PhotosListViewModel @Inject constructor(private val data: PexelsDataSource) : ViewModel() {

    // tracks whether we should show a progress bar
    private val _liveBusy: MutableLiveData<Boolean> = MutableLiveData()
    val liveBusy: LiveData<Boolean> get() = _liveBusy

    // tracks whether an error occurred
    private val _liveError: MutableLiveData<Throwable?> = MutableLiveData()
    val liveError: LiveData<Throwable?> get() = _liveError

    // tracks our list of photos
    private val _livePhotos: MutableLiveData<List<Photo>> = MutableLiveData()
    val livePhotos: LiveData<List<Photo>> get() = _livePhotos

    // the current search query from user input
    var searchQuery: String? = null

    // our RxJava disposables (to be cleared when the view model is cleared)
    private val disposables = CompositeDisposable()

    // the latest in-flight request
    private var lastFetch: Disposable? = null

    // the latest response from the API
    private var lastResponse: PhotosResponse? = null

    override fun onCleared() {
        super.onCleared()
        disposables.clear()
    }

    /**
     * Clears the error value after processing (so that we don't reprocess it on activity re-creation)
     */
    fun clearError() {
        _liveError.value = null
    }

    /**
     * Fetches [Photo]s, either from the curated or the search API, depending on whether a
     * non-empty search query has been provided
     */
    @MainThread
    fun fetchPhotos(page: Int = 1) {
        // only allow one request at a time
        if (lastFetch?.isDisposed == false) return

        // choose the API -- search or curated
        val query = searchQuery
        Log.d(TAG, "fetchPhotos: page=$page; query=$query")
        val op = if (query.isNullOrEmpty()) {
            data.getCuratedPhotos(page)
        } else {
            data.getSearchResults(page, query)
        }

        _liveBusy.value = page == 1 // show progress for page 1 only
        val rx = op.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doFinally {
                lastFetch = null
                _liveBusy.postValue(false)
            }
            .subscribe(
                { processResponse(it) },
                { error ->
                    Log.w(TAG, "fetchPhotos failed: page=$page; query=$query", error)
                    _liveError.value = error
                }
            )

        lastFetch = rx
        disposables.add(rx)
    }

    /**
     * Processes the [PhotosResponse] from the API and updates the live data value with a merged
     * list of all the data from each page since page 1. Also adds a "loading" item at the end
     * of the list if more pages are available.
     */
    @MainThread
    private fun processResponse(response: PhotosResponse) {
        lastResponse = response

        // preserve the existing list when requesting additional pages (page > 1)
        val merged = mutableListOf<Photo>()
        if (response.page > 1) {
            // use the existing items from the live data; remove any "loading" items
            merged.addAll(_livePhotos.value?.filter { !it.isLoading() } ?: emptyList())
        }

        // append the values from the response to the end of the existing list
        merged.addAll(response.photos)

        // if we have more pages available, add a dummy "loading" item at the end
        if (hasMore()) merged.add(Photo.loadingPhoto)

        // notify the view of the updated list
        _livePhotos.value = merged
    }

    /**
     * Called when the search query string changes
     */
    fun searchPhotos(query: String) {
        // cancel the previous search request if still in-flight
        lastFetch?.dispose()
        lastFetch = null
        searchQuery = query
        fetchPhotos(1)
    }

    /**
     * Fetches the next page of results (if available)
     */
    fun fetchMore() {
        if (!hasMore()) return
        val lastPage = lastResponse?.page ?: 0
        fetchPhotos(lastPage + 1)
    }

    /**
     * Helper method to determine if we have more pages available to fetch
     */
    private fun hasMore(): Boolean {
        val response = lastResponse ?: return true
        return !response.nextPage.isNullOrEmpty()
    }

    companion object {
        private val TAG = PhotosListViewModel::class.simpleName
    }

}