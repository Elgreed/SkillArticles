package ru.skillbranch.skillarticles.ui.custom.markdown

import android.text.Spannable
import android.text.SpannableString
import android.util.Log
import androidx.core.text.getSpans
import ru.skillbranch.skillarticles.ui.custom.spans.SearchFocusSpan
import ru.skillbranch.skillarticles.ui.custom.spans.SearchSpan

interface IMarkdownView {

    var fontSize : Float
    val spannableContent : Spannable

    fun clearSearchResult() {
        spannableContent.getSpans<SearchSpan>().forEach { spannableContent.removeSpan(it) }
    }

    fun renderSearchResult(
            results : List<Pair<Int, Int>>,
            offset : Int
    ) {
        clearSearchResult()
        val offsetResult = results.map { (start, end) -> start.minus(offset) to end.minus(offset) }

        try {

            offsetResult.forEach {(start, end)  ->
                spannableContent.setSpan(
                        SearchSpan(),
                        start,
                        end,
                        SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
                )

        }
        } catch (e : Exception) {
            e.printStackTrace()
            Log.e("IMarkdownView", "renderSearchResult: ${e.message}")
        }

    }

    fun renderSearchPosition(
            position : Pair<Int, Int>,
            offset: Int
    ) {
        spannableContent.getSpans<SearchFocusSpan>().forEach { spannableContent.removeSpan(it) }

        spannableContent.setSpan(
                SearchFocusSpan(),
                position.first.minus(offset),
                position.second.minus(offset),
                SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
        )
    }

}