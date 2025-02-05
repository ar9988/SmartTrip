package com.neungi.moyeo.views.aiplanning.viewmodel


import android.content.Context
import com.neungi.data.repository.festival.model.RegionSubRegions
import com.neungi.moyeo.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class RegionMapper @Inject constructor(
    @ApplicationContext private val context: Context
) {
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

    fun getRegionCode(regionName: String): String {
        // 1. 특별시/광역시 확인
        specialCityMap[regionName]?.let { return it }

        // 2. 도 직접 매핑 확인
        provinceMap[regionName]?.let { return it }

        // 3. 시군구인 경우 상위 도의 코드 반환
        return getParentRegionCode(regionName)
    }

    private fun getParentRegionCode(subRegionName: String): String {
        val subRegions = RegionSubRegions(
            gyeonggi = context.resources.getStringArray(R.array.local_gyeonggi).toSet(),
            gangwon = context.resources.getStringArray(R.array.local_gangwon).toSet(),
            gyeongbuk = context.resources.getStringArray(R.array.local_gyeongbuk).toSet(),
            gyeongnam = context.resources.getStringArray(R.array.local_gyeongnam).toSet(),
            jeonbuk = context.resources.getStringArray(R.array.local_jeonbuk).toSet(),
            jeonnam = context.resources.getStringArray(R.array.local_jeonnam).toSet(),
            chungbuk = context.resources.getStringArray(R.array.local_chungbuk).toSet()
        )

        return when {
            subRegions.gyeonggi.contains(subRegionName) -> "31"
            subRegions.gangwon.contains(subRegionName) -> "32"
            subRegions.gyeongbuk.contains(subRegionName) -> "35"
            subRegions.gyeongnam.contains(subRegionName) -> "36"
            subRegions.jeonbuk.contains(subRegionName) -> "37"
            subRegions.jeonnam.contains(subRegionName) -> "38"
            subRegions.chungbuk.contains(subRegionName) -> "33"
            else -> "1"
        }
    }
}