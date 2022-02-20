package ru.skillbranch.skillarticles.ui.custom.spans

import android.graphics.Canvas
import android.graphics.Paint
import android.text.Layout
import android.text.style.LeadingMarginSpan
import androidx.annotation.ColorInt
import androidx.annotation.Px
import ru.skillbranch.skillarticles.extensions.getLineBottomWithoutPadding

class UnorderedListSpan(
    @Px
    private val gapWidth: Float,
    @Px
    private val bulletRadius: Float,
    @ColorInt
    private val bulletColor: Int
) : LeadingMarginSpan {
    override fun getLeadingMargin(first: Boolean): Int {

        return (4*bulletRadius + gapWidth).toInt()
    }

    override fun drawLeadingMargin(
        c: Canvas?,
        p: Paint?,
        x: Int,
        dir: Int,
        top: Int,
        baseline: Int,
        bottom: Int,
        text: CharSequence?,
        start: Int,
        end: Int,
        first: Boolean,
        layout: Layout?
    ) {
        if (first){
            p?.withCustomColor {
                c?.drawCircle(gapWidth + x + bulletRadius,
                    (top + layout?.getLineBottomWithoutPadding(layout.getLineForOffset(start))!!)/2f,
                    bulletRadius,
                    p)
            }
        }
    }
    private inline fun Paint.withCustomColor(block : () -> Unit){
        val oldColor = color
        val oldStyle = style
        color = bulletColor
        style = Paint.Style.FILL
        block()
        color = oldColor
        style = oldStyle
    }

}