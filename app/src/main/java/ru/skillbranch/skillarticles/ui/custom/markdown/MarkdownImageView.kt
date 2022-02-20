package ru.skillbranch.skillarticles.ui.custom.markdown

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.os.Parcel
import android.os.Parcelable
import android.text.Spannable
import android.util.Log
import android.view.*
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.Px
import androidx.annotation.VisibleForTesting
import androidx.core.animation.doOnEnd
import androidx.core.graphics.ColorUtils
import androidx.core.view.isVisible
import androidx.core.view.setPadding
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
) : FrameLayout(context, null, 0), IMarkdownView {

    override var fontSize: Float = fontSize
        set(value) {
            tvTitle.textSize = value * 0.75f
            tvAlt?.textSize = value
            field = value
        }
    override val spannableContent: Spannable
        get() = tvTitle.text as Spannable

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
    private val colorSurface: Int = context.attrValue(R.attr.colorSurface, true)

    @ColorInt
    private val colorOnSurface: Int = context.attrValue(R.attr.colorOnSurface, true)

    @ColorInt
    private val colorOnBackground: Int = context.attrValue(R.attr.colorOnBackground, true)

    @ColorInt
    private var lineColor: Int = context.getColor(R.color.color_divider)

    //for draw object allocation
    private var linePositionY: Float = 0f
    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = lineColor
        strokeWidth = 0f
    }

    init {
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        ivImage = ImageView(context).apply {
            scaleType = ImageView.ScaleType.CENTER_CROP
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
        //if (ivImage.id == NO_ID) ivImage.id = View.generateViewId()

        tvTitle = MarkdownTextView(context, fontSize * 0.75f).apply {
            setText("title", TextView.BufferType.SPANNABLE)
            setTextColor(colorOnBackground)
            gravity = Gravity.CENTER
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
            setPaddingOptionally(start = titlePadding, end = titlePadding)
        }
        addView(tvTitle)
        //if (tvTitle.id == NO_ID) tvTitle.id = View.generateViewId()
    }

    constructor(
        context: Context,
        fontSize: Float,
        url: String,
        title: CharSequence,
        alt: String?
    ) : this(context, fontSize) {
        imageUrl = url
        imageTitle = title
        tvTitle.setText(title, TextView.BufferType.SPANNABLE)
        Glide
            .with(context)
            .load(url)
            .transform(AspectRatioResizeTransform())
            .into(ivImage)

        if (alt != null) {
            tvAlt = TextView(context).apply {
                setText(alt, TextView.BufferType.SPANNABLE)
                setTextColor(colorOnSurface)
                setBackgroundColor(ColorUtils.setAlphaComponent(colorSurface, 160))
                gravity = Gravity.CENTER
                textSize = fontSize
                setPadding(titleTopMargin)
                isVisible = false
            }
            addView(tvAlt)
            //if (tvAlt!!.id == NO_ID)  tvAlt!!.id = View.generateViewId()
            ivImage.setOnClickListener {
                if (tvAlt?.isVisible == true) animateHideAlt() else animateShowAlt()
            }
        }
        Log.d("M_MarkdownImageView", "Setted ImageId=${ivImage.id}, TitleId=${tvTitle.id}, AltId=${tvAlt?.id}")

    }


    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    public override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var usedHeight = 0
        var width = View.getDefaultSize(suggestedMinimumWidth, widthMeasureSpec)
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
    public override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        var usedHeight = 0
        val bodyWidth = r - l - paddingLeft - paddingRight
        val left = paddingLeft
        val right = paddingLeft + bodyWidth
        ivImage.layout(
            left,
            usedHeight, right,
            usedHeight + ivImage.measuredHeight
        )
        usedHeight += titleTopMargin + ivImage.measuredHeight
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
        super.onLayout(changed,l,t,r,b)
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    public override fun dispatchDraw(canvas: Canvas) {
        super.dispatchDraw(canvas)
        canvas.drawLine(0f, linePositionY, titlePadding.toFloat(), linePositionY, linePaint)
        canvas.drawLine(
            canvas.width - titlePadding.toFloat(),
            linePositionY,
            canvas.width.toFloat(),
            linePositionY,
            linePaint
        )
    }

    private fun animateShowAlt() {
        tvAlt?.isVisible = true
        val endRadius = hypot(tvAlt?.width?.toFloat() ?: 0f, tvAlt?.height?.toFloat() ?: 0f)
        val va = ViewAnimationUtils.createCircularReveal(
            tvAlt, tvAlt?.width ?: 0,
            tvAlt?.height ?: 0,
            0f,
            endRadius
        )
        va.start()
    }

    private fun animateHideAlt() {
        val endRadius = hypot(tvAlt?.width?.toFloat() ?: 0f, tvAlt?.height?.toFloat() ?: 0f)
        val va = ViewAnimationUtils.createCircularReveal(
            tvAlt, tvAlt?.width ?: 0,
            tvAlt?.height ?: 0,
            endRadius,
            0f
        )
        va.doOnEnd { tvAlt?.isVisible = false }
        va.start()
    }

    override fun onSaveInstanceState(): Parcelable? {
        val savedState = SavedState(super.onSaveInstanceState())
        if (ivImage.id == NO_ID)  ivImage.id = View.generateViewId()
        if (tvTitle.id == NO_ID)  tvTitle.id = View.generateViewId()
        if (tvAlt!!.id == NO_ID)  tvAlt!!.id = View.generateViewId()
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
            Log.d("M_MarkdownImageView", "Restored ImageId=${ivImage.id}, TitleId=${tvTitle.id}, AltId=${tvAlt?.id}")
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
    private val ID =
        "ru.skillbranch.skillarticles.glide.AspectRatioResizeTransform" //any unique string
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