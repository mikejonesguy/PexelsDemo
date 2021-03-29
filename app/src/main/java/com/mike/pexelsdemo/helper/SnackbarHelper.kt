package com.mike.pexelsdemo.helper

import android.graphics.Color
import android.view.View
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.mike.pexelsdemo.R

object SnackbarHelper {

    /**
     * Show a customized [Snackbar] with a "Retry" action
     */
    fun showErrorSnackbar(@StringRes messageId: Int, parent: View, action: (() -> Unit)): Snackbar {
        val duration = 30000
        val snackbar = Snackbar.make(parent, messageId, duration)
        snackbar.setAction(R.string.retry) { action.invoke() }
        snackbar.isGestureInsetBottomIgnored = true
        snackbar.setActionTextColor(ContextCompat.getColor(parent.context, R.color.teal_100))
        snackbar.view.setBackgroundColor(ContextCompat.getColor(parent.context, R.color.teal_900))
        val textView = snackbar.view.findViewById<TextView>(R.id.snackbar_text)
        textView.setTextColor(Color.WHITE)
        snackbar.show()

        return snackbar
    }
}