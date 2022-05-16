package ru.skillbranch.skillarticles.ui.custom.spans

import android.graphics.Color
import android.text.style.ForegroundColorSpan
import androidx.core.graphics.ColorUtils

open class SearchSpan() : ForegroundColorSpan(Color.WHITE)//BackgroundColorSpan(bgColor) {
//    private val alpha by lazy {
//        ColorUtils.setAlphaComponent(bgColor, 160)
//    }
//
//    override fun updateDrawState(textPaint: TextPaint) {
//        textPaint.bgColor = alpha
//        textPaint.color = fgColor
//}
//}