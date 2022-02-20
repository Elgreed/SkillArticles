package ru.skillbranch.skillarticles.viewmodels

import android.os.Bundle
import android.util.Log
import androidx.core.os.bundleOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import ru.skillbranch.skillarticles.data.ArticleData
import ru.skillbranch.skillarticles.data.ArticlePersonalInfo
import ru.skillbranch.skillarticles.data.repositories.ArticleRepository
import ru.skillbranch.skillarticles.data.repositories.MarkdownElement
import ru.skillbranch.skillarticles.extensions.*
import ru.skillbranch.skillarticles.data.repositories.MarkdownParser
import ru.skillbranch.skillarticles.data.repositories.clearContent

class ArticleViewModel(private val articleId : String, savedStateHandle: SavedStateHandle)
    : BaseViewModel<ArticleState>(ArticleState(), savedStateHandle), IArticleViewModel {

    private val repository = ArticleRepository()
    private var clearContent : String? = null

    init {
          subscribeOnDataSource(getArticleData()) { article, state ->
              article ?: return@subscribeOnDataSource null
              state.copy(
                      shareLink = article.shareLink,
                      author = article.author,
                      title = article.title,
                      category = article.category,
                      categoryIcon = article.categoryIcon,
                      date = article.date.format()
              )
          }

        subscribeOnDataSource(getArticlePersonalInfo()) { info, state ->
            info ?: return@subscribeOnDataSource null
            state.copy(
                    isBookmark = info.isBookmark,
                    isLike = info.isLike
            )

        }

        subscribeOnDataSource(repository.getAppSettings()) { settings, state ->
            settings ?: return@subscribeOnDataSource null
            state.copy(
                    isDarkMode = settings.isDarkMode,
                    isBigText = settings.isBigText
            )
        }

        subscribeOnDataSource(getArticleContent()) { content, state ->
            content ?: return@subscribeOnDataSource null
            state.copy(
                    isLoadingContent = false,
                    content = content
            )
        }


    }

    override fun getArticleContent() : LiveData<List<MarkdownElement>?> {
        return repository.loadArticleContent(articleId)
    }

    override fun getArticleData() : LiveData<ArticleData?> {
        return repository.getArticle(articleId)
    }

    override fun getArticlePersonalInfo(): LiveData<ArticlePersonalInfo?> {
        return repository.loadArticlePersonalInfo(articleId)
    }

   override fun handleLike() {
        val toggleLike = {
            val info = currentState.toArticlePersonalInfo()
            repository.updateArticlePersonalInfo(info.copy(isLike = !info.isLike))
        }

        toggleLike()

        val message = if (currentState.isLike) Notify.TextMessage("Mark is liked")
                     else {
                           Notify.ActionMessage("Don`t like it anymore",
                                   "No, still like it",
                           toggleLike)
                      }

        notify(message)
    }

   override fun handleBookmark() {
       val personalInfo = currentState.toArticlePersonalInfo()
       repository.updateArticlePersonalInfo(personalInfo.copy(isBookmark = !personalInfo.isBookmark))

       val message = if (currentState.isBookmark) Notify.TextMessage("Add to bookmarks")
                     else Notify.TextMessage("Remove from bookmarks")

       notify(message)
    }

   override fun handleShare() {
        val message = "Share is not implemented"
        notify(Notify.ErrorMessage(message, "OK", null))
    }

   override fun handleToggleMenu() {
        updateState {
            it.copy(isShowMenu = !it.isShowMenu)
        }
    }

    override fun handleSearchMode(isSearch: Boolean) {
       updateState {
           it.copy(
                   isSearch = isSearch,
                   isShowMenu = false,
                   searchPosition = 0
           )
       }
    }

    override fun handleSearch(query: String?) {
        query ?: return

        Log.d("Query", "handleSearch: ${query} ")

       if (clearContent == null && currentState.content.isNotEmpty()) clearContent = currentState.content.clearContent()

        val result = clearContent.indexesOf(query)
                .map { it to it + query.length }

        updateState {
            it.copy(searchQuery = query, searchResults = result)
        }

    }

    override fun handleUpResult() {
        updateState { it.copy(searchPosition = it.searchPosition.dec()) }
    }

    override fun handleDownResult() {
        updateState { it.copy(searchPosition = it.searchPosition.inc()) }
    }

    override fun handleCopyCode() {
        notify(Notify.TextMessage("Code copy to clipboard"))
    }

    override fun handleUpText() {
        repository.updateSettings(currentState.toAppSettings().copy(isBigText = true))
    }

   override fun handleDownText() {
        repository.updateSettings(currentState.toAppSettings().copy(isBigText = false))
    }

   override fun handleNightMode() {
        val settings = currentState.toAppSettings()
        repository.updateSettings(settings.copy(isDarkMode = !settings.isDarkMode))
    }

}



