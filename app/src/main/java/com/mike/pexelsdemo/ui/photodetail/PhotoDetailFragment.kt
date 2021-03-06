package com.mike.pexelsdemo.ui.photodetail

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.github.piasy.biv.loader.ImageLoader
import com.github.piasy.biv.view.BigImageView
import com.github.piasy.biv.view.FrescoImageViewFactory
import com.github.piasy.biv.view.ImageShownCallback
import com.mike.pexelsdemo.R
import com.mike.pexelsdemo.databinding.FragmentPhotoDetailBinding
import com.mike.pexelsdemo.helper.SnackbarHelper
import com.mike.pexelsdemo.helper.ViewHelper.setVisible
import com.mike.pexelsdemo.model.Photo
import java.io.File

/**
 * Displays a [Photo] image full-size in a [BigImageView]
 */
class PhotoDetailFragment : Fragment() {

    private val item: Photo? by lazy { Photo.fromJson(arguments?.getString(ARG_PHOTO)) }
    private var binding: FragmentPhotoDetailBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPhotoDetailBinding.inflate(inflater)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupImageView()
        showImage()
    }

    override fun onDestroy() {
        binding?.photoDetailImage?.cancel()
        binding = null
        super.onDestroy()
    }

    private fun setupImageView() {
        val imageView = binding?.photoDetailImage ?: return

        // for accessibility
        imageView.contentDescription = getString(R.string.content_description_image_full)

        // for image loading via Fresco
        imageView.setImageViewFactory(FrescoImageViewFactory())

        // listen for loader callback events, specifically errors
        imageView.setImageLoaderCallback(object : ImageLoader.Callback {
            override fun onCacheHit(imageType: Int, image: File?) {}
            override fun onCacheMiss(imageType: Int, image: File?) {}
            override fun onStart() {}
            override fun onFinish() {}
            override fun onProgress(progress: Int) {}
            override fun onSuccess(image: File?) {}

            override fun onFail(error: Exception?) {
                Log.w(TAG, "onFail", error)
                if (isDetached) return
                val root = binding?.root ?: return

                // hide the progress bar
                binding?.photoDetailProgress?.hide()

                // inform the user there was a problem
                val parent = root.findViewById<ViewGroup>(R.id.photo_detail_layout)
                SnackbarHelper.showErrorSnackbar(
                    R.string.hi_res_photo_error,
                    parent
                ) { showImage() }
            }
        })

        // listen for image shown callbacks
        imageView.setImageShownCallback(object : ImageShownCallback {
            override fun onThumbnailShown() {
                Log.d(TAG, "thumbnail shown")
            }

            override fun onMainImageShown() {
                // hide the progress bar now that we're displaying the main image
                binding?.photoDetailProgress?.hide()
                Log.d(TAG, "main image shown")
            }
        })
    }

    fun showImage() {
        val imageView = binding?.photoDetailImage ?: return
        val item = item ?: return handleEmpty()
        binding?.emptyView?.setVisible(false)

        val mainImageUrl = item.src?.getBestFullUrl()
        val thumbnailUrl = item.src?.getBestThumbUrl()

        // it's unlikely that either of these urls will be null or empty
        if (!mainImageUrl.isNullOrEmpty()) {
            Log.d(TAG, "full-size image: ${item.width} x ${item.height}")

            // show the progress bar while we load
            binding?.photoDetailProgress?.show()

            if (!thumbnailUrl.isNullOrEmpty()) {
                // show the thumbnail first, since we should have it in cache
                imageView.showImage(Uri.parse(thumbnailUrl), Uri.parse(mainImageUrl))
            } else {
                // no thumbnail, so just show the main image
                imageView.showImage(Uri.parse(mainImageUrl))
            }
        } else {
            // this is an unexpected edge case, otherwise, we'd show an error here
            Log.w(TAG, "showImage: missing full-size url")
        }
    }

    private fun handleEmpty() {
        binding?.photoDetailProgress?.hide()
        binding?.emptyView?.setVisible(true)
    }

    companion object {
        private val TAG = PhotoDetailFragment::class.simpleName
        const val ARG_PHOTO = "photo"
    }
}