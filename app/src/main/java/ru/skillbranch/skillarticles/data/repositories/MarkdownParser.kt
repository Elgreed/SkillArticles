package ru.skillbranch.skillarticles.data.repositories

import android.util.Log
import java.util.regex.Pattern

object MarkdownParser {

    private val LINE_SEPARATOR = System.getProperty("line.separator") ?: "\n"

    private const val UNORDERED_LIST_ITEM_GROUP = "(^[*+-] .+$)"
    private const val HEADER_GROUP = "(^#{1,6} .+?$)"
    private const val QUOTE_GROUP = "(^> .+?$)"
    private const val ITALIC_GROUP = "((?<!\\*)\\*[^*].*?[^*]?\\*(?!\\*)|(?<!_)_[^_].*?[^_]?_(?!_))"
    private const val BOLD_GROUP = "((?<!\\*)\\*{2}[^*].*?[^*]?\\*{2}(?!\\*)|(?<!_)_{2}[^_].*?[^_]?_{2}(?!_))"
    private const val STRIKE_GROUP = "((?<!~)~{2}[^~].*?[^~]?~{2}(?!~))"
    private const val RULE_GROUP = "(^[-_*]{3}$)"
    private const val INLINE_GROUP = "((?<!`)`[^`\\s].*?[^`\\s]?`(?!`))"
    private const val LINK_GROUP = "(\\[[^\\[\\]]*?]\\(.+?\\))"
    private const val IMAGE_GROUP = "(!\\[[^\\[\\]]*?]\\(.+?\"?.*?\"?\\))"
    private const val BLOCK_GROUP = "((?<!`)`{3}[^`\\s](.\\s?\n?)*?[^`\\s]?`{3}(?!`))"
    private const val ORDERED_LIST_ITEM_GROUP = "(^[0-9]+\\. .+$)"

    private const val MARKDOWN_GROUPS
            = "$UNORDERED_LIST_ITEM_GROUP|$HEADER_GROUP|$QUOTE_GROUP|$ITALIC_GROUP|$BOLD_GROUP|$STRIKE_GROUP|$RULE_GROUP|$INLINE_GROUP|$LINK_GROUP|$IMAGE_GROUP|$ORDERED_LIST_ITEM_GROUP|$BLOCK_GROUP"


    private val pattern by lazy { Pattern.compile(MARKDOWN_GROUPS, Pattern.MULTILINE) }

    fun parse(string : String) : List<MarkdownElement> {
        val elements = mutableListOf<Element>()
        elements.addAll(findElements(string))
        return elements.fold(mutableListOf()) { acc, element ->
            val last = acc.lastOrNull()
            when (element) {
                is Element.Image -> acc.add(MarkdownElement.Image(element, last?.bounds?.second ?: 0))
                is Element.BlockCode -> acc.add(MarkdownElement.Scroll(element, last?.bounds?.second ?: 0))
                else -> {
                    if (last is MarkdownElement.Text) last.elements.add(element)
                    else acc.add(MarkdownElement.Text(mutableListOf(element), last?.bounds?.second ?: 0))
                }
            }
            acc
        }
    }

//    fun clear(string: String?) : String? {
//        string?.let {
//            val markdown = parse(it)
//
//            var result = createString(markdown.elements)
//
//            return result
//        }
//
//        return null
//    }

    private fun createString(elements : List<Element>) : String {
        var result = ""

        if (!elements.isEmpty()) {

            for (element in elements) {
                when (element) {
                    is Element.Text -> result += element.text
                    is Element.Header -> result += element.text
                    is Element.Rule -> result += element.text
                    is Element.InlineCode -> result += element.text
                    is Element.BlockCode -> result += element.text
                    is Element.Link -> result += element.text
                    is Element.Image -> result += element.text
                    is Element.OrderedListItem -> result += element.text
                }

                result += createString(element.elements)
            }

        }

        return result
    }

