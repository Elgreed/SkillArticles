package ru.skillbranch.skillarticles.viewmodels

import androidx.annotation.IdRes
import androidx.lifecycle.Observer
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavOptions
import ru.skillbranch.skillarticles.MainFlowDirections
import ru.skillbranch.skillarticles.R
import ru.skillbranch.skillarticles.data.repositories.RootRepository

class RootViewModel(savedStateHandle: SavedStateHandle) :
    BaseViewModel<RootState>(RootState(), savedStateHandle), IRootViewModel {

    companion object {
        val privateDestinations = listOf(R.id.nav_profile)
    }

    private val repository: RootRepository = RootRepository()

    // for live data updated
    private val selfObserver = Observer<RootState> { state -> /*do nothing*/ }

    init {
        subscribeOnDataSource(repository.isAuth()) { isAuth, currentState ->
            currentState.copy(isAuth = isAuth)
        }
        state.observeForever(selfObserver)
    }

    override fun onCleared() {
        super.onCleared()
        state.removeObserver(selfObserver)
    }

    override fun topLevelNavigate(@IdRes resId: Int) {
        val options = NavOptions.Builder()
            .setLaunchSingleTop(true)
            .setEnterAnim(R.animator.nav_default_enter_anim)
            .setExitAnim(R.animator.nav_default_exit_anim)
            .setPopEnterAnim(R.animator.nav_default_pop_enter_anim)
            .setPopExitAnim(R.animator.nav_default_pop_exit_anim)

        if (privateDestinations.contains(resId) && !currentState.isAuth) {
            val action = MainFlowDirections.startLogin(resId)
            navigate(NavCommand.Action(action))
        } else {
            navigate(NavCommand.TopLevel(resId, options.build()))
        }
    }

}

data class RootState(val isAuth: Boolean = false) : VMState