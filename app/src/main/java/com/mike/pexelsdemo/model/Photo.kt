package com.mike.pexelsdemo.model

import com.google.gson.annotations.SerializedName
import com.mike.pexelsdemo.helper.GsonHelper

data class Photo(
    val id: Int,
    val width: Int,
    val height: Int,
    val url: String,
    val photographer: String?,
    @SerializedName("photographer_url")
    val photographerUrl: String?,
    @SerializedName("photographer_id")
    val photographerId: String?,
    @SerializedName("avg_color")
    val avgColor: String?,
    val src: PhotoSrc?,
    val liked: Boolean?,
) {

    fun isLoading() = this.id == LOADING_ID
    fun toJson(): String = GsonHelper.toJson(this)

    companion object {
        const val LOADING_ID = -1

        val loadingPhoto = Photo(LOADING_ID, 0, 0, "", null, null, null, null, null, null)

        fun fromJson(json: String?): Photo? {
            return GsonHelper.tryParseJson(json, Photo::class.java)
        }
    }
}

/*
    Sample data for Photo model - see: https://www.pexels.com/api/documentation/#photos-overview
{
    "id": 2014422,
    "width": 3024,
    "height": 3024,
    "url": "https://www.pexels.com/photo/brown-rocks-during-golden-hour-2014422/",
    "photographer": "Joey Farina",
    "photographer_url": "https://www.pexels.com/@joey",
    "photographer_id": 680589,
    "avg_color": "#978E82",
    "src": {
        "original": "https://images.pexels.com/photos/2014422/pexels-photo-2014422.jpeg",
        "large2x": "https://images.pexels.com/photos/2014422/pexels-photo-2014422.jpeg?auto=compress&cs=tinysrgb&dpr=2&h=650&w=940",
        "large": "https://images.pexels.com/photos/2014422/pexels-photo-2014422.jpeg?auto=compress&cs=tinysrgb&h=650&w=940",
        "medium": "https://images.pexels.com/photos/2014422/pexels-photo-2014422.jpeg?auto=compress&cs=tinysrgb&h=350",
        "small": "https://images.pexels.com/photos/2014422/pexels-photo-2014422.jpeg?auto=compress&cs=tinysrgb&h=130",
        "portrait": "https://images.pexels.com/photos/2014422/pexels-photo-2014422.jpeg?auto=compress&cs=tinysrgb&fit=crop&h=1200&w=800",
        "landscape": "https://images.pexels.com/photos/2014422/pexels-photo-2014422.jpeg?auto=compress&cs=tinysrgb&fit=crop&h=627&w=1200",
        "tiny": "https://images.pexels.com/photos/2014422/pexels-photo-2014422.jpeg?auto=compress&cs=tinysrgb&dpr=1&fit=crop&h=200&w=280"
    },
    "liked": false
}
 */

