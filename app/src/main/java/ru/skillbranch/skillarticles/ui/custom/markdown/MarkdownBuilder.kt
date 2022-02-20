package ru.skillbranch.skillarticles.ui.custom.markdown

import android.content.Context
import android.graphics.Typeface
import android.text.SpannableStringBuilder
import android.text.SpannedString
import android.text.style.StrikethroughSpan
import android.text.style.StyleSpan
import android.text.style.URLSpan
import androidx.core.content.ContextCompat
import androidx.core.text.buildSpannedString
import androidx.core.text.inSpans
import ru.skillbranch.skillarticles.R
import ru.skillbranch.skillarticles.extensions.attrValue
import ru.skillbranch.skillarticles.extensions.dpToPx
import ru.skillbranch.skillarticles.data.repositories.Element
import ru.skillbranch.skillarticles.data.repositories.MarkdownElement
import ru.skillbranch.skillarticles.data.repositories.MarkdownParser
import ru.skillbranch.skillarticles.ui.custom.spans.*

class MarkdownBuilder(context : Context) {

    private val gap : Float = context.dpToPx(8)
    private val bulletRadius : Float = context.dpToPx(4)
    private val colorSecondary = context.attrValue(R.attr.colorSecondary)
    private val quoteWidth = context.dpToPx(4)
    private val colorPrimary = context.attrValue(R.attr.colorPrimary)
    private val colorDivider = context.getColor(R.color.color_divider)
    private val headerMarginTop = context.dpToPx(12)
    private val headerMarginBottom = context.dpToPx(8)
    private val ruleWidth = context.dpToPx(2)
    private val colorOnSurface = context.attrValue(R.attr.colorOnSurface)
    private val opacityColorSurface = context.getColor(R.color.color_surface)
    private val cornerRadius = context.dpToPx(8)
    private val strikeWidth = context.dpToPx(4)
    private val linkIcon = ContextCompat.getDrawable(context, R.drawable.ic_baseline_link)!!

    fun mardownToSpan(markdownText : MarkdownElement.Text) : SpannedString {
      //  val markdown = MarkdownParser.parse(string)
        return buildSpannedString {
            markdownText.elements.forEach {
                buildElement(it, this)
            }
        }
    }

    private fun buildElement(element: Element, builder : SpannableStringBuilder) : CharSequence {
         return builder.apply {
             when(element) {
                 is Element.Text -> append(element.text)
                 is Element.UnorderedListItem -> {
                     inSpans(UnorderedListSpan(gap, bulletRadius, colorSecondary)) {
                         for (child in element.elements) {
                             buildElement(child, builder)
                         }
                     }
                 }
                 is Element.Quote -> {
                     inSpans(BlockquotesSpan(gap, quoteWidth, colorSecondary), StyleSpan(Typeface.ITALIC)) {
                         for (child in element.elements) {
                             buildElement(child, builder)
                         }
                     }
                 }

                 is Element.Header -> {
                     inSpans(HeaderSpan(element.level, colorOnSurface, colorDivider, headerMarginTop, headerMarginBottom)) {
                         append(element.text)
                     }
                 }

                 is Element.Italic -> {
                     inSpans(StyleSpan(Typeface.ITALIC)) {
                         for (child in element.elements) {
                             buildElement(child, builder)
                         }
                     }
                 }
                 is Element.Bold -> {
                     inSpans(StyleSpan(Typeface.BOLD)) {
                         for (child in element.elements) {
                             buildElement(child, builder)
                         }
                     }
                 }
                 is Element.Strike -> {
                     inSpans(StrikethroughSpan()) {
                         for (child in element.elements) {
                             buildElement(child, builder)
                         }
                     }
                 }
                 is Element.Rule -> {
                     inSpans(HorizontalRuleSpan(ruleWidth, colorDivider)) {
                         append(element.text)
                     }
                 }
                 is Element.InlineCode -> {
                     inSpans(InlineCodeSpan(colorOnSurface, opacityColorSurface, cornerRadius, gap)) {
                         append(element.text)
                     }
                 }
                 is Element.Link -> {
                     inSpans(
                             IconLinkSpan(linkIcon, gap, colorOnSurface, strikeWidth),
                             URLSpan(element.link)
                     ) {
                         append(element.text)
                     }
                 }
                 else -> append(element.text)
             }
         }
    }

}