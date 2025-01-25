package com.neungi.moyeo.util

object CommonUtils {

    private val emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}\$".toRegex()
    private val passwordRegex = "^(?=.*[A-Za-z])(?=.*\\\\d)(?=.*[!@#$%^&*(),.?\":{}|<>])[A-Za-z\\\\d!@#$%^&*(),.?\":{}|<>]{8,16}$".toRegex()

    fun validateEmail(email: CharSequence): Boolean = emailRegex.matches(email)

    fun validatePassword(password: CharSequence): Boolean = passwordRegex.matches(password)
}