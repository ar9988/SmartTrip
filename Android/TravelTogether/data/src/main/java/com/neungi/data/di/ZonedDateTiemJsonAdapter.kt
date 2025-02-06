package com.neungi.data.di

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class ZonedDateTimeJsonAdapter {

    private val formatter = DateTimeFormatter.ISO_ZONED_DATE_TIME

    @FromJson
    fun fromJson(json: String): ZonedDateTime {
        return ZonedDateTime.parse(json, formatter)
    }

    @ToJson
    fun toJson(date: ZonedDateTime): String {
        return date.format(formatter)
    }
}
