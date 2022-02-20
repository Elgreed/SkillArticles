package ru.skillbranch.skillarticles.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.os.PersistableBundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import androidx.activity.viewModels
import androidx.annotation.VisibleForTesting
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.snackbar.Snackbar
import ru.skillbranch.skillarticles.R
import ru.skillbranch.skillarticles.databinding.ActivityRootBinding
import ru.skillbranch.skillarticles.extensions.dpToIntPx
import ru.skillbranch.skillarticles.extensions.hideKeyboard
import ru.skillbranch.skillarticles.extensions.setMarginOptionally
import ru.skillbranch.skillarticles.ui.delegates.viewBinding
import ru.skillbranch.skillarticles.viewmodels.*

class RootActivity : AppCompatActivity(), IArticleView {
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    var viewModelFactory: ViewModelProvider.Factory = ViewModelFactory(this, "0")
    private val viewModel: ArticleViewModel by viewModels { viewModelFactory }
    private val vb: ActivityRootBinding by viewBinding(ActivityRootBinding::inflate)
    private val vbBottomBar
        get() = vb.bottombar
    private val vbSubmenu
        get() = vb.submenu
    private lateinit var searchView: SearchView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(vb.root)
        setupToolbar()
        setupBottomBar()
        setupSubmenu()
        setupCopyListener()
        viewModel.observeState(this, ::renderUi)
        viewModel.observeSubState(this, ArticleState::toBottombarData, ::renderBotombar)
        viewModel.observeSubState(this, ArticleState::toSubmenuData, ::renderSubmenu)
        viewModel.observeNotifications(this) {
            renderNotification(it)
        }
    }

    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        viewModel.saveState()
        super.onSaveInstanceState(outState, outPersistentState)
    }


    private fun renderNotification(notify: Notify) {
        val snackbar = Snackbar.make(vb.coordinatorContainer, notify.message, Snackbar.LENGTH_LONG)
            .setAnchorView(vb.bottombar)
        when (notify) {
            is Notify.TextMessage -> {
            }
            is Notify.ActionMessage -> {
                snackbar.setActionTextColor(getColor(R.color.color_accent_dark))
                snackbar.setAction(notify.actionLabel) {
                    notify.actionHandler.invoke()
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

    override fun setupSubmenu() {
        with(vbSubmenu) {
            btnTextUp.setOnClickListener { viewModel.handleUpText() }
            btnTextDown.setOnClickListener { viewModel.handleDownText() }
            switchMode.setOnClickListener { viewModel.handleNightMode() }
        }
    }

    override fun setupBottomBar() {
        with(vbBottomBar) {
            btnLike.setOnClickListener {viewModel.handleLike()}
            btnBookmark.setOnClickListener { viewModel.handleBookmark() }
            btnSettings.setOnClickListener { viewModel.handleToggleMenu() }
            btnShare.setOnClickListener { viewModel.handleShare() }
            btnResultUp.setOnClickListener {
                if (!vb.tvTextContent.hasFocus()) {
                    vb.tvTextContent.requestFocus()
                }
                hideKeyboard(it)
                viewModel.handleUpResult()
            }
            btnResultDown.setOnClickListener {
                if (!vb.tvTextContent.hasFocus()) {
                    vb.tvTextContent.requestFocus()
                }
                hideKeyboard(it)
                viewModel.handleDownResult()
            }
            btnSearchClose.setOnClickListener {
                viewModel.handleSearchMode(false)
                invalidateOptionsMenu()
            }
        }
    }

    override fun renderBotombar(data: BottombarData) {
        with(vbBottomBar) {
            btnLike.isChecked = data.isLike
            btnBookmark.isChecked = data.isBookmark
            btnSettings.isChecked = data.isShowMenu
        }
        if (data.isSearch) {
            showSearchBar(data.resultsCount, data.searchPosition)
            with(vb.toolbar) {
                (layoutParams as AppBarLayout.LayoutParams).scrollFlags =
                    AppBarLayout.LayoutParams.SCROLL_FLAG_NO_SCROLL
            }
        } else {
            hideSearchBar()
            with(vb.toolbar) {
                (layoutParams as AppBarLayout.LayoutParams).scrollFlags =
                    AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL or
                            AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS_COLLAPSED
            }
        }
    }

    override fun renderSubmenu(data: SubmenuData) {
        with(vbSubmenu) {
            switchMode.isChecked = data.isDarkMode
            btnTextDown.isChecked = !data.isBigText
            btnTextUp.isChecked = data.isBigText
        }
        if (data.isShowMenu) vb.submenu.open() else vb.submenu.close()
    }

    override fun renderUi(data: ArticleState) {
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
            if (data.categoryIcon != null) logo =
                ContextCompat.getDrawable(context, data.categoryIcon as Int)
        }

        if (data.isLoadingContent) return
        if (data.isSearch) {
            renderSearchResult(data.searchResults)
            renderSearchPosition(data.searchPosition, data.searchResults)
        } else clearSearchResult()
    }

    override fun setupToolbar() {
        with(vb.toolbar) {
            setSupportActionBar(this)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            val logo = if (childCount > 2) this.getChildAt(2) as ImageView else null
            logo?.scaleType = ImageView.ScaleType.CENTER_CROP
            println(logo?.id)
            val lp = logo?.layoutParams as? Toolbar.LayoutParams
            lp?.let {
                it.width = this@RootActivity.dpToIntPx(40)
                it.height = this@RootActivity.dpToIntPx(40)
                it.marginEnd = this@RootActivity.dpToIntPx(16)
                logo.layoutParams = it
            }
        }
    }

    override fun renderSearchResult(searchResult: List<Pair<Int, Int>>) {
        vb.tvTextContent.renderSearchResult(searchResult)
    }

    override fun renderSearchPosition(searchPosition: Int, searchResult: List<Pair<Int, Int>>) {
        vb.tvTextContent.renderSearchPosition(searchResult.getOrNull(searchPosition))
    }

    override fun clearSearchResult() {
        vb.tvTextContent.clearSearchResult()
    }

    override fun showSearchBar(resultsCount: Int, searchPosition: Int) {
        with(vb.bottombar) {
            setSearchState(true)
            setSearchInfo(resultsCount, searchPosition)
        }
        vb.scroll.setMarginOptionally(bottom = dpToIntPx(56))
    }

    override fun hideSearchBar() {
        with(vb.bottombar) {
            setSearchState(false)
        }
        vb.scroll.setMarginOptionally(bottom = dpToIntPx(0))
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_search, menu)
        val menuItem = menu?.findItem(R.id.action_search)

        searchView = (menuItem?.actionView as? SearchView)!!
        searchView.queryHint = getString(R.string.article_search_placeholder)

        if (viewModel.currentState.isSearch) {
            menuItem.expandActionView()
            searchView.setQuery(viewModel.currentState.searchQuery, false)
            searchView.requestFocus()
        } else {
            searchView.clearFocus()
        }

        menuItem.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem?): Boolean {
                viewModel.handleSearchMode(true)
                return true
            }

            override fun onMenuItemActionCollapse(item: MenuItem?): Boolean {
                viewModel.handleSearchMode(false)
                return true
            }
        })

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                viewModel.handleSearch(query)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.handleSearch(newText)
                return true
            }
        })
        return super.onCreateOptionsMenu(menu)
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