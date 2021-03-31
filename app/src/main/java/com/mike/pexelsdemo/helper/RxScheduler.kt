@file:Suppress("unused")

package com.mike.pexelsdemo.helper

import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

/**
 * Helper class for managing RxJava schedulers (enables substitution during testing)
 */
open class RxScheduler @Inject constructor() {
    open fun cpu(): Scheduler = Schedulers.computation()
    open fun io(): Scheduler = Schedulers.io()
    open fun ui(): Scheduler = AndroidSchedulers.mainThread()
}