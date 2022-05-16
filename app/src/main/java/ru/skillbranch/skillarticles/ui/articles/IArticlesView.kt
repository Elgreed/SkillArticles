package ru.skillbranch.skillarticles.ui.articles

import ru.skillbranch.skillarticles.viewmodels.articles.ArticleItem

interface IArticlesView {
    fun onArticleClick(articleItem: ArticleItem)
    fun onToggleBookmark(articleItem: ArticleItem, isChecked: Boolean)
}