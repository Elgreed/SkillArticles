package ru.skillbranch.skillarticles.viewmodels.auth

interface IAuthViewModel {
    /**
     * навигация на страницу авторизации
     **/
    fun navigateToPrivacy()

    /**
     * навигация на страницу регистрации
     **/
    fun navigateToRegistration()

    /**
     * обработка авторизации пользователя, если авторизация успешна перенаправить пользователя на
     * предидущее местоназначения (если это был приватный destination) то на него, иначе pop inclusive true
     **/
    fun handleLogin(login: String, password: String)

    /**
     * обработка регистрации пользователя, если авторизация успешна перенаправить пользователя на
     * предидущее местоназначения (если это был приватный destination) то на него, иначе pop inclusive true
     **/
    fun handleRegistration(name: String, login: String, password: String)

    /**
     * сброс ошибок валидации
     **/
    fun resetErrors()
}