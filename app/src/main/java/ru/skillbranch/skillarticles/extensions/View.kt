package ru.skillbranch.skillarticles.extensions

import android.view.View
import android.view.ViewGroup
import androidx.annotation.IdRes
import androidx.core.view.*
import androidx.navigation.NavDestination
import com.google.android.material.bottomnavigation.BottomNavigationView

fun View.setMarginOptionally(
    left: Int = marginLeft,
    top: Int = marginTop,
    right: Int = marginRight,
    bottom: Int = marginBottom
) {
    (layoutParams as? ViewGroup.MarginLayoutParams)?.run {
        leftMargin = left
        rightMargin = right
        topMargin = top
        bottomMargin = bottom
    }
    requestLayout()
}

fun View.setPaddingOptionally(
    left: Int = paddingLeft,
    top: Int = paddingTop,
    right: Int = paddingRight,
    bottom: Int = paddingBottom
) {
    setPadding(left, top, right, bottom)
}