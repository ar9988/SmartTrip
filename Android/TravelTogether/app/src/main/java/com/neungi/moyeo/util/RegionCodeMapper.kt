package com.neungi.moyeo.util

import android.content.Context
import com.neungi.moyeo.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RegionCodeMapper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val gyeonggiRegions = context.resources.getStringArray(R.array.local_gyeonggi).toSet()
    private val gangwonRegions = context.resources.getStringArray(R.array.local_gangwon).toSet()
    private val provinceMap = mapOf(
        "경기" to "31",
        "강원" to "32",
        "충북" to "33",
        "충남" to "34",
        "경북" to "35",
        "경남" to "36",
        "전북" to "37",
        "전남" to "38"
    )

    // 특별시/광역시 매핑
    private val specialCityMap = mapOf(
        "서울" to "1",
        "인천" to "2",
        "대전" to "3",
        "대구" to "4",
        "광주" to "5",
        "부산" to "6",
        "울산" to "7",
        "세종" to "8",
        "제주" to "39"
    )

    fun getCodeByName(regionName: String): String? {
        // 1. 특별시/광역시 확인
        if (specialCityMap.containsKey(regionName)) {
            return specialCityMap[regionName]
        }


        if (provinceMap.containsKey(regionName)) {
            return provinceMap[regionName]
        }

        val resources = context.resources


        val regionArrays = mapOf(
            R.array.local_gyeonggi to "31",
            R.array.local_gangwon to "32",
            R.array.local_gyeongbuk to "35",
            R.array.local_gyeongnam to "36",
            R.array.local_jeonbuk to "37",
            R.array.local_jeonnam to "38",
            R.array.local_chungbuk to "33"
        )

        for ((arrayId, code) in regionArrays) {
            val regions = resources.getStringArray(arrayId)
            if (regions.contains(regionName)) {
                return code
            }
        }

        return null
    }
}