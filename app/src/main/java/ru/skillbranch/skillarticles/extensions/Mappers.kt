package ru.skillbranch.skillarticles.extensions

import ru.skillbranch.skillarticles.data.AppSettings
import ru.skillbranch.skillarticles.data.ArticlePersonalInfo
import ru.skillbranch.skillarticles.data.local.User
import ru.skillbranch.skillarticles.viewmodels.ArticleState

fun ArticleState.toAppSettings(): AppSettings{
    return AppSettings(isDarkMode, isBigText)
}

fun ArticleState.toArticlePersonalInfo(): ArticlePersonalInfo{
    return ArticlePersonalInfo(isLike, isBookmark)
}

fun ArticleState.asMap():Map<String, Any?> = mapOf(
    "isAuth" to isAuth,
    "isLoadingContent" to isLoadingContent,
    "isLoadingReviews" to isLoadingReviews,
    "isLike" to isLike,
    "isBookmark" to isBookmark,
    "isShowMenu" to isShowMenu,
    "isBigText" to isBigText,
    "isDarkMode" to isDarkMode,
    "isSearch" to isSearch,
    "searchQuery" to searchQuery,
    "searchResults" to searchResults,
    "searchPosition" to searchPosition,
    "shareLink" to shareLink,
    "title" to title,
    "category" to category,
    "categoryIcon" to categoryIcon,
    "date" to date,
    "author" to author,
    "poster" to poster,
    "content" to content,
    "reviews" to reviews
)

fun User.asMap(): Map<String, Any?> = mapOf(
    "id"  to id,
    "name"  to name,
    "avatar"  to avatar,
    "rating"  to rating,
    "respect"  to respect,
    "about"  to about
)

fun List<Pair<Int, Int>>.groupByBounds(bounds: List<Pair<Int, Int>>): List<MutableList<Pair<Int, Int>>> {
    return bounds.fold(mutableListOf<MutableList<Pair<Int, Int>>>()){acc, pair ->
        val res = this.filter {
            it.first >= pair.first && it.second <= pair.second
        }.toMutableList()
        acc.add(res)
        acc
    }
}