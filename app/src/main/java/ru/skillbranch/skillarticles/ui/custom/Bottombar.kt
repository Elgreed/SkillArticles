package ru.skillbranch.skillarticles.ui.custom

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewAnimationUtils
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.animation.doOnEnd
import androidx.core.view.isVisible
import com.google.android.material.shape.MaterialShapeDrawable
import ru.skillbranch.skillarticles.R
import ru.skillbranch.skillarticles.databinding.LayoutBottombarBinding
import ru.skillbranch.skillarticles.ui.custom.behaviors.BottombarBehavior
import kotlin.math.hypot


class Bottombar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr), CoordinatorLayout.AttachedBehavior {

    var isClose = true
    private val _bottombarBinding : LayoutBottombarBinding
    val bottombarBinding : LayoutBottombarBinding
                get() = _bottombarBinding

    var isSearchMode = false
    private val TAG = "Bottombar"

    init {
        val view = View.inflate(context, R.layout.layout_bottombar, this)
        _bottombarBinding = LayoutBottombarBinding.bind(view)
        val materialBg = MaterialShapeDrawable.createWithElevationOverlay(context)
        materialBg.elevation = elevation
        background = materialBg
    }

    override fun getBehavior(): CoordinatorLayout.Behavior<*> {
        return BottombarBehavior()
    }

    override fun onSaveInstanceState(): Parcelable {
        val saveState = SavedState(super.onSaveInstanceState())
        saveState.ssIsSearchMode = isSearchMode
        return saveState
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        super.onRestoreInstanceState(state)
        if(state is SavedState) {
            isSearchMode = state.ssIsSearchMode
            bottombarBinding.reveal.isVisible = isSearchMode
            bottombarBinding.bottomGroup.isVisible = !isSearchMode
        }
    }

    fun setSearchInfo(searchCount : Int = 0, position : Int = 0) {
        Log.d("NumberPos", "setSearchInfo: ${position}")
         with(bottombarBinding) {
             btnResultUp.isEnabled = searchCount > 0
             btnResultDown.isEnabled = searchCount > 0

             tvSearchResult.text =
                     if (searchCount == 0) "Not found"
                     else "${position.inc()} of ${searchCount}"

             when(position) {
                 0 -> btnResultUp.isEnabled = false
                 searchCount.dec() -> btnResultDown.isEnabled = false
             }

             when (position) {
                 searchCount.dec() -> btnResultDown.isEnabled = false
             }
         }
    }

    fun setSearchState(search : Boolean) {
        if (search == isSearchMode || !isAttachedToWindow) {
            Log.d(TAG, "setSearchState: return ${search == isSearchMode} and ${isAttachedToWindow}")
            return
        }
        isSearchMode = search

        if (isSearchMode) animateShowSearch()
        else animateHideSearch()
    }

    private fun animateShowSearch() {
        bottombarBinding.reveal.isVisible = true

        val endRadius = hypot(width.toDouble(), height.toDouble() / 2 )

        val va = ViewAnimationUtils.createCircularReveal(
            bottombarBinding.reveal,
            width,
            height / 2,
            0f,
            endRadius.toFloat()
        )

        va.doOnEnd {
            bottombarBinding.bottomGroup.isVisible = false
        }

        va.start()
    }

    private fun animateHideSearch() {
        bottombarBinding.bottomGroup.isVisible = true

        val endRadius = hypot(width.toDouble(), height.toDouble() / 2 )

        val va = ViewAnimationUtils.createCircularReveal(
            bottombarBinding.reveal,
            width,
            height / 2,
            endRadius.toFloat(),
            0f
        )

        va.doOnEnd {
            bottombarBinding.reveal.isVisible = false
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
            override fun createFromParcel(parcel: Parcel)= SavedState(parcel)
            override fun newArray(size: Int): Array<SavedState?> = arrayOfNulls(size)
        }

    }

}