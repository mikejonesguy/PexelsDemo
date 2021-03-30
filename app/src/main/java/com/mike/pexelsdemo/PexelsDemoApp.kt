package com.mike.pexelsdemo

import android.app.Application
import com.github.piasy.biv.BigImageViewer
import com.github.piasy.biv.loader.fresco.FrescoImageLoader
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class PexelsDemoApp : Application() {

    override fun onCreate() {
        super.onCreate()

        // Initialize the BigImageViewer library. This also initializes Fresco
        // see: https://github.com/Piasy/BigImageViewer#initialize
        BigImageViewer.initialize(FrescoImageLoader.with(this))
    }
}