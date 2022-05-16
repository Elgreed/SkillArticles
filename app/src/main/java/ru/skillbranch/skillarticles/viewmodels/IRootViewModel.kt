package ru.skillbranch.skillarticles.viewmodels

import androidx.annotation.IdRes

interface IRootViewModel {
    /**
     * обработка навигации верхнего уровня (перемещение по BottomNavigationView)
     * если точка назначения требует авторизации то переключить пользователя на
     * Auth flow
     **/
    fun topLevelNavigate(@IdRes resId: Int)
}