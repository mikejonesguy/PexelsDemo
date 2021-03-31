package com.mike.pexelsdemo.ui.photoslist

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.mike.pexelsdemo.data.PexelsDataSource
import com.mike.pexelsdemo.model.Photo
import com.mike.pexelsdemo.testing.LifeCycleTestOwner
import com.mike.pexelsdemo.testing.MockData
import com.mike.pexelsdemo.testing.RxTestScheduler
import com.nhaarman.mockitokotlin2.*
import io.reactivex.Single
import org.junit.*
import org.junit.rules.TestRule


class PhotosListViewModelTest {
    @get:Rule var rule: TestRule = InstantTaskExecutorRule()

    private lateinit var owner: LifeCycleTestOwner
    private lateinit var viewModel: PhotosListViewModel

    private val dataSource: PexelsDataSource = mock {  }
    private val photosObserver: Observer<List<Photo>> = mock {  }
    private val busyObserver: Observer<Boolean> = mock {  }
    private val errorObserver: Observer<Throwable?> = mock {  }

    @Before
    fun setup() {
        // create the life cycle owner
        owner = LifeCycleTestOwner()
        owner.onCreate()

        // create the view model
        viewModel = PhotosListViewModel(dataSource, RxTestScheduler())

        // observe live data updates
        viewModel.livePhotos.observe(owner, photosObserver)
        viewModel.liveBusy.observe(owner, busyObserver)
        viewModel.liveError.observe(owner, errorObserver)
    }

    @After
    fun tearDown() {
        owner.onDestroy()
    }

    @Test
    fun `fetch photos should add a 'loading' item if hasMore == true`() {
        // given
        owner.onResume()
        val response = MockData.randomPhotosResponse(30)
        val processed = response.photos.toMutableList().apply { add(Photo.loadingPhoto) }
        whenever(dataSource.getCuratedPhotos(1)).doReturn(Single.just(response))
        // when
        viewModel.fetchPhotos(1)
        // then
        Assert.assertTrue(viewModel.hasMore())
        verifyHappyPath(processed)
    }

    @Test
    fun `fetch photos should NOT add a 'loading' item if hasMore == false`() {
        // given
        owner.onResume()
        val response = MockData.randomPhotosResponse(30, hasNext = false)
        val processed = response.photos.toMutableList()//.apply { add(Photo.loadingPhoto) }
        whenever(dataSource.getCuratedPhotos(1)).doReturn(Single.just(response))
        // when
        viewModel.fetchPhotos(1)
        // then
        Assert.assertFalse(viewModel.hasMore())
        verifyHappyPath(processed)
    }

    @Test
    fun `fetch photos with error response should hasMore == true`() {
        // given
        owner.onResume()
        whenever(dataSource.getCuratedPhotos(1)).doReturn(Single.error(RuntimeException("fake error")))
        // when
        viewModel.fetchPhotos(1)
        // then
        Assert.assertTrue(viewModel.hasMore())
        verifySadPath()
    }

    @Test
    fun `search photos should call the dataSource search method`() {
        // given
        owner.onResume()
        val query = "test query"
        val response = MockData.randomPhotosResponse(30)
        val processed = response.photos.toMutableList().apply { add(Photo.loadingPhoto) }
        whenever(dataSource.getSearchResults(1, query)).doReturn(Single.just(response))
        // when
        viewModel.searchPhotos(query)
        // then
        Assert.assertTrue(viewModel.hasMore())
        verifyHappyPath(processed, query = query)
    }

    private fun verifyHappyPath(list: List<Photo>, page: Int = 1, query: String? = null) {
        // post in order: data.get(), busy=true, photos=list, busy=false; no errors
        val inOrder = inOrder(dataSource, busyObserver, photosObserver, busyObserver)
        if (query.isNullOrEmpty()) {
            inOrder.verify(dataSource).getCuratedPhotos(page)
        } else {
            inOrder.verify(dataSource).getSearchResults(page, query)
        }
        inOrder.verify(busyObserver).onChanged(true)
        inOrder.verify(photosObserver).onChanged(list)
        inOrder.verify(busyObserver).onChanged(false)

        // no errors
        verifyZeroInteractions(errorObserver)
    }

    private fun verifySadPath(page: Int = 1, query: String? = null) {
        // post in order: data.get(), busy=true, error=any, busy=false
        val inOrder = inOrder(dataSource, busyObserver, errorObserver, busyObserver)
        if (query.isNullOrEmpty()) {
            inOrder.verify(dataSource).getCuratedPhotos(page)
        } else {
            inOrder.verify(dataSource).getSearchResults(page, query)
        }
        inOrder.verify(busyObserver).onChanged(true)
        inOrder.verify(errorObserver).onChanged(any())
        inOrder.verify(busyObserver).onChanged(false)

        // no photos
        verifyZeroInteractions(photosObserver)
    }
}