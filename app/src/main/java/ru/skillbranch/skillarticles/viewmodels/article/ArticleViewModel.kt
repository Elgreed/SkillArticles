package ru.skillbranch.skillarticles.viewmodels.article

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import ru.skillbranch.skillarticles.data.ArticleData
import ru.skillbranch.skillarticles.data.ArticlePersonalInfo
import ru.skillbranch.skillarticles.data.repositories.ArticleRepository
import ru.skillbranch.skillarticles.data.repositories.MarkdownElement
import ru.skillbranch.skillarticles.data.repositories.clearContent
import ru.skillbranch.skillarticles.extensions.asMap
import ru.skillbranch.skillarticles.extensions.format
import ru.skillbranch.skillarticles.extensions.toAppSettings
import ru.skillbranch.skillarticles.extensions.toArticlePersonalInfo
import ru.skillbranch.skillarticles.extensions.indexesOf
import ru.skillbranch.skillarticles.ui.article.ArticleFragmentArgs
import ru.skillbranch.skillarticles.viewmodels.BaseViewModel
import ru.skillbranch.skillarticles.viewmodels.Notify
import ru.skillbranch.skillarticles.viewmodels.VMState

class ArticleViewModel(savedStateHandle: SavedStateHandle) :
    BaseViewModel<ArticleState>(ArticleState(), savedStateHandle), IArticleViewModel {

    private val repository = ArticleRepository
    private val args: ArticleFragmentArgs =
        ArticleFragmentArgs.fromSavedStateHandle(savedStateHandle)
    private val articleId = args.articleId
    private var clearContent: String? = null

    // В блоке инициализации подписываем на изменения
    init {
        savedStateHandle.setSavedStateProvider("state") {
            currentState.toBundle()
        }

        subscribeOnDataSource(repository.getAppSettings()) { setting, state ->
            state.copy(
                isDarkMode = setting.isDarkMode,
                isBigText = setting.isBigText
            )
        }
        subscribeOnDataSource(getArticleData()) { article, state ->
            article ?: return@subscribeOnDataSource null
            state.copy(
                shareLink = article.shareLink,
                title = article.title,
                author = article.author,
                category = article.category,
                categoryIcon = article.categoryIcon,
                date = article.date.format()
            )
        }

        subscribeOnDataSource(getArticleContent()) { content, state ->
            content ?: return@subscribeOnDataSource null
            state.copy(
                isLoadingContent = false,
                content = content
            )
        }

        subscribeOnDataSource(getArticlePersonalInfo()) { info, state ->
            info ?: return@subscribeOnDataSource null
            state.copy(
                isLike = info.isLike,
                isBookmark = info.isBookmark
            )
        }
    }

    override fun getArticleContent(): LiveData<List<MarkdownElement>?> {
        return repository.loadArticleContent(articleId)
    }

    override fun getArticleData(): LiveData<ArticleData?> {
        return repository.getArticle(articleId)
    }

    override fun getArticlePersonalInfo(): LiveData<ArticlePersonalInfo?> {
        return repository.loadArticlePersonalInfo(articleId)
    }

    override fun handleNightMode() {
        val mode = currentState.toAppSettings()
        repository.updateSettings(mode.copy(isDarkMode = !mode.isDarkMode))
    }

    override fun handleUpText() {
        repository.updateSettings(currentState.toAppSettings().copy(isBigText = true))
    }

    override fun handleDownText() {
        repository.updateSettings(currentState.toAppSettings().copy(isBigText = false))
    }

    override fun handleBookmark() {
        val info = currentState.toArticlePersonalInfo()
        repository.updateArticlePersonalInfo(info.copy(isBookmark = !info.isBookmark))
        val msg =
            if (currentState.isBookmark) Notify.TextMessage("Add to bookmarks") else Notify.TextMessage(
                "Remove from bookmarks"
            )
        notify(msg)
    }

    override fun handleLike() {
        // Определим лямбду, обновляющую информацию о статье
        val toggleLike = {
            val info = currentState.toArticlePersonalInfo()
            repository.updateArticlePersonalInfo(info.copy(isLike = !info.isLike))
        }
        toggleLike()
        val msg = if (currentState.isLike) Notify.TextMessage("Mark is liked")
        else {
            Notify.ActionMessage(
                "Don`t like it anymore",
                "No, still like it",
                toggleLike
            )
        }
        notify(msg)
    }

    override fun handleShare() {
        val msg = Notify.ErrorMessage("Share is not implemented", "OK", null)
        notify(msg)
    }

    override fun handleToggleMenu() {
        updateState {
            it.copy(isShowMenu = !it.isShowMenu)//.also { menuIsShown = !state.isShowMenu }
        }
    }

    override fun handleSearchMode(isSearch: Boolean) {
        updateState { it.copy(isSearch = isSearch, isShowMenu = false, searchPosition = 0) }
    }

    override fun handleSearch(query: String?) {
        query ?: return

        if (clearContent == null && currentState.content.isNotEmpty()) clearContent =
            currentState.content.clearContent()

        val result = clearContent.indexesOf(query)
            .map { it to it + query.length }

        updateState { it.copy(searchQuery = query, searchResults = result) }
    }

    override fun handleUpResult() {
        updateState {
            it.copy(searchPosition = it.searchPosition.dec())
        }
    }

    override fun handleDownResult() {
        updateState {
            it.copy(searchPosition = it.searchPosition.inc())
        }
    }

    override fun handleCopyCode() {
        notify(Notify.TextMessage("Code copy to clipboard"))
    }
}

