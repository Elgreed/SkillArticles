package ru.skillbranch.skillarticles.ui.article

import android.content.ClipData
import android.content.ClipboardManager
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.WindowManager
import android.widget.ImageView
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.view.children
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions.circleCropTransform
import ru.skillbranch.skillarticles.R
import ru.skillbranch.skillarticles.databinding.FragmentArticleBinding
import ru.skillbranch.skillarticles.extensions.dpToIntPx
import ru.skillbranch.skillarticles.extensions.format
import ru.skillbranch.skillarticles.extensions.setMarginOptionally
import ru.skillbranch.skillarticles.ui.BaseFragment
import ru.skillbranch.skillarticles.ui.custom.ArticleSubmenu
import ru.skillbranch.skillarticles.ui.custom.Bottombar
import ru.skillbranch.skillarticles.ui.delegates.viewBinding
import ru.skillbranch.skillarticles.viewmodels.article.*

class ArticleFragment :
    BaseFragment<ArticleState, ArticleViewModel, FragmentArticleBinding>(R.layout.fragment_article),
    IArticleView {

    override val viewModel: ArticleViewModel by viewModels()
    override val viewBinding: FragmentArticleBinding by viewBinding(FragmentArticleBinding::bind)

    private lateinit var toolbar: Toolbar
    private lateinit var bottombar: Bottombar
    private lateinit var submenu: ArticleSubmenu
    private lateinit var searchView: SearchView

    private val logoSize: Int by lazy { dpToIntPx(40) }
    private val cornerRadius: Int by lazy { dpToIntPx(8) }

    private val args: ArticleFragmentArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun setupCopyListener() {
        viewBinding.tvTextContent.setCopyListener { copy ->
            val clipboard = getSystemService(requireContext(), ClipboardManager::class.java)
            val clip = ClipData.newPlainText("Copied code", copy)
            clipboard?.setPrimaryClip(clip)
            viewModel.handleCopyCode()
        }
    }

    override fun onClickMessageSend() {
        TODO("Not yet implemented")
    }

    override fun renderUi(state: ArticleState) {
        root.delegate.localNightMode =
            if (state.isDarkMode) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO

        with(viewBinding.tvTextContent) {
            textSize = if (state.isBigText) 18f else 14f
            isLoading = state.content.isEmpty()
            setContent(state.content)
        }

        if (state.isLoadingContent) return

        if (state.isSearch) {
            renderSearchResult(state.searchResults)
            renderSearchPosition(state.searchPosition, state.searchResults)
        } else clearSearchResult()
    }

    override fun setupBottombar() {
        with(bottombar) {
            btnLike.setOnClickListener { viewModel.handleLike() }
            btnBookmark.setOnClickListener { viewModel.handleBookmark() }
            btnShare.setOnClickListener { viewModel.handleShare() }
            btnSettings.setOnClickListener { viewModel.handleToggleMenu() }

            btnResultUp.setOnClickListener {
                //Что будет, если сбросить фокус вьюхи, у которой нет фокуса?
                // Может быть ничего страшного и не произойдет, но я бы на всякий случай добавил
                // проверку на наличие фокуса, дабы избжать лишних действий во вью.
                if (searchView.hasFocus()) searchView.clearFocus()
                viewModel.handleUpResult()
            }

            btnResultDown.setOnClickListener {
                if (searchView.hasFocus()) searchView.clearFocus()
                viewModel.handleDownResult()
            }

            btnSearchClose.setOnClickListener {
                viewModel.handleSearchMode(false)
            }
        }
    }

    override fun renderBottombar(data: BottombarData) {
        with(bottombar) {
            btnSettings.isChecked = data.isShowMenu
            btnLike.isChecked = data.isLike
            btnBookmark.isChecked = data.isBookmark
        }

        if (data.isSearch) showSearchBar(data.resultsCount, data.searchPosition)
        else hideSearchBar()
    }

    override fun renderSearchResult(searchResult: List<Pair<Int, Int>>) {
        viewBinding.tvTextContent.renderSearchResult(searchResult)
    }

    override fun renderSearchPosition(searchPosition: Int, searchResult: List<Pair<Int, Int>>) {
        viewBinding.tvTextContent.renderSearchPosition(searchResult.getOrNull(searchPosition))
    }

    override fun clearSearchResult() {
        viewBinding.tvTextContent.clearSearchResult()
    }

    override fun showSearchBar(resultsCount: Int, searchPosition: Int) {
        with(bottombar) {
            setSearchState(true)
            setSearchInfo(resultsCount, searchPosition)
        }
        viewBinding.scroll.setMarginOptionally(bottom = dpToIntPx(56))
    }

    override fun hideSearchBar() {
        //Для одного вызова метода необязательно использовать блок with.
        bottombar.setSearchState(false)
        viewBinding.scroll.setMarginOptionally(bottom = dpToIntPx(0))
    }

    override fun setupSubmenu() {
        with(submenu) {
            switchMode.setOnClickListener { viewModel.handleNightMode() }
            btnTextDown.setOnClickListener { viewModel.handleDownText() }
            btnTextUp.setOnClickListener { viewModel.handleUpText() }
        }
    }

    override fun setupToolbar() {
        toolbar.setLogo(R.drawable.logo_placeholder)

        val logo = toolbar.children.find { it is AppCompatImageView } as? ImageView
        logo ?: return
        logo.scaleType = ImageView.ScaleType.CENTER_CROP
        val lp = logo?.layoutParams as? Toolbar.LayoutParams
        lp?.let {
            //лучше получить размер один раз, чтобы избежать лишних вычислений.
            it.width = logoSize
            it.height = logoSize
            it.marginEnd = this.dpToIntPx(16)
            logo.layoutParams = it
        }

        toolbar.subtitle = args.category

        Glide.with(this)
            .load(args.categoryIcon)
            .apply(circleCropTransform())
            .override(logoSize)
            .into(logo)
    }

    override fun renderSubmenu(data: SubmenuData) {
        with(submenu) {
            btnTextDown.isChecked = !data.isBigText
            btnTextUp.isChecked = data.isBigText
            switchMode.isChecked = data.isDarkMode
        }
        if (data.isShowMenu) submenu.open() else submenu.close()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_search, menu)
        val menuItem = menu.findItem(R.id.action_search)
        searchView = (menuItem.actionView as SearchView)
        searchView.queryHint = getString(R.string.article_search_placeholder)
        // restore SearchView
        if (viewModel.currentState.isSearch) {
            menuItem.expandActionView()
            searchView.setQuery(viewModel.currentState.searchQuery, false)
            searchView.requestFocus()
        } else {
            searchView.clearFocus()
        }

        menuItem?.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
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
                searchView.clearFocus()
                viewModel.handleSearch(query)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (!newText.isNullOrBlank()) {
                    viewModel.handleSearch(newText)
                }
                return true
            }
        })

        return super.onCreateOptionsMenu(menu, inflater)
    }

    override fun setupViews() {
        setupCopyListener()

        with(viewBinding) {
            Glide.with(this@ArticleFragment)
                .load(args.authorAvatar)
                .placeholder(R.drawable.logo_placeholder)
                .apply(circleCropTransform())
                .override(logoSize)
                .into(ivAuthorAvatar)

            Glide.with(this@ArticleFragment)
                .load(args.poster)
                .placeholder(R.drawable.poster_placeholder)
                .transform(CenterCrop(), RoundedCorners(cornerRadius))
                .into(ivPoster)

            tvTitle.text = args.title
            tvAuthor.text = args.author
            tvDate.text = args.date.format()
        }
    }

    override fun setupActivityViews() {
        root.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        root.viewBinding.navView.isVisible = false
        toolbar = root.viewBinding.toolbar
        bottombar = Bottombar(requireContext())
        submenu = ArticleSubmenu(requireContext())
        root.viewBinding.coordinatorContainer.addView(bottombar)
        root.viewBinding.coordinatorContainer.addView(submenu)
        setupToolbar()
        setupBottombar()
        setupSubmenu()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        root.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)
        root.viewBinding.navView.isVisible = true
        toolbar.logo = null
        toolbar.subtitle = null
        root.viewBinding.coordinatorContainer.removeView(bottombar)
        root.viewBinding.coordinatorContainer.removeView(submenu)
    }

    override fun observeViewModelData() {
        viewModel.observeSubState(this, ArticleState::toBottombarData, ::renderBottombar)
        viewModel.observeSubState(this, ArticleState::toSubmenuData, ::renderSubmenu)
    }
}