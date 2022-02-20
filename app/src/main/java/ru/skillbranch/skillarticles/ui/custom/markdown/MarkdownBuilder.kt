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
import ru.skillbranch.skillarticles.data.repositories.Element
import ru.skillbranch.skillarticles.data.repositories.MarkdownElement
import ru.skillbranch.skillarticles.extensions.attrValue
import ru.skillbranch.skillarticles.extensions.dpToPx
import ru.skillbranch.skillarticles.ui.custom.spans.*

class MarkdownBuilder(context: Context) {
    private val strikeWidth: Float = context.dpToPx(4)
    private val colorOnSurface = context.attrValue(R.attr.colorOnSurface, true)
    private val opacityColorSurface = context.getColor(R.color.opacity_color_surface)
    private val colorSecondary = context.attrValue(R.attr.colorSecondary, true)
    private val colorPrimary = context.attrValue(R.attr.colorPrimary, true)
    private val colorDivider = context.getColor(R.color.color_divider)
    private val cornerRadius = context.dpToPx(8)
    private val gap = context.dpToPx(8)
    private val bulletRadius = context.dpToPx(4)
    private val quoteWidth = context.dpToPx(4)
    private val headerMarginTop = context.dpToPx(12)
    private val headerMarginBottom = context.dpToPx(8)
    private val ruleWidth = context.dpToPx(2)
    private val linkIcon = ContextCompat.getDrawable(context, R.drawable.ic_baseline_link_24)!!

    fun markdownToSpan(elem: MarkdownElement.Text): SpannedString {
        return buildSpannedString {
            elem.elements.forEach { buildElement(it, this) }
        }
    }

    private fun buildElement(element: Element, builder: SpannableStringBuilder): CharSequence {
        return builder.apply {
            when (element) {
                is Element.Text -> append(element.text)
                is Element.UnorderedListItem -> {
                    inSpans(UnorderedListSpan(gap, bulletRadius, colorSecondary)) {
                        for (child in element.elements) {
                            buildElement(child, builder)
                        }
                    }
                }
                is Element.Quote -> {
                    inSpans(
                        StyleSpan(Typeface.ITALIC),
                        BlockquotesSpan(gap, quoteWidth, colorSecondary)
                    ) {
                        for (child in element.elements) {
                            buildElement(child, builder)
                        }
                    }
                }
                is Element.Header -> {
                    inSpans(
                        HeaderSpan(
                            element.level,
                            colorPrimary,
                            colorDivider,
                            headerMarginTop,
                            headerMarginBottom
                        )
                    ) {
                        append(element.text)
                    }
                }
                is Element.Bold -> {
                    inSpans(StyleSpan(Typeface.BOLD)) {
                        for (child in element.elements) {
                            buildElement(child, builder)
                        }
                    }
                }
                is Element.Italic -> {
                    inSpans(StyleSpan(Typeface.ITALIC)) {
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
                    inSpans(
                        InlineCodeSpan(
                            colorOnSurface,
                            opacityColorSurface,
                            cornerRadius,
                            gap
                        )
                    ) {
                        append(element.text)
                    }
                }
                is Element.Link -> {
                    inSpans(
                        IconLinkSpan(linkIcon, gap, colorPrimary, strikeWidth),
                        URLSpan(element.link)
                    ) {
                        append(element.text)
                    }
                }
                is Element.OrderedListItem -> {
                    inSpans(OrderedListSpan(gap, element.order, colorPrimary)) {
                        append(element.text)
                    }
                }
//                is Element.BlockCode -> {
//                    inSpans(BlockCodeSpan(colorOnSurface,
//                        opacityColorSurface,
//                        cornerRadius,
//                        gap)) {
//                        append(element.text)
//                    }
//                }
                else -> append(element.text)
            }
        }
    }
}