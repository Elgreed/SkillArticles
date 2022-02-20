package ru.skillbranch.skillarticles.extensions

import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.marginBottom
import androidx.core.view.marginLeft
import androidx.core.view.marginRight
import androidx.core.view.marginTop

fun View.setMarginOptionally(left : Int = marginLeft, top : Int = marginTop, right : Int = marginRight, bottom : Int = marginBottom) {
    val coordinatorLayoutParams = layoutParams as CoordinatorLayout.LayoutParams
    coordinatorLayoutParams.setMargins(left, top, right, bottom)
}

fun View.setPaddingOptionally(left : Int = paddingLeft, top : Int = paddingTop, right : Int = paddingRight, bottom : Int = paddingBottom) {
    setPadding(left, top, right, bottom)
}