package ru.skillbranch.skillarticles.ui.custom.markdown

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.children
import ru.skillbranch.skillarticles.data.repositories.MarkdownElement
import ru.skillbranch.skillarticles.extensions.dpToIntPx
import ru.skillbranch.skillarticles.extensions.setPaddingOptionally
import kotlin.properties.Delegates

class MarkdownContentView @JvmOverloads constructor(
        context : Context,
        attrs : AttributeSet? = null,
        defStyleAttr : Int = 0
) : ViewGroup(context, attrs, defStyleAttr) {

    private lateinit var copyListener: (String) -> Unit
    private var elements : List<MarkdownElement> = emptyList()
    private var ids = arrayListOf<Int>()

    var textSize by Delegates.observable(14f) { _, old, newValue ->
        if (newValue == old) return@observable
        this.children.forEach {
            it as IMarkdownView
            it.fontSize = newValue
        }

    }

    var isLoading = true
    private val padding = context.dpToIntPx(8)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var usedHeight = paddingTop
        val width = View.getDefaultSize(suggestedMinimumWidth, widthMeasureSpec)

        children.forEach {
            measureChild(it, widthMeasureSpec, heightMeasureSpec)
            usedHeight += it.measuredHeight
        }

        usedHeight += paddingBottom
        setMeasuredDimension(width, usedHeight)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        var usedHeight = paddingTop
        val bodyWidth = right - left - paddingLeft - paddingRight
        val left = paddingLeft
        val right = paddingLeft + bodyWidth

        children.forEach {
            if (it is MarkdownTextView) {
                it.layout(
                        left - paddingLeft/2,
                        usedHeight,
                        r - paddingRight/2,
                        usedHeight + it.measuredHeight
                )
            } else {
                it.layout(
                        left,
                        usedHeight,
                        right,
                        usedHeight + it.measuredHeight
                )
            }
            usedHeight += it.measuredHeight
        }

    }

    fun setContent(content : List<MarkdownElement>) {
        if (elements.isNotEmpty()) return
        elements = content
        content.forEach {
            when (it) {
                is MarkdownElement.Text -> {
                    val tv = MarkdownTextView(context, textSize).apply {
                        setPaddingOptionally(left = padding, right = padding)
                    }

                    MarkdownBuilder(context)
                            .mardownToSpan(it)
                            .run {
                                tv.setText(this, TextView.BufferType.SPANNABLE)
                            }

                    addView(tv)

                }

                is MarkdownElement.Image -> {

                    Log.d("ImageGroup", "setContent: ${it}")
                    val iv = MarkdownImageView(
                            context,
                            textSize,
                            it.image.url,
                            it.image.text,
                            it.image.alt
                    )

                    addView(iv)

                }

                is MarkdownElement.Scroll -> {
                    val sv = MarkdownCodeView(
                        context,
                        textSize,
                        it.blockCode.text
                    )

                    sv.copyListener = copyListener
                    addView(sv)
                }

            }

        }

    }

    fun renderSearchResult(searchResult : List<Pair<Int, Int>>) {

        children.forEach { view ->
            view as IMarkdownView
            view.clearSearchResult()
        }

        if (searchResult.isEmpty()) return


        val bounds = elements.map { it.bounds }
        val groups = searchResult.groupByBounds(bounds)

        children.forEachIndexed { index, view ->
            view as IMarkdownView
            view.renderSearchResult(groups[index], elements[index].offset)
        }

    }

    fun renderSearchPosition(searchPosition : Pair<Int, Int>?) {
        searchPosition ?: return
        val bounds = elements.map { it.bounds }

        val index = bounds.indexOfFirst { (start, end) ->
              val boundRange = start..end
              val (startPos, endPos) = searchPosition
              startPos in boundRange && endPos in boundRange
        }

        if (index == -1) return
        val view = getChildAt(index)
        view as IMarkdownView
        view.renderSearchPosition(searchPosition, elements[index].offset)
    }

    fun clearSearchResult() {
        children.forEach { view ->
            view as IMarkdownView
            view.clearSearchResult()
        }
    }

    fun setCopyListener(listener : (String) -> Unit) {
        copyListener = listener
    }

}

private fun  List<Pair<Int, Int>>.groupByBounds(bounds: List<Pair<Int, Int>>): List<List<Pair<Int, Int>>> {

    val result = mutableListOf<List<Pair<Int, Int>>>()

    bounds.forEach { (startBound, endBound) ->
        val boundRange = startBound..endBound
        val searchBounds = filter { (startSearching, endSearching) ->
            startSearching in boundRange && endSearching in boundRange
        }
        result.add(searchBounds)
    }

    return result

}
