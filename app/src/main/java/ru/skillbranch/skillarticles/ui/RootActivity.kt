package ru.skillbranch.skillarticles.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Selection
import android.text.Spannable
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import ru.skillbranch.skillarticles.ui.custom.spans.SearchFocusSpan
import ru.skillbranch.skillarticles.ui.custom.spans.SearchSpan

import android.widget.ImageView
import android.widget.TextView

import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.VisibleForTesting
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.core.text.getSpans
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar

import ru.skillbranch.skillarticles.R
import ru.skillbranch.skillarticles.databinding.ActivityRootBinding
import ru.skillbranch.skillarticles.databinding.LayoutBottombarBinding
import ru.skillbranch.skillarticles.databinding.LayoutSubmenuBinding
import ru.skillbranch.skillarticles.extensions.dpToIntPx
import ru.skillbranch.skillarticles.extensions.setMarginOptionally
import ru.skillbranch.skillarticles.ui.custom.markdown.MarkdownBuilder

import ru.skillbranch.skillarticles.ui.delegates.AttrValue
import ru.skillbranch.skillarticles.ui.delegates.viewBinding
import ru.skillbranch.skillarticles.viewmodels.*

class RootActivity : AppCompatActivity(), IArticleView {

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    var viewModelFactory: ViewModelProvider.Factory = ViewModelFactory(this, "0")
    private val viewModel : ArticleViewModel by viewModels<ArticleViewModel> { viewModelFactory }
    private val vb : ActivityRootBinding by viewBinding(ActivityRootBinding::inflate)
    private lateinit var searchView : SearchView

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    val bgColor by AttrValue(R.attr.colorSecondary)

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    val fgColor by AttrValue(R.attr.colorOnSecondary)

    private val TAG = "LiveData"

    private val submenuBinding : LayoutSubmenuBinding
             get() = vb.submenu.submenuBinding

    private val bottombarBinding : LayoutBottombarBinding
             get() = vb.bottombar.bottombarBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupToolbar()
        setupBottombar()
        setupSubmenu()
        setupCopyListener()

        viewModel.observeState(this, ::renderUi)

        viewModel.observeNotifications(this, ::renderNotification)

        viewModel.observeSubState(this, ArticleState::toBottombarData, ::renderBotombar)

        viewModel.observeSubState(this, ArticleState::toSubmenuData, ::renderSubmenu)

    }

    override fun onSaveInstanceState(outState: Bundle) {
        viewModel.saveState()
        super.onSaveInstanceState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
       // viewModel.restoreState()
        super.onRestoreInstanceState(savedInstanceState)
    }

    private fun renderNotification(notify: Notify) {
        val snackbar = Snackbar.make(vb.coordinatorContainer, notify.message, Snackbar.LENGTH_LONG)
                .setAnchorView(vb.bottombar)


        when (notify) {
            is Notify.ActionMessage -> {
                snackbar.setTextColor(getColor(R.color.color_accent_dark))
                snackbar.setAction(notify.actionLabel) {
                    notify.actionHandler?.invoke()
                }
            }

            is Notify.ErrorMessage -> {
                with(snackbar) {
                    setBackgroundTint(getColor(R.color.design_default_color_error))
                    setTextColor(getColor(android.R.color.white))
                    setActionTextColor(getColor(android.R.color.white))
                    setAction(notify.errLabel) {
                        notify.errHandler?.invoke()
                    }
                }
            }
        }

        snackbar.show()

    }

    override fun renderUi(data : ArticleState) {

        Log.d(TAG, "renderUi: ${data}")
       delegate.localNightMode =
               if (data.isDarkMode) AppCompatDelegate.MODE_NIGHT_YES
               else AppCompatDelegate.MODE_NIGHT_NO

        with(vb.tvTextContent) {
            textSize = if (data.isBigText) 18f else 14f
            isLoading = data.content.isEmpty()
            setContent(data.content)
        }

        with(vb.toolbar) {
            title = data.title ?: "loading"
            subtitle = data.category ?: "loading"

            if (data.categoryIcon != null) logo = getDrawable(data.categoryIcon as Int)

        }

        if (data.isLoadingContent) return

        if (data.isSearch) {
             renderSearchResult(data.searchResults)
             renderSearchPosition(data.searchPosition, data.searchResults)
        } else clearSearchResult()

    }

    override fun renderSearchResult(searchResult: List<Pair<Int, Int>>) {

        vb.tvTextContent.renderSearchResult(searchResult)

//        val content = vb.tvTextContent.text as Spannable
//
//        clearSearchResult()
//
//        searchResult.forEach {(start, end)  ->
//            content.setSpan(
//                    SearchSpan(),
//                    start,
//                    end,
//                    SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
//            )
//
//        }
    }

    override fun renderSearchPosition(searchPosition: Int, searchResult : List<Pair<Int, Int>>) {

        vb.tvTextContent.renderSearchPosition(searchResult.getOrNull(searchPosition))
//        val content = vb.tvTextContent.text as Spannable
//
//        val spans = content.getSpans<SearchSpan>()
//
//        content.getSpans<SearchFocusSpan>()
//                .forEach {
//                    content.removeSpan(it)
//                }
//
//        if (spans.isNotEmpty()) {
//            val result = spans[searchPosition]
//
//            Selection.setSelection(content, content.getSpanStart(result))
//
//            content.setSpan(
//                    SearchFocusSpan(),
//                    content.getSpanStart(result),
//                    content.getSpanEnd(result),
//                    SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
//            )
//
//        }
    }

    override fun clearSearchResult() {
       vb.tvTextContent.clearSearchResult()
//        val content = vb.tvTextContent.text as Spannable
//        content.getSpans<SearchSpan>()
//                .forEach {
//                     content.removeSpan(it)
//                }
    }

    override fun showSearchBar(resultsCount: Int, searchPosition: Int) {
        with(vb.bottombar) {
            setSearchState(true)
            setSearchInfo(resultsCount, searchPosition)
        }

        vb.scroll.setMarginOptionally(bottom = dpToIntPx(56))
    }

    override fun hideSearchBar() {
        vb.bottombar.setSearchState(false)
        vb.scroll.setMarginOptionally(bottom = dpToIntPx(0))
    }

    override fun setupBottombar() {
        bottombarBinding.btnLike.setOnClickListener {
            viewModel.handleLike()
        }

        bottombarBinding.btnBookmark.setOnClickListener {
            viewModel.handleBookmark()
        }

        bottombarBinding.btnShare.setOnClickListener {
            viewModel.handleShare()
        }

        bottombarBinding.btnSettings.setOnClickListener {
            viewModel.handleToggleMenu()
        }
    }

    override fun setupSubmenu() {

        submenuBinding.btnTextUp.setOnClickListener {
            viewModel.handleUpText()
        }

        submenuBinding.btnTextDown.setOnClickListener {
            viewModel.handleDownText()
        }

        submenuBinding.switchMode.setOnClickListener {
            viewModel.handleNightMode()
        }

    }

    override fun setupToolbar() {
        setSupportActionBar(vb.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        val logo = if (vb.toolbar.childCount > 2) vb.toolbar.getChildAt(2) as ImageView else null
        logo?.scaleType = ImageView.ScaleType.CENTER_CROP
        val lp = logo?.layoutParams as? Toolbar.LayoutParams
        lp?.let {
            it.width = this.dpToIntPx(40)
            it.height = this.dpToIntPx(40)
            it.marginEnd = this.dpToIntPx(16)
            logo.layoutParams = it
        }

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.search_menu, menu)

        val searchItem = menu.findItem(R.id.action_search)
        searchView = (searchItem.actionView as SearchView).apply {
            queryHint = "Search"
        }

        if (viewModel.currentState.isSearch) {
            searchItem.expandActionView()
            searchView.setQuery(viewModel.currentState.searchQuery, false)
            searchView.requestFocus()
        } else {
            searchView.clearFocus()
        }

//        searchItem.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
//            override fun onMenuItemActionExpand(item: MenuItem?): Boolean {
//                Log.d("SearchMODE", "onMenuItemActionExpand: ")
//                viewModel.handleSearchMode(true)
//                return true
//            }
//
//            override fun onMenuItemActionCollapse(item: MenuItem?): Boolean {
//                Log.d("SearchMODE", "onMenuItemActionCollapse: ")
//                viewModel.handleSearchMode(false)
//                return true
//            }
//
//        })
//
//
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                Log.d("Query", "onQueryTextSubmit: ${query}")
                viewModel.handleSearch(query)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                Log.d("Query", "onQueryTextChange: ${newText}")
                 newText?.let {
                     if (!it.equals(viewModel.currentState.searchQuery)) {
                         viewModel.handleSearch(it)
                     }
                 }

                return true
            }

        })
