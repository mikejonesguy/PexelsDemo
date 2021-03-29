package com.mike.pexelsdemo.data

import com.mike.pexelsdemo.model.PhotosResponse
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query
import javax.inject.Inject

class PexelsDataSource @Inject constructor(private val api: PexelsApi) {
    fun getCuratedPhotos(page: Int): Single<PhotosResponse> {
        return api.curated(PER_PAGE, page)
    }

    fun getSearchResults(page: Int, query: String): Single<PhotosResponse> {
        return api.search(query, null, null, null, null, PER_PAGE, page)
    }

    companion object {
        private const val PER_PAGE = 30
    }
}

interface PexelsApi {
    /**
     * See: https://www.pexels.com/api/documentation/#photos-search
     */
    @Headers(AUTHORIZATION)
    @GET("v1/search")
    fun search(
        @Query("query") query: String,
        @Query("orientation") orientation: String?,
        @Query("size") size: String?,
        @Query("color") color: String?,
        @Query("locale") locale: String?,
        @Query("per_page") perPage: Int?,
        @Query("page") page: Int?,
    ): Single<PhotosResponse>

    /**
     * See: https://www.pexels.com/api/documentation/#photos-curated
     */
    @Headers(AUTHORIZATION)
    @GET("v1/curated")
    fun curated(
        @Query("per_page") perPage: Int?,
        @Query("page") page: Int?,
    ): Single<PhotosResponse>

    companion object {
        const val BASE_URL = "https://api.pexels.com"
        private const val API_KEY = "563492ad6f917000010000012c52a2c66ffd4a6c94cf22f667bd4381"
        private const val AUTHORIZATION = "Authorization: $API_KEY"
    }
}