package com.mike.pexelsdemo.injection

import com.mike.pexelsdemo.data.PexelsApi
import com.mike.pexelsdemo.helper.GsonHelper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory

@Module
@InstallIn(ViewModelComponent::class)
object DataModule {

    @Provides
    fun providePexelsApi(): PexelsApi {
        val retrofit = Retrofit.Builder()
            .baseUrl(PexelsApi.BASE_URL)
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonHelper.converterFactory)
            .client(OkHttpClient.Builder().build())
            .build()

        return retrofit.create(PexelsApi::class.java)
    }
}