////
////        return true
////
        searchView.setOnSearchClickListener {
            Toast.makeText(this, "Search click", Toast.LENGTH_SHORT).show()
            viewModel.handleSearchMode(true)
        }
//
//        viewModel.observeState(this) {
//
//            if (it.isSearch) {
//                searchView.onActionViewExpanded()
//              //  searchItem.expandActionView()
//
//                if (!it.searchQuery.isNullOrEmpty()) {
//                    searchView.setQuery(it.searchQuery, false)
//                }
//
//            } else {
//                searchView.onActionViewCollapsed()
//              //  searchItem.collapseActionView()
//            }
//
//
//           Log.d("Query",  "${it.searchQuery}")
//        }

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            Toast.makeText(this, "Back", Toast.LENGTH_SHORT).show()
            searchView.clearFocus()
            viewModel.handleSearchMode(false)
            invalidateOptionsMenu()
        }
        return true
    }

    override fun renderBotombar(data: BottombarData) {
        Log.d(TAG, "renderBotombar: ${data} ")
         with(bottombarBinding) {
             btnSettings.isChecked = data.isShowMenu
             btnLike.isChecked = data.isLike
             btnBookmark.isChecked = data.isBookmark

             btnResultUp.setOnClickListener {
                 searchView.clearFocus()
                 viewModel.handleUpResult()
             }

             btnResultDown.setOnClickListener {
                 searchView.clearFocus()
                 viewModel.handleDownResult()
             }

             btnSearchClose.setOnClickListener {
                 searchView.clearFocus()
                 viewModel.handleSearchMode(false)
                 invalidateOptionsMenu()
             }
         }

        if (data.isSearch) showSearchBar(data.resultsCount, data.searchPosition)
        else hideSearchBar()

    }

    override fun renderSubmenu(data: SubmenuData) {
        Log.d(TAG, "renderSubmenu: ${data}")
        with(submenuBinding) {
            btnTextUp.isChecked = data.isBigText
            btnTextDown.isChecked = !data.isBigText
            switchMode.isChecked = data.isDarkMode
        }

        if (data.isShowMenu) vb.submenu.open() else vb.submenu.close()

    }

    override fun setupCopyListener() {
        vb.tvTextContent.setCopyListener { copy ->
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Copied code", copy)
            clipboard.setPrimaryClip(clip)
            viewModel.handleCopyCode()
        }
    }

}






