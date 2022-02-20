package ru.skillbranch.skillarticles.ui

import ru.skillbranch.skillarticles.viewmodels.ArticleState
import ru.skillbranch.skillarticles.viewmodels.BottombarData
import ru.skillbranch.skillarticles.viewmodels.Notify
import ru.skillbranch.skillarticles.viewmodels.SubmenuData

interface IArticleView {

    fun renderSubmenu(data : SubmenuData)

    fun renderBotombar(data : BottombarData)

    fun renderUi(data : ArticleState)

    fun renderSearchResult(searchResult: List<Pair<Int, Int>>)

    fun renderSearchPosition(searchPosition: Int, searchResult : List<Pair<Int, Int>>)

    fun clearSearchResult()

    fun showSearchBar(resultsCount: Int, searchPosition: Int)

    fun hideSearchBar()

    fun setupSubmenu()

    fun setupBottombar()

    fun setupToolbar()

    fun setupCopyListener()

}