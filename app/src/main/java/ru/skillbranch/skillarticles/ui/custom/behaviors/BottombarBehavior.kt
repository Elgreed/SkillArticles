package ru.skillbranch.skillarticles.ui.custom.behaviors

import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.math.MathUtils
import androidx.core.view.ViewCompat
import androidx.customview.widget.ViewDragHelper
import ru.skillbranch.skillarticles.R
import ru.skillbranch.skillarticles.ui.custom.Bottombar

class BottombarBehavior: CoordinatorLayout.Behavior<Bottombar>() {
    //private var bottomBound = 0
    //private var topBound = 0
    //private var interceptingEvents = false
    //lateinit var dragHelper: ViewDragHelper

    /*override fun onLayoutChild(parent: CoordinatorLayout, child: Bottombar, layoutDirection: Int): Boolean {
        parent.onLayoutChild(child, layoutDirection)
        if (!::dragHelper.isInitialized) initialize(parent, child)
        
        return true
    }

    private fun initialize(parent: CoordinatorLayout, child: Bottombar) {
        dragHelper = ViewDragHelper.create(parent, 1f, DragHelperCallback())
        topBound = parent.height - child.height
        bottomBound = parent.height - child.minHeight
        Log.e("BottombarBehavior", "topBound : $topBound")
        Log.e("BottombarBehavior", "bottomBound : $bottomBound")
        //val webView = child.findViewById<WebView>(R.id.webview)
        //webView.webViewClient = WebViewClient()
        //webView.loadUrl("https://static-maps.yandex.ru/1.x/?ll=-18.783719,64.881884&size=400,300&l=sat&z=9")
    }*/

    override fun onStartNestedScroll(coordinatorLayout: CoordinatorLayout,
                                     child: Bottombar,
                                     directTargetChild: View,
                                     target: View,
                                     axes: Int, type: Int): Boolean {
        return axes == ViewCompat.SCROLL_AXIS_VERTICAL
    }

    override fun onNestedPreScroll(coordinatorLayout: CoordinatorLayout,
                                   child: Bottombar,
                                   target: View,
                                   dx: Int,
                                   dy: Int,
                                   consumed: IntArray,
                                   type: Int) {
        //Скорее всего, нужно проводить проверку и не скрывать боттомбар в режиме поиска.
        if(!child.isSearchMode) {
            val offset = MathUtils.clamp(child.translationY + dy, 0f, child.height.toFloat())
            if (offset != child.translationY) child.translationY = offset
        }
        super.onNestedPreScroll(coordinatorLayout, child, target, dx, dy, consumed, type)
    }

    /*// Определяет будет ли обработано касание потомком или же будет обработано CoordinatorLayout
    override fun onInterceptTouchEvent(parent: CoordinatorLayout, child: Bottombar, ev: MotionEvent): Boolean {
        when(ev.actionMasked) {
            // Палец положен на дисплэй и находится в области нашей рабочей вью
            MotionEvent.ACTION_DOWN -> interceptingEvents = parent.isPointInChildBounds(child, ev.x.toInt(), ev.y.toInt())
            // Если событие было вне нашей вью или палец убран
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> interceptingEvents = false
        }

        return if (interceptingEvents) dragHelper.shouldInterceptTouchEvent(ev)
        else false
    }

    // Обработчик события касания
    override fun onTouchEvent(parent: CoordinatorLayout, child: Bottombar, ev: MotionEvent): Boolean {
        if (::dragHelper.isInitialized) {
            dragHelper.processTouchEvent(ev)
        }
        return true
    }

    inner class DragHelperCallback: ViewDragHelper.Callback() {
        // Может ли текущая вью быть перетаскиваемой
        override fun tryCaptureView(child: View, pointerId: Int): Boolean {
            return child is Bottombar
        }

        override fun getViewVerticalDragRange(child: View): Int {
            return bottomBound - topBound
        }

        override fun clampViewPositionVertical(child: View, top: Int, dy: Int): Int {
            return MathUtils.clamp(top, topBound, bottomBound)
        }
    }*/
}