data class ArticleState(
        val isAuth : Boolean = false,
        val isLoadingContent : Boolean = true,
        val isLoadingReviews : Boolean = true,
        val isLike : Boolean = false,
        val isBookmark : Boolean = false,
        val isShowMenu : Boolean = false,
        val isBigText : Boolean = false,
        val isDarkMode : Boolean = false,
        val isSearch : Boolean = false,
        val searchQuery : String? = null,
        val searchResults : List<Pair<Int, Int>> = emptyList(),
        val searchPosition : Int = 0,
        val shareLink : String? = null,
        val title : String? = null,
        val category : String? = null,
        val categoryIcon : Any? = null,
        val date : String? = null,
        val author : Any? = null,
        val poster : String? = null,
        val content : List<MarkdownElement> = emptyList(),
        val reviews : List<Any> = emptyList()
) : VMState {

    override fun toBundle(): Bundle {
        val map = copy(content = emptyList(), isLoadingContent = true)
                .asMap()
                .toList()
                .toTypedArray()

        return bundleOf(*map)
    }

    override fun fromBundle(bundle: Bundle): VMState? {
        val map = bundle.keySet().associateWith { bundle[it] }

        return copy(
                isAuth  = map["isAuth"] as Boolean,
         isLoadingContent = map["isLoadingContent"] as Boolean,
        isLoadingReviews  = map["isLoadingReviews"] as Boolean,
        isLike = map["isLike"] as Boolean,
        isBookmark  = map["isBookmark"] as Boolean,
        isShowMenu = map[ "isShowMenu"] as Boolean,
       isBigText  = map["isBigText"] as Boolean,
       isDarkMode  = map["isDarkMode"] as Boolean,
       isSearch  = map["isSearch"] as Boolean,
        searchQuery  = map[ "searchQuery"] as String?,
        searchResults = map["searchResults"] as List<Pair<Int, Int>>,
        searchPosition = map[ "searchPosition"] as Int,
       shareLink= map["shareLink"] as String?,
        title = map["title"] as String?,
       category = map[ "category"] as String?,
        categoryIcon  = map["categoryIcon"] as Any?,
         date  = map["date"] as String?,
       author = map["author"] as Any?,
       poster = map["poster"] as String?,
        content = map[ "content"] as List<MarkdownElement>,
        reviews =  map["reviews"] as List<Any>
        )
    }

}

data class BottombarData(val isLike : Boolean = false,
                         val isBookmark : Boolean = false,
                         val isShowMenu : Boolean = false,
                         val isSearch : Boolean = false,
                         val resultsCount : Int = 0,
                         val searchPosition : Int = 0)

data class SubmenuData(val isBigText : Boolean = false,
                       val isDarkMode : Boolean = false,
                       val isShowMenu: Boolean = false)


fun ArticleState.toBottombarData() : BottombarData
        = BottombarData(isLike = isLike, isShowMenu = isShowMenu, isBookmark = isBookmark, isSearch = isSearch,
                        searchPosition = searchPosition, resultsCount = searchResults.size)

fun ArticleState.toSubmenuData() : SubmenuData
        = SubmenuData(isBigText = isBigText, isDarkMode = isDarkMode, isShowMenu = isShowMenu)