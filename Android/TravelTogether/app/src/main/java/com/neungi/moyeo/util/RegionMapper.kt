package com.neungi.moyeo.util


import android.content.Context
import com.neungi.data.repository.festival.model.RegionSubRegions
import com.neungi.moyeo.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class RegionMapper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val specialCityMap = mapOf(
        "서울특별시" to "1",
        "인천광역시" to "2",
        "대전광역시" to "3",
        "대구광역시" to "4",
        "광주광역시" to "5",
        "부산광역시" to "6",
        "울산광역시" to "7",
        "세종특별자치시" to "8",
        "제주특별자치도" to "39"
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
    private val provinceFullMap = mapOf(
        "경기도" to "31",
        "강원특별자치도" to "32",
        "충청북도" to "33",
        "충청남도" to "34",
        "경상북도" to "35",
        "경상남도" to "36",
        "전북특별자치도" to "37",
        "전라남도" to "38"
    )

    private val regionDrawableMap = mapOf(
        "1" to R.drawable.img_region_seoul,
        "2" to R.drawable.img_region_incheon,
        "3" to R.drawable.img_region_daejeon,
        "4" to R.drawable.img_region_daegu,
        "5" to R.drawable.img_region_gwangju,
        "6" to R.drawable.img_region_busan,
        "7" to R.drawable.img_region_ulsan,
        "8" to R.drawable.img_region_sejong,
        "31" to R.drawable.img_region_gyeonggi,
        "32" to R.drawable.img_region_gangwon,
        "33" to R.drawable.img_region_chungbuk,
        "34" to R.drawable.img_region_chungnam,
        "35" to R.drawable.img_region_gyeongbuk,
        "36" to R.drawable.img_region_gyeongnam,
        "37" to R.drawable.img_region_jeonbuk,
        "38" to R.drawable.img_region_jeonnam,
        "39" to R.drawable.img_region_jeju
    )


    private val allRegionDrawables = regionDrawableMap.values.toList()

    fun getRegionDrawable(regionName: String): Int {
        // 지역 이름으로 코드를 찾음
        val regionCode = provinceFullMap[regionName] ?: specialCityMap[regionName]

        return when {
            // 매핑된 이미지가 있으면 해당 이미지 반환
            regionCode != null -> regionDrawableMap[regionCode] ?: getRandomDrawable()
            // 매핑된 이미지가 없으면 랜덤 이미지 반환
            else -> getRandomDrawable()
        }
    }

    private fun getRandomDrawable(): Int {
        return allRegionDrawables.random()
    }

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