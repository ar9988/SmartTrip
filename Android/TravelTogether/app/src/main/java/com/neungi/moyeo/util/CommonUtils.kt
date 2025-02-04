package com.neungi.moyeo.util

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Date

object CommonUtils {

    private val emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}\$".toRegex()
    private val passwordRegex = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*()\\-_=+{}\\[\\]|;:'\",.<>?/]).{8,16}$".toRegex()
    private val phoneNumberRegex = "^010-\\d{4}-\\d{4}\$".toRegex()

    fun validateEmail(email: CharSequence): Boolean = emailRegex.matches(email)

    fun validatePassword(password: CharSequence): Boolean = passwordRegex.matches(password)

    fun validatePhoneNumber(phoneNumber: CharSequence): Boolean = phoneNumberRegex.matches(phoneNumber)

    fun convertToDegree(value: String): Double {
        val dms = value.split(",", limit = 3)
        val degrees = dms[0].split("/").let { it[0].toDouble() / it[1].toDouble() }
        val minutes = dms[1].split("/").let { it[0].toDouble() / it[1].toDouble() }
        val seconds = dms[2].split("/").let { it[0].toDouble() / it[1].toDouble() }

        return degrees + (minutes / 60) + (seconds / 3600)
    }

    fun convertToYYYYMMDD(date: LocalDate?): String =
        (date ?: LocalDate.now()).format(DateTimeFormatter.ofPattern("yyyyMMdd"))
}