    private fun findElements(string: CharSequence) : List<Element> {
        val parents = mutableListOf<Element>()
        val matcher = pattern.matcher(string)
        var lastStartIndex = 0

        loop@ while (matcher.find(lastStartIndex)) {

            val startIndex = matcher.start()
            val endIndex = matcher.end()

            if (lastStartIndex < startIndex) {
                parents.add(Element.Text(string.subSequence(lastStartIndex, startIndex).toString()))
            }

            var text : CharSequence

            val groups = 1..12
            var group = -1

            for (gr in groups) {
                if (matcher.group(gr) != null) {
                    group = gr
                    break
                }
            }

            when (group) {
                -1 -> break@loop

                1 ->  {
                    text = string.subSequence(startIndex.plus(2), endIndex)
                    println("Group $group and Text = $text")
                    val subs = findElements(text)
                    val element = Element.UnorderedListItem(text.toString(), subs)
                    parents.add(element)
                    lastStartIndex = endIndex
                }

                2 -> {
                    val reg = "^#{1,6}".toRegex().find(string.subSequence(startIndex, endIndex))
                    val level = reg!!.value.length
                    text = string.subSequence(startIndex.plus(level).plus(1), endIndex)
                    println("Group $group and Text = $text")
                    val element = Element.Header(level, text.toString())
                    parents.add(element)
                    lastStartIndex = endIndex
                }

                3 -> {
                    text = string.subSequence(startIndex.plus(2), endIndex)
                    println("Group $group and Text = $text")
                    val subs = findElements(text)
                    val element = Element.Quote(text.toString(), subs)
                    parents.add(element)
                    lastStartIndex = endIndex
                }

                4 -> {
                    text = string.subSequence(startIndex.plus(1), endIndex.minus(1))
                    println("Group $group and Text = $text")
                    val subs = findElements(text)
                    val element = Element.Italic(text.toString(), subs)
                    parents.add(element)
                    lastStartIndex = endIndex
                }

                5 -> {
                    text = string.subSequence(startIndex.plus(2), endIndex.minus(2))
                    println("Group $group and Text = $text")
                    val subs = findElements(text)
                    val element = Element.Bold(text.toString(), subs)
                    parents.add(element)
                    lastStartIndex = endIndex
                }

                6 -> {
                    text = string.subSequence(startIndex.plus(2), endIndex.minus(2))
                    println("Group $group and Text = $text")
                    val subs = findElements(text)
                    val element = Element.Strike(text.toString(), subs)
                    parents.add(element)
                    lastStartIndex = endIndex
                }

                7 -> {
                    val element = Element.Rule()
                    parents.add(element)
                    lastStartIndex = endIndex
                }

                8 -> {
                    text = string.subSequence(startIndex.inc(), endIndex.dec())
                    println("Group $group and Text = $text")
                    val element = Element.InlineCode(text.toString())
                    parents.add(element)
                    lastStartIndex = endIndex
                }

                9 -> {
                    text = string.subSequence(startIndex, endIndex)

                    val (title : String, link : String) = "\\[(.*)]\\((.*)\\)".toRegex().find(text)!!.destructured
                    println("Group $group and Text = $title")
                    val element = Element.Link(link, title)
                    parents.add(element)
                    lastStartIndex = endIndex
                }

                10 -> {
                    text = string.subSequence(startIndex, endIndex)
                    Log.d("ImageGroup", "findElements: $text")
                    val resultArray = text.split("[\\[\\]\\)\\(]".toRegex())
                    println(resultArray)
                    println("Group $group and Text = $text")
                    val url = resultArray[3].split(" ")[0]
                    val alt = if (!resultArray[1].isEmpty()) resultArray[1] else null
                    println("Result 3 ${resultArray[3]} and split ${resultArray[3].split(" ")}")

                    val title  = if (resultArray[3].split(" ").size > 1)
                        "\"(.*)\"".toRegex().find(resultArray[3])!!.destructured.toList()[0]
                    else ""

                    Log.d("ImageGroup", "findElements: url = ${url} alt = ${alt} title = ${title}")

                    val element = Element.Image(url, alt, title)
                    println("Group $group and Text = ${element.text}")
                    parents.add(element)
                    lastStartIndex = endIndex
                }

                12 -> {
                    text = string.subSequence(startIndex.plus(3), endIndex.minus(3))
                    println("Group $group and Text = $text")
                    val element = Element.BlockCode(text.toString())
                    parents.add(element)
                    lastStartIndex = endIndex
                }

                11 -> {
                    text = string.subSequence(startIndex.plus(3), endIndex)
                    println("Group $group and Text = $text")
                    val order = string.subSequence(startIndex, startIndex.plus(3))
                    val element = Element.OrderedListItem("${order.trim()}", text.toString())
                    parents.add(element)
                    lastStartIndex = endIndex
                }

            }

        }

        if (lastStartIndex < string.length) {
            parents.add(Element.Text(string.subSequence(lastStartIndex, string.length).toString()))
        }

        return parents
    }

}

