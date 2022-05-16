package ru.skillbranch.skillarticles.ui

import android.os.Bundle
import android.view.View
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.viewbinding.ViewBinding
import ru.skillbranch.skillarticles.viewmodels.BaseViewModel
import ru.skillbranch.skillarticles.viewmodels.VMState

abstract class BaseFragment<S, T : BaseViewModel<S>, B : ViewBinding>(@LayoutRes layout: Int) :
    Fragment(layout), LifecycleObserver where S : VMState {
    protected val root
        get() = requireActivity() as RootActivity

    abstract val viewModel: T
    abstract val viewBinding: B

    // render ui from state
    abstract fun renderUi(data: S)
    abstract fun setupViews()

    // access to toolbar, call when activity inflated
    open fun setupActivityViews() {}

    // observer substates this if need
    open fun observeViewModelData() {}

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        requireActivity().lifecycle.addObserver(this)
        viewModel.observeNotifications(root, root::renderNotification)
        viewModel.observeNavigation(root, root::handleNavigation)
        viewModel.observeState(viewLifecycleOwner, ::renderUi)

        observeViewModelData()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        requireActivity().lifecycle.removeObserver(this)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        viewModel.saveState()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    private fun activityInflated() {
        // show appbar if hidden due to scroll behavior
        root.viewBinding.appbar.setExpanded(true, true)
        root.viewBinding.toolbar.logo =
            null // remove glitch destroy view -> move clear toolbar logic
        root.viewBinding.toolbar.subtitle = null
        setupActivityViews()
    }
}