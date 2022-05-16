package ru.skillbranch.skillarticles.ui.custom.markdown

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.os.Parcel
import android.os.Parcelable
import android.text.Spannable
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.Px
import androidx.annotation.VisibleForTesting
import androidx.core.animation.doOnEnd
import androidx.core.graphics.ColorUtils
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation
import ru.skillbranch.skillarticles.R
import ru.skillbranch.skillarticles.extensions.attrValue
import ru.skillbranch.skillarticles.extensions.dpToIntPx
import ru.skillbranch.skillarticles.extensions.dpToPx
import ru.skillbranch.skillarticles.extensions.setPaddingOptionally
import java.nio.charset.Charset
import java.security.MessageDigest
import kotlin.math.hypot

@SuppressLint("ViewConstructor")
class MarkdownImageView private constructor(
    context: Context,
    fontSize: Float
) : ViewGroup(context, null, 0), IMarkdownView {
    //views
    private lateinit var imageUrl: String
    private lateinit var imageTitle: CharSequence

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    val ivImage: ImageView

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    val tvTitle: MarkdownTextView

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    var tvAlt: TextView? = null

    @Px
    private val titleTopMargin: Int = context.dpToIntPx(8)

    @Px
    private val titlePadding: Int = context.dpToIntPx(56)

    @Px
    private val cornerRadius: Float = context.dpToPx(4)

    @ColorInt
    private val colorSurface: Int = context.attrValue(R.attr.colorSurface)

    @ColorInt
    private val colorOnSurface: Int = context.attrValue(R.attr.colorOnSurface)

    @ColorInt
    private val colorOnBackground: Int = context.attrValue(R.attr.colorOnBackground)

    @ColorInt
    private var lineColor: Int = context.getColor(R.color.color_divider)

    //for draw object allocation
    private var linePositionY: Float = 0f
    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = lineColor
        strokeWidth = 0f
    }

    override var fontSize: Float = fontSize
        set(value) {
            tvTitle.textSize = value * 0.75f
            tvAlt?.textSize = value
            field = value
        }

    override val spannableContent: Spannable
        get() = tvTitle.text as Spannable

    init {
        layoutParams =
            LayoutParams(LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT))
        ivImage = ImageView(context).apply {
            outlineProvider = object : ViewOutlineProvider() {
                override fun getOutline(view: View, outline: Outline) {
                    outline.setRoundRect(
                        Rect(0, 0, view.measuredWidth, view.measuredHeight),
                        cornerRadius
                    )
                }
            }
            clipToOutline = true
        }
        addView(ivImage)

        tvTitle = MarkdownTextView(context, fontSize * 0.75f, false).apply {
            setTextColor(colorOnBackground)
            gravity = Gravity.CENTER
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
            setPaddingOptionally(left = titlePadding, right = titlePadding)
        }
        addView(tvTitle)
    }

    constructor(
        context: Context,
        fontSize: Float,
        url: String,
        title: String,
        alt: String?
    ) : this(context, fontSize) {
        imageUrl = url
        imageTitle = title

        tvTitle.setText(title, TextView.BufferType.SPANNABLE)

        Glide
            .with(context)
            .load(imageUrl)
            .transform(AspectRatioResizeTransform())
            .into(ivImage)

        alt?.let {
            tvAlt = TextView(context).apply {
                text = alt
                setTextColor(colorOnSurface)
                setBackgroundColor(ColorUtils.setAlphaComponent(colorSurface, 160))
                gravity = Gravity.CENTER
                textSize = fontSize
                setPaddingOptionally(titleTopMargin)
                isVisible = false
            }
            addView(tvAlt)

            ivImage.setOnClickListener {
                if (tvAlt?.isVisible == true) animateHideAlt()
                else animateShowAlt()
            }
        }
    }

    private fun animateShowAlt() {
        tvAlt?.isVisible = true
        val endRadius = hypot(tvAlt?.width?.toFloat() ?: 0f, tvAlt?.height?.toFloat() ?: 0f)
        val va = ViewAnimationUtils.createCircularReveal(
            tvAlt,
            tvAlt?.width ?: 0,
            tvAlt?.height ?: 0,
            0f,
            endRadius
        )
        va.start()
    }

    private fun animateHideAlt() {

        tvAlt?.isVisible = true
        val endRadius = hypot(tvAlt?.width?.toFloat() ?: 0f, tvAlt?.height?.toFloat() ?: 0f)
        val va = ViewAnimationUtils.createCircularReveal(
            tvAlt,
            tvAlt?.width ?: 0,
            tvAlt?.height ?: 0,
            0f,
            endRadius
        )
        va.doOnEnd { tvAlt?.isVisible = false }
        va.start()
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    override public fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        var usedHeight = 0
        val bodyWidth = r - l - paddingLeft - paddingRight
        val left = paddingLeft
        val right = paddingLeft + bodyWidth

        ivImage.layout(
            left,
            usedHeight,
            right,
            usedHeight + ivImage.measuredHeight
        )

        usedHeight += ivImage.measuredHeight + titleTopMargin

        tvTitle.layout(
            left,
            usedHeight,
            right,
            usedHeight + tvTitle.measuredHeight
        )

        tvAlt?.layout(
            left,
            ivImage.measuredHeight - (tvAlt?.measuredHeight ?: 0),
            right,
            ivImage.measuredHeight
        )
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    override public fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var usedHeight = 0
        val width = View.getDefaultSize(suggestedMinimumWidth, widthMeasureSpec)

        //create measureSpec for children EXACTLY
        //all children width == parent width(constraint parent widhth
        val ms = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY)

        ivImage.measure(ms, heightMeasureSpec)
        tvTitle.measure(ms, heightMeasureSpec)
        tvAlt?.measure(ms, heightMeasureSpec)

        usedHeight += ivImage.measuredHeight
        usedHeight += titleTopMargin
        linePositionY = usedHeight + tvTitle.measuredHeight / 2f
        usedHeight += tvTitle.measuredHeight

        setMeasuredDimension(width, usedHeight)
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    override public fun dispatchDraw(canvas: Canvas) {
        super.dispatchDraw(canvas)
        canvas.drawLine(
            0f,
            linePositionY,
            titlePadding.toFloat(),
            linePositionY,
            linePaint
        )

        canvas.drawLine(
            canvas.width - titlePadding.toFloat(),
            linePositionY,
            canvas.width.toFloat(),
            linePositionY,
            linePaint
        )
    }

    override fun onSaveInstanceState(): Parcelable? {
        val savedState = SavedState(super.onSaveInstanceState())
        if (ivImage.id == NO_ID) ivImage.id = View.generateViewId()
        if (tvTitle.id == NO_ID) tvTitle.id = View.generateViewId()
        if (tvAlt?.id == NO_ID) tvAlt?.id = View.generateViewId()
        savedState.ssIsOpen = tvAlt?.isVisible ?: false
        savedState.ssImageId = ivImage.id
        savedState.ssTitleId = tvTitle.id
        savedState.ssAltId = tvAlt?.id ?: 0
        return savedState
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        super.onRestoreInstanceState(state)
        if (state is SavedState) {
            tvAlt?.isVisible = state.ssIsOpen
            ivImage.id = state.ssImageId
            tvTitle.id = state.ssTitleId
            tvAlt?.id = state.ssAltId
            Log.d(
                "M_MarkdownImageView",
                "Restored ImageId=${ivImage.id}, TitleId=${tvTitle.id}, AltId=${tvAlt?.id}"
            )
        }
    }

    private class SavedState : BaseSavedState, Parcelable {
        var ssIsOpen: Boolean = false
        var ssImageId = 0
        var ssTitleId = 0
        var ssAltId = 0

        constructor(superState: Parcelable?) : super(superState)

        constructor(src: Parcel) : super(src) {
            ssIsOpen = src.readInt() == 1
            ssImageId = src.readInt()
            ssTitleId = src.readInt()
            ssAltId = src.readInt()
        }

        override fun writeToParcel(dst: Parcel, flags: Int) {
            super.writeToParcel(dst, flags)
            dst.writeInt(if (ssIsOpen) 1 else 0)
            dst.writeInt(ssImageId)
            dst.writeInt(ssTitleId)
            dst.writeInt(ssAltId)
        }

        override fun describeContents() = 0

        companion object CREATOR : Parcelable.Creator<SavedState> {
            override fun createFromParcel(parcel: Parcel) = SavedState(parcel)
            override fun newArray(size: Int): Array<SavedState?> = arrayOfNulls(size)
        }
    }
}

class AspectRatioResizeTransform : BitmapTransformation() {
    private val ID = "ru.skillbranch.skillarticles.glide.AspectRatioResizeTransform"
    private val ID_BYTES = ID.toByteArray(
        Charset.forName("UTF-8")
    )

    override fun updateDiskCacheKey(messageDigest: MessageDigest) {
        messageDigest.update(ID_BYTES)
    }

    override fun transform(
        pool: BitmapPool,
        toTransform: Bitmap,
        outWidth: Int,
        outHeight: Int
    ): Bitmap {
        val originWidth = toTransform.width
        val originHeight = toTransform.height
        val aspectRatio = originWidth.toFloat() / originHeight
        return Bitmap.createScaledBitmap(
            toTransform,
            outWidth,
            (outWidth / aspectRatio).toInt(),
            true
        )
    }

    override fun equals(other: Any?): Boolean = other is AspectRatioResizeTransform

    override fun hashCode(): Int = ID.hashCode()
}