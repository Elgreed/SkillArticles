package ru.skillbranch.skillarticles.ui.custom

import android.annotation.SuppressLint
import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.view.ViewAnimationUtils
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.Px
import androidx.annotation.VisibleForTesting
import androidx.appcompat.widget.AppCompatImageView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.animation.doOnEnd
import androidx.core.view.isVisible
import androidx.core.view.setPadding
import com.google.android.material.shape.MaterialShapeDrawable
import ru.skillbranch.skillarticles.R
import ru.skillbranch.skillarticles.extensions.*
import ru.skillbranch.skillarticles.ui.custom.behaviors.BottombarBehavior
import kotlin.math.hypot


class Bottombar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ViewGroup(context, attrs, defStyleAttr), CoordinatorLayout.AttachedBehavior {

    //sizes
    @Px
    private val iconSize = context.dpToIntPx(56)
    @Px
    private val iconPadding = context.dpToIntPx(16)
    private val iconTint = context.getColorStateList(R.color.tint_color)
    val minHeight: Int = iconSize

    //views
    val btnLike: CheckableImageView
    val btnBookmark: CheckableImageView
    val btnShare: AppCompatImageView
    val btnSettings: CheckableImageView

    private val searchBar: SearchBar
    val tvSearchResult
        get() = searchBar.tvSearchResult
    val btnResultUp
        get() = searchBar.btnResultUp
    val btnResultDown
        get() = searchBar.btnResultDown
    val btnSearchClose
        get() = searchBar.btnSearchClose

    var isSearchMode = false
    override fun getBehavior(): CoordinatorLayout.Behavior<Bottombar> {
        return BottombarBehavior()
    }

    init {
        val materialBg = MaterialShapeDrawable.createWithElevationOverlay(context)
        materialBg.elevation = elevation
        background = materialBg
        btnLike = CheckableImageView(context).apply {
            setImageResource(R.drawable.like_states)
            imageTintList = iconTint
            setBackgroundResource(R.drawable.ripple)
            setPadding(iconPadding)

        }
        addView(btnLike)
        btnBookmark = CheckableImageView(context).apply {
            setImageResource(R.drawable.bookmark_states)
            imageTintList = iconTint
            setBackgroundResource(R.drawable.ripple)
            setPadding(iconPadding)
        }
        addView(btnBookmark)
        btnShare = AppCompatImageView(context).apply {
            setImageResource(R.drawable.ic_share_black_24dp)
            isFocusable = true
            isClickable = true
            imageTintList = iconTint
            setBackgroundResource(R.drawable.ripple)
            setPadding(iconPadding)
        }
        addView(btnShare)
        btnSettings = CheckableImageView(context).apply {
            setImageResource(R.drawable.ic_format_size_black_24dp)
            imageTintList = iconTint
            setBackgroundResource(R.drawable.ripple)
            setPadding(iconPadding)
        }
        addView(btnSettings)
        searchBar = SearchBar().apply {
            isVisible = false

        }
        addView(searchBar)
    }

