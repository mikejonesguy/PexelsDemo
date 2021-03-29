package com.mike.pexelsdemo.ui.photodetail

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.mike.pexelsdemo.R
import com.mike.pexelsdemo.helper.GsonHelper
import com.mike.pexelsdemo.model.Photo
import com.mike.pexelsdemo.ui.photoslist.PhotosListActivity

/**
 * Displays the [PhotoDetailFragment] for viewing photos full-size
 */
class PhotoDetailActivity : AppCompatActivity() {

    private val item: Photo? by lazy {
        GsonHelper.tryParseJson(intent.getStringExtra(PhotoDetailFragment.ARG_PHOTO), Photo::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo_detail)

        val toolbar: Toolbar = findViewById(R.id.detail_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.subtitle = getString(R.string.photo_by, item?.photographer ?: "unknown")

        // savedInstanceState is non-null on fragment re-creation (from orientation change, etc)
        // we only want to add the fragment on the first run, not on config changes
        if (savedInstanceState == null) {
            // create the detail fragment
            val fragment = PhotoDetailFragment().apply {
                // copy the string extra from the intent to the fragment bundle
                arguments = Bundle().apply {
                    putString(
                        PhotoDetailFragment.ARG_PHOTO,
                        intent.getStringExtra(PhotoDetailFragment.ARG_PHOTO)
                    )
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