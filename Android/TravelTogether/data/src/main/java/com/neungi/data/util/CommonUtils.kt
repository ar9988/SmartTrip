package com.neungi.data.util

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object CommonUtils {

    fun formatDateTime(input: String): String {
        if (input == "") return "1970.01.01"
        val inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")
        val outputFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd")

        val dateTime = LocalDateTime.parse(input, inputFormatter)
        return dateTime.format(outputFormatter)
    }
}