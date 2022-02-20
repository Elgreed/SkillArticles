package ru.skillbranch.skillarticles.extensions

import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.marginBottom
import androidx.core.view.marginEnd
import androidx.core.view.marginStart
import androidx.core.view.marginTop

fun View.setMarginOptionally(start:Int = this.marginStart,
                             top:Int = this.marginTop,
                             end:Int = this.marginEnd,
                             bottom:Int = this.marginBottom,
                            ){
    val lp = this.layoutParams as CoordinatorLayout.LayoutParams
    lp.marginStart = start
    lp.topMargin = top
    lp.marginEnd = end
    lp.bottomMargin = bottom
    this.layoutParams = lp

}

fun View.setPaddingOptionally(start:Int = this.paddingStart,
                             top:Int = this.paddingTop,
                             end:Int = this.paddingEnd,
                             bottom:Int = this.paddingBottom,
){
    this.setPadding(start,top,end,bottom)
}