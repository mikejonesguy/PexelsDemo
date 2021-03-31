package com.mike.pexelsdemo.testing

import com.mike.pexelsdemo.helper.RxScheduler
import io.reactivex.Scheduler
import io.reactivex.schedulers.Schedulers


/**
 * Test implementation of [RxScheduler] that uses the trampoline (immediate) scheduler for all
 * transformations.
 */
class RxTestScheduler : RxScheduler() {
    override fun cpu(): Scheduler = Schedulers.trampoline()
    override fun io(): Scheduler = Schedulers.trampoline()
    override fun ui(): Scheduler = Schedulers.trampoline()
}