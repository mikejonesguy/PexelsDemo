package com.mike.pexelsdemo.ui.photodetail

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.mike.pexelsdemo.R
import com.mike.pexelsdemo.databinding.ActivityPhotoDetailBinding
import com.mike.pexelsdemo.model.Photo
import com.mike.pexelsdemo.ui.photoslist.PhotosListActivity

/**
 * Displays the [PhotoDetailFragment] for viewing photos full-size
 */
class PhotoDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPhotoDetailBinding

    private val photoJson: String? by lazy { intent.getStringExtra(PhotoDetailFragment.ARG_PHOTO) }
    private val photo: Photo? by lazy { Photo.fromJson(photoJson) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPhotoDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // configure the toolbar
        setSupportActionBar(binding.detailToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // show the photographer in the toolbar subtitle
        val photog = photo?.photographer ?: getString(R.string.unknown)
        binding.detailToolbar.subtitle = getString(R.string.photo_by, photog)

        // savedInstanceState is non-null on fragment re-creation (from orientation change, etc)
        // we only want to add the fragment on the first run, not on config changes
        if (savedInstanceState == null) {
            // create the detail fragment
            val fragment = PhotoDetailFragment().apply {
                // copy the string extra from the intent to the fragment bundle
                arguments = Bundle().apply {
                    putString(PhotoDetailFragment.ARG_PHOTO, photoJson)
                }
            }

            // add the fragment to our activity's view
            supportFragmentManager.beginTransaction()
                .add(R.id.detail_container, fragment)
                .commit()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem) =
        when (item.itemId) {
            android.R.id.home -> {
                // see: http://developer.android.com/design/patterns/navigation.html#up-vs-back
                navigateUpTo(Intent(this, PhotosListActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
}