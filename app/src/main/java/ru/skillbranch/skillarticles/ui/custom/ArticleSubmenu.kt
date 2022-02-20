package ru.skillbranch.skillarticles.ui.custom

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View
import android.view.ViewAnimationUtils
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.Px
import androidx.annotation.VisibleForTesting
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import androidx.core.view.isVisible
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.switchmaterial.SwitchMaterial
import ru.skillbranch.skillarticles.R
import ru.skillbranch.skillarticles.extensions.attrValue
import ru.skillbranch.skillarticles.extensions.dpToIntPx
import ru.skillbranch.skillarticles.extensions.setPaddingOptionally
import ru.skillbranch.skillarticles.ui.custom.behaviors.SubmenuBehavior
import kotlin.math.hypot
import kotlin.math.roundToInt

class ArticleSubmenu @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ViewGroup(context, attrs, defStyleAttr), CoordinatorLayout.AttachedBehavior {
    //settings
    @Px
    private val menuWidth = context.dpToIntPx(200)

    @Px
    private val menuHeight = context.dpToIntPx(96)

    @Px
    private val btnHeight = context.dpToIntPx(40)

    @Px
    private val btnWidth = context.dpToIntPx(100)

    @Px
    private val defaultPadding = context.dpToIntPx(16)

    @ColorInt
    private var lineColor: Int = context.getColor(R.color.color_divider)

    @ColorInt
    private val textColor = context.attrValue(R.attr.colorOnSurface, true)
    private val iconTint = context.getColorStateList(R.color.tint_color)

    @DrawableRes
    private val bg = context.attrValue(R.attr.selectableItemBackground, needRes = true)

    //views
    val btnTextDown: CheckableImageView
    val btnTextUp: CheckableImageView
    val switchMode: SwitchMaterial
    val tvLabel: TextView

    var isOpen = false

    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = lineColor
        strokeWidth = 0f
    }

    init {
        val materialBg = MaterialShapeDrawable.createWithElevationOverlay(context)
        materialBg.elevation = elevation
        background = materialBg
        val topBottomPaddingDown = context.dpToIntPx(12)
        val topBottomPaddingUp = context.dpToIntPx(8)
        btnTextDown = CheckableImageView(context).apply {
            setImageResource(R.drawable.ic_title_black_24dp)
            setBackgroundColor(bg)
            imageTintList = iconTint
            setPaddingOptionally(top = topBottomPaddingDown, bottom = topBottomPaddingDown)
        }
        addView(btnTextDown)
        btnTextUp = CheckableImageView(context).apply {
            setPaddingOptionally(top = topBottomPaddingUp, bottom = topBottomPaddingUp)
            setImageResource(R.drawable.ic_title_black_24dp)
            setBackgroundColor(bg)
            imageTintList = iconTint
        }
        addView(btnTextUp)
        switchMode = SwitchMaterial(context).apply {

        }
        addView(switchMode)
        tvLabel = TextView(context).apply {
            text = resources.getString(R.string.dark_mode_label)
            this.setTextColor(textColor)
        }
        addView(tvLabel)
    }

    override fun getBehavior(): CoordinatorLayout.Behavior<ArticleSubmenu> {
        return SubmenuBehavior()
    }

    fun open() {
        if (isOpen || !isAttachedToWindow) return
        isOpen = true
        animatedShow()
    }

    fun close() {
        if (!isOpen || !isAttachedToWindow) return
        isOpen = false
        animatedHide()
    }

    private fun animatedShow() {
        val endRadius = hypot(menuWidth.toDouble(), menuHeight.toDouble()).toInt()
        val anim = ViewAnimationUtils.createCircularReveal(
            this,
            menuWidth,
            menuHeight,
            0f,
            endRadius.toFloat()
        )
        anim.doOnStart {
            visibility = View.VISIBLE
        }
        anim.start()
    }

    private fun animatedHide() {
        val endRadius = hypot(menuWidth.toDouble(), menuHeight.toDouble()).toInt()
        val anim = ViewAnimationUtils.createCircularReveal(
            this,
            menuWidth,
            menuHeight,
            endRadius.toFloat(),
            0f
        )
        anim.doOnEnd {
            visibility = View.GONE
        }
        anim.start()
    }

    //save state
    override fun onSaveInstanceState(): Parcelable {
        val savedState = SavedState(super.onSaveInstanceState())
        savedState.ssIsOpen = isOpen
        return savedState
    }

    //restore state
    override fun onRestoreInstanceState(state: Parcelable) {
        super.onRestoreInstanceState(state)
        if (state is SavedState) {
            isOpen = state.ssIsOpen
            isVisible = isOpen
        }
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    public override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val wms = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
        val hms = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
        tvLabel.measure(wms, hms)
        switchMode.measure(wms, hms)
        setMeasuredDimension(menuWidth, menuHeight)
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    public override fun onLayout(p0: Boolean, l: Int, t: Int, r: Int, b: Int) {
        var usedHeight = paddingTop
        btnTextDown.layout(
            paddingLeft,
            usedHeight,
            btnWidth,
            usedHeight + btnHeight
        )
        btnTextUp.layout(
            btnWidth,
            usedHeight,
            menuWidth - paddingRight,
            usedHeight + btnHeight
        )
        usedHeight += btnHeight
        tvLabel.layout(
            context.dpToIntPx(16) + paddingLeft,
            ((menuHeight - paddingBottom + btnHeight - tvLabel.measuredHeight) / 2f).roundToInt(),
            paddingLeft + context.dpToIntPx(16) + tvLabel.measuredWidth,
            (menuHeight - paddingBottom + btnHeight) / 2 + tvLabel.measuredHeight / 2
        )

        switchMode.layout(
            menuWidth - paddingRight - switchMode.measuredWidth - context.dpToIntPx(16),
            ((menuHeight - paddingBottom + btnHeight - switchMode.measuredHeight) / 2f).roundToInt(),
            menuWidth - paddingRight - context.dpToIntPx(16),
            (menuHeight - paddingBottom + btnHeight + switchMode.measuredHeight) / 2
        )
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    public override fun dispatchDraw(canvas: Canvas) {
        super.dispatchDraw(canvas)
        canvas.drawLine(
            menuWidth / 2f,
            0f,
            menuWidth / 2f,
            paddingTop + btnHeight.toFloat(),
            linePaint
        )
        canvas.drawLine(
            0f,
            paddingTop + btnHeight.toFloat(),
            menuWidth.toFloat(),
            paddingTop + btnHeight.toFloat(),
            linePaint
        )

    }

    private class SavedState : BaseSavedState, Parcelable {
        var ssIsOpen: Boolean = false

        constructor(superState: Parcelable?) : super(superState)

        constructor(src: Parcel) : super(src) {
            ssIsOpen = src.readInt() == 1
        }

        override fun writeToParcel(dst: Parcel, flags: Int) {
            super.writeToParcel(dst, flags)
            dst.writeInt(if (ssIsOpen) 1 else 0)
        }

        override fun describeContents() = 0

        companion object CREATOR : Parcelable.Creator<SavedState> {
            override fun createFromParcel(parcel: Parcel) = SavedState(parcel)
            override fun newArray(size: Int): Array<SavedState?> = arrayOfNulls(size)
        }
    }

}