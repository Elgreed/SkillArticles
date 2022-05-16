package ru.skillbranch.skillarticles.viewmodels.articles

import java.util.*

data class ArticleItem(
    val id: String = "",
    val date: Date = Date(),
    val author: String = "",
    val authorAvatar: String = "",
    val title: String = "",
    val description: String = "",
    val poster: String = "",
    val category: String = "",
    val categoryIcon: String = "",
    val likeCount: Int,
    val commentCount: Int,
    val readDuration: Int,
    val categoryId: String = "",
    val isBookmark: Boolean = false
)