data class MarkdownText(val elements : List<Element>)

sealed class MarkdownElement {

    abstract val offset : Int
    val bounds : Pair<Int, Int> by lazy {
        when (this) {
            is Text -> {
                val end = elements.fold(offset) { acc, element ->
                    acc + element.spread().map { it.text.length }.sum()
                }
                offset to end
            }
            is Image -> offset to image.text.length + offset
            is Scroll -> offset to blockCode.text.length + offset
        }
    }

    data class Text(
        val elements : MutableList<Element>,
        override val offset : Int = 0
    ) : MarkdownElement()

    data class Image(
        val image : Element.Image,
        override val offset : Int = 0
    ) : MarkdownElement()

    data class Scroll(
        val blockCode : Element.BlockCode,
        override val offset : Int = 0
    ) : MarkdownElement()

}

sealed class Element {

    abstract val text : String
    abstract val elements : List<Element>

    data class Text(
            override val text: String,
            override val elements: List<Element> = emptyList()
    ) : Element()

    data class UnorderedListItem(
            override val text: String,
            override val elements: List<Element> = emptyList()
    ) : Element()

    data class Header(
            val level : Int,
            override val text: String,
            override val elements: List<Element> = emptyList()
    ) : Element()

    data class Quote(
            override val text: String,
            override val elements: List<Element> = emptyList()
    ) : Element()

    data class Italic(
            override val text: String,
            override val elements: List<Element> = emptyList()
    ) : Element()

    data class Bold(
            override val text: String,
            override val elements: List<Element> = emptyList()
    ) : Element()

    data class Strike(
            override val text: String,
            override val elements: List<Element> = emptyList()
    ) : Element()

    data class Rule(
            override val text: String = " ",
            override val elements: List<Element> = emptyList()
    ) : Element()

    data class InlineCode(
            override val text: String,
            override val elements: List<Element> = emptyList()
    ) : Element()

    data class Link(
            val link : String,
            override val text: String,
            override val elements: List<Element> = emptyList()
    ) : Element()

    data class Image(
            val url: String,
            val alt: String?,
            override val text: String,
            override val elements: List<Element> = emptyList()
    ) : Element()

    data class BlockCode(
            override val text: String,
            override val elements: List<Element> = emptyList()
    ) : Element()

    data class OrderedListItem(
            val order: String,
            override val text: String,
            override val elements: List<Element> = emptyList()
    ) : Element()

}

private fun Element.spread() : List<Element> {
    val elements = mutableListOf<Element>()
    if (this.elements.isNotEmpty()) elements.addAll(this.elements.spread())
    else elements.add(this)
    return elements
}

private fun List<Element>.spread() : List<Element> {
    val elemants = mutableListOf<Element>()
    forEach {
        elemants.addAll(it.spread())
    }
    return elemants
}

private fun Element.clearContent() : String {
    return StringBuilder().apply {
        val element = this@clearContent
        if (element.elements.isEmpty()) append(element.text)
        else element.elements.forEach { append(it.clearContent()) }
    }.toString()
}

fun List<MarkdownElement>.clearContent() : String {
    return StringBuilder().apply {
        this@clearContent.forEach {
            when (it) {
                is MarkdownElement.Text -> it.elements.forEach { el -> append(el.clearContent()) }
                is MarkdownElement.Image -> append(it.image.clearContent())
                is MarkdownElement.Scroll -> append(it.blockCode.clearContent())
            }
        }
    }.toString()
}