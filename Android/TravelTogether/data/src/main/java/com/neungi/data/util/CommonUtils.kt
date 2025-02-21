package com.neungi.data.util

import android.util.Log
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

object CommonUtils {

    fun formatDateTime(input: String): String {
        if (input == "") return "1970.01.01"
        val inputFormatter =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm").withZone(ZoneId.of("Asia/Seoul"))
        val outputFormatter =
            DateTimeFormatter.ofPattern("yyyy.MM.dd").withZone(ZoneId.of("Asia/Seoul"))

        val dateTime = LocalDateTime.parse(input, inputFormatter)
        return dateTime.format(outputFormatter)
    }

    fun formatDateTimeHourMinute(input: String): String {
        if (input.isEmpty()) return "1970.01.01 00:00"

        val inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm[:ss]")
        val outputFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm")

        val zonedDateTime =
            ZonedDateTime.parse(input.split(".").first(), inputFormatter.withZone(ZoneId.of("UTC")))

        val koreaTime = zonedDateTime.withZoneSameInstant(ZoneId.of("Asia/Seoul"))

        return koreaTime.format(outputFormatter)
    }

    fun formatDateTimeHourMinuteSecond(input: String): String {
        if (input == "") return "1970.01.01 00:00:00"
        Log.d("CommonUtils", "formatDateTimeHourMinuteSecond: $input")
        val inputFormatter =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss").withZone(ZoneId.of("Asia/Seoul"))
        val outputFormatter =
            DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm:ss").withZone(ZoneId.of("Asia/Seoul"))

        val dateTime = LocalDateTime.parse(input.split(".").first(), inputFormatter)
        return dateTime.format(outputFormatter)
    }
}