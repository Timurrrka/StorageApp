package ru.musintimur.storageapp.app

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager

fun Context.showKeyboard(view: View) {
    if (view.requestFocus()) {
        (this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
            .showSoftInput(view.findFocus(), InputMethodManager.SHOW_IMPLICIT)
    }
}

fun Context.hideKeyboard(view: View) {
    (this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
        .hideSoftInputFromWindow(view.windowToken, 0)
}