    override fun onSaveInstanceState(): Parcelable {
        val saveState = SavedState(super.onSaveInstanceState())
        saveState.ssIsSearchMode = isSearchMode
        return saveState
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        super.onRestoreInstanceState(state)
        if (state is SavedState) {
            isSearchMode = state.ssIsSearchMode
            searchBar.isVisible = isSearchMode
        }
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    public override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val usedWidth = getDefaultSize(suggestedMinimumWidth, widthMeasureSpec)
        measureChild(searchBar,widthMeasureSpec, heightMeasureSpec)
        setMeasuredDimension(usedWidth, iconSize)
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    public override fun onLayout(p0: Boolean, l: Int, t: Int, r: Int, b: Int) {
        var usedWidth = paddingLeft
        btnLike.layout(
            l + usedWidth,
            paddingTop,
            l + usedWidth + iconSize,
            iconSize - paddingBottom
        )
        usedWidth += iconSize
        btnBookmark.layout(
            l + usedWidth,
            paddingTop,
            l + usedWidth + iconSize,
            iconSize - paddingBottom
        )
        usedWidth += iconSize
        btnShare.layout(
            l + usedWidth,
            paddingTop,
            l + usedWidth + iconSize,
            iconSize - paddingBottom
        )
        btnSettings.layout(
            r - iconSize,
            paddingTop,
            r,
            iconSize - paddingBottom
        )
        searchBar.layout(
            l,
            paddingTop,
            r,
            iconSize-paddingBottom
        )

    }

    fun setSearchState(isSearch: Boolean) {
        if (isSearch == isSearchMode || !isAttachedToWindow) return
        isSearchMode = isSearch
        if (isSearchMode) animatedShowSearch()
        else animateHideSearch()
    }

    fun setSearchInfo(searchCount: Int = 0, position: Int = 0) {
        btnResultUp.isEnabled = searchCount > 0
        btnResultDown.isEnabled = searchCount > 0
        tvSearchResult.text =
            if (searchCount == 0) resources.getString(R.string.not_found_text) else "${position.inc()} of $searchCount"
        when (position) {
            0 -> btnResultUp.isEnabled = false
            searchCount.dec() -> btnResultDown.isEnabled = false
        }
    }

    private fun animatedShowSearch() {
        searchBar.isVisible = true
        val endRadius = hypot(width.toDouble(), height / 2.toDouble())
        val va = ViewAnimationUtils.createCircularReveal(
            searchBar,
            width,
            height / 2,
            0f,
            endRadius.toFloat()
        )
        va.doOnEnd {
            btnLike.isVisible = false
            btnBookmark.isVisible = false
            btnShare.isVisible = false
            btnSettings.isVisible = false
        }
        va.start()
    }

    private fun animateHideSearch() {
        btnLike.isVisible = true
        btnBookmark.isVisible = true
        btnShare.isVisible = true
        btnSettings.isVisible = true
        val endRadius = hypot(width.toDouble(), height / 2.toDouble())
        val va = ViewAnimationUtils.createCircularReveal(
            searchBar,
            width,
            height / 2,
            endRadius.toFloat(),
            0f
        )
        va.doOnEnd {
            searchBar.isVisible = false

        }
        va.start()
    }

    private class SavedState : BaseSavedState, Parcelable {
        var ssIsSearchMode: Boolean = false

        constructor(superState: Parcelable?) : super(superState)

        constructor(parcel: Parcel) : super(parcel) {
            ssIsSearchMode = parcel.readByte() != 0.toByte()
        }

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            super.writeToParcel(parcel, flags)
            parcel.writeByte(if (ssIsSearchMode) 1 else 0)
        }

        override fun describeContents(): Int {
            return 0
        }

        companion object CREATOR : Parcelable.Creator<SavedState> {
            override fun createFromParcel(parcel: Parcel) = SavedState(parcel)
            override fun newArray(size: Int): Array<SavedState?> = arrayOfNulls(size)
        }

    }

    @SuppressLint("ViewConstructor")
    inner class SearchBar : ViewGroup(context, null, 0) {
        internal val btnSearchClose: AppCompatImageView
        internal val tvSearchResult: TextView
        internal val btnResultDown: AppCompatImageView
        internal val btnResultUp: AppCompatImageView
        @ColorInt
        private val iconColor = context.attrValue(R.attr.colorPrimary, true)
        private val iconTintSearch = context.getColorStateList(R.color.tint_search_color)
        private val textMargin = context.dpToIntPx(16)

        init {
            setBackgroundColor(resources.getColor(R.color.color_on_article_bar, context.theme))
            btnSearchClose = AppCompatImageView(context).apply {
                setImageResource(R.drawable.ic_close_black_24dp)
                setPadding(iconPadding)
                imageTintList = iconTintSearch
                setBackgroundResource(R.drawable.ripple)

            }
            addView(btnSearchClose)
            tvSearchResult = TextView(context).apply {
                text = resources.getString(R.string.not_found_text)
                textSize = 14f
                setTextColor(context.attrValue(R.attr.colorPrimary, true))
                setPaddingOptionally(start = context.dpToIntPx(16))
            }
            addView(tvSearchResult)
            btnResultDown = AppCompatImageView(context).apply {
                setImageResource(R.drawable.ic_keyboard_arrow_down_black_24dp)
                setPadding(iconPadding)
                imageTintList = iconTintSearch
                setBackgroundResource(R.drawable.ripple)
            }
            addView(btnResultDown)
            btnResultUp = AppCompatImageView(context).apply {
                setImageResource(R.drawable.ic_keyboard_arrow_up_black_24dp)
                setPadding(iconPadding)
                imageTintList = iconTintSearch
                setBackgroundResource(R.drawable.ripple)

            }
            addView(btnResultUp)
        }

        override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {

            tvSearchResult.measure(widthMeasureSpec, heightMeasureSpec)
            setMeasuredDimension(widthMeasureSpec, heightMeasureSpec)
        }

        override fun onLayout(p0: Boolean, l: Int, t: Int, r: Int, b: Int) {
            var usedWidth = paddingLeft
            btnSearchClose.layout(
                l + usedWidth,
                paddingTop,
                l + usedWidth + iconSize,
                b - paddingBottom
            )
            usedWidth += iconSize
            tvSearchResult.layout(
                l + usedWidth,
                (iconSize - tvSearchResult.measuredHeight) / 2,
                l + usedWidth + tvSearchResult.measuredWidth,
                (iconSize + tvSearchResult.measuredHeight) / 2
            )
            btnResultDown.layout(
                r - 2*iconSize,
                paddingTop,
                r - iconSize,
                iconSize - paddingBottom
            )
            btnResultUp.layout(
                r - iconSize,
                paddingTop,
                r,
                iconSize - paddingBottom
            )


        }

    }
}

