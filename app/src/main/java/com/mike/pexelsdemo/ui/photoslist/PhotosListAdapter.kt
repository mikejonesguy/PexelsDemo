package com.mike.pexelsdemo.ui.photoslist

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.paging.PagedList
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mike.pexelsdemo.R
import com.mike.pexelsdemo.model.Photo
import kotlin.math.roundToInt

class PhotosListAdapter(
    private val listener: ItemClickListener,
    private val boundaryCallback: PagedList.BoundaryCallback<Photo>
) : ListAdapter<Photo, PhotosListAdapter.ViewHolder>(adapterDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_photo, parent, false)
        view.layoutParams.height = (parent.measuredWidth.toDouble() / 3.0).roundToInt()
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)

        // notify the activity that we are nearing the end of the list
        if (position >= itemCount - PREFETCH) boundaryCallback.onItemAtEndLoaded(item)
    }

    @Suppress("MemberVisibilityCanBePrivate")
    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        init {
            itemView.setOnClickListener { listener.onClicked(this) }
        }

        var data: Photo? = null
        val image: ImageView = view.findViewById(R.id.image)
        val photographer: TextView = view.findViewById(R.id.photographer)
        val loading: ProgressBar = view.findViewById(R.id.loading)

        fun bind(item: Photo) {
            data = item
            photographer.text = item.photographer
            photographer.visibility =
                if (item.photographer.isNullOrEmpty()) View.GONE else View.VISIBLE
            val url = item.src?.getBestThumbUrl()
            image.setImageURI(if (url.isNullOrEmpty()) null else Uri.parse(url))
            loading.visibility = if (data?.isLoading() == true) View.VISIBLE else View.GONE
        }
    }

    interface ItemClickListener {
        fun onClicked(holder: PhotosListAdapter.ViewHolder)
    }

    companion object {
        private const val PREFETCH = 10

        // this callback is used when submitting updates to the list adapter
        private val adapterDiffCallback = object : DiffUtil.ItemCallback<Photo>() {
            override fun areContentsTheSame(oldItem: Photo, newItem: Photo): Boolean {
                return areItemsTheSame(oldItem, newItem) && oldItem == newItem
            }

            override fun areItemsTheSame(oldItem: Photo, newItem: Photo): Boolean {
                return oldItem.id == newItem.id && !oldItem.isLoading()
            }
        }
    }
}