package com.mike.pexelsdemo.model

import com.google.gson.annotations.SerializedName

data class PhotosResponse(
    @SerializedName("total_results")
    val totalResults: Int,
    val page: Int,
    @SerializedName("per_page")
    val perPage: Int,
    val photos: List<Photo>,
    @SerializedName("next_page")
    val nextPage: String?,
)

/*
    Sample data for PhotosResponse -- see: https://www.pexels.com/api/documentation/#photos-search
{
    "total_results": 10000,
    "page": 1,
    "per_page": 1,
    "photos": [
        ...
    ],
    "next_page": "https://api.pexels.com/v1/search/?page=2&per_page=1&query=nature"
}
 */