/**
 * Стэйт(состояние) статьи
 */
data class ArticleState(
    val isAuth: Boolean = false, // пользователь авторизован
    val isLoadingContent: Boolean = true, // контент загружается
    val isLoadingReviews: Boolean = true, // отзывы загружается
    val isLike: Boolean = false, // отмечено как Like
    val isBookmark: Boolean = false, // в закладках
    val isShowMenu: Boolean = false, // отображается меню
    val isBigText: Boolean = false, // шрифт увеличен
    val isDarkMode: Boolean = false, // темный режим
    val isSearch: Boolean = false, // режим поиска
    val searchQuery: String? = null, // поисковый запрос
    val searchResults: List<Pair<Int, Int>> = emptyList(), // результаты поиска (стартовая и конечная позиции)
    val searchPosition: Int = 0, // текущая позиция найденного результата
    val shareLink: String? = null, // ссылка Share
    val title: String? = null, // заголовок статьи
    val category: String? = null, // категория
    val categoryIcon: Any? = null, // иконка категории
    val date: String? = null, // дата публикации
    val author: Any? = null, // автор
    val poster: String? = null,
    val content: List<MarkdownElement> = emptyList(),
    val reviews: List<Any> = emptyList(), // комментарии
    val message: String? = null // сообщение пользователя
) : VMState {
    override fun toBundle(): Bundle {
        val map = copy(content = emptyList(), isLoadingContent = true)
            .asMap()
            .toList()
            .toTypedArray()

        return bundleOf(*map)
    }

    override fun fromBundle(bundle: Bundle): ArticleState? {
        val map = bundle.keySet().associateWith { bundle[it] }
        return copy(
            isAuth = map["isAuth"] as Boolean,
            isLoadingContent = map["isLoadingContent"] as Boolean,
            isLoadingReviews = map["isLoadingReviews"] as Boolean,
            isLike = map["isLike"] as Boolean,
            isBookmark = map["isBookmark"] as Boolean,
            isShowMenu = map["isShowMenu"] as Boolean,
            isBigText = map["isBigText"] as Boolean,
            isDarkMode = map["isDarkMode"] as Boolean,
            isSearch = map["isSearch"] as Boolean,
            searchResults = map["searchResults"] as List<Pair<Int, Int>>,
            searchQuery = map["searchQuery"] as String?,
            searchPosition = map["searchPosition"] as Int,
            shareLink = map["shareLink"] as String?,
            title = map["title"] as String?,
            category = map["category"] as String?,
            categoryIcon = map["categoryIcon"] as Any?,
            date = map["date"] as String?,
            author = map["author"] as Any?,
            poster = map["poster"] as String?,
            content = map["content"] as List<MarkdownElement>,
            reviews = map["reviews"] as List<Any>,
            message = map["message"] as String?
        )
    }
}

data class BottombarData(
    val isLike: Boolean = false,
    val isBookmark: Boolean = false,
    val isShowMenu: Boolean = false,
    val isSearch: Boolean = false,
    val resultsCount: Int = 0,
    val searchPosition: Int = 0
)

data class SubmenuData(
    val isShowMenu: Boolean = false,
    val isDarkMode: Boolean = false,
    val isBigText: Boolean = false
)

fun ArticleState.toBottombarData() =
    BottombarData(isLike, isBookmark, isShowMenu, isSearch, searchResults.size, searchPosition)

fun ArticleState.toSubmenuData() =
    SubmenuData(isShowMenu, isDarkMode, isBigText)
