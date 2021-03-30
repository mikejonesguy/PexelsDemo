package com.mike.pexelsdemo.helper

import android.view.View

object ViewHelper {
    fun View.setVisible(visible: Boolean) {
        visibility = if (visible) View.VISIBLE else View.GONE
    }
}