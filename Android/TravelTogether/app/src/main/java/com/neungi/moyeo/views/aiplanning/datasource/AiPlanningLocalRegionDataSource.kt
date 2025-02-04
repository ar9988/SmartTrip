package com.neungi.moyeo.views.aiplanning.datasource

//import android.content.Context
//import com.neungi.data.repository.aiplanning.datasource.AiPlanningLocalRegionDataSource
//import com.neungi.data.repository.aiplanning.model.RegionSubRegions
//import com.neungi.moyeo.R
//import dagger.hilt.android.qualifiers.ApplicationContext
//import javax.inject.Inject
//
//class RegionLocalDataSource @Inject constructor(
//    @ApplicationContext private val context: Context
//) : AiPlanningLocalRegionDataSource {
//    override fun getAllSubRegions(): RegionSubRegions {
//        return RegionSubRegions(
//            gyeonggi = context.resources.getStringArray(R.array.local_gyeonggi).toSet(),
//            gangwon = context.resources.getStringArray(R.array.local_gangwon).toSet(),
//            gyeongbuk = context.resources.getStringArray(R.array.local_gyeongbuk).toSet(),
//            gyeongnam = context.resources.getStringArray(R.array.local_gyeongnam).toSet(),
//            jeonbuk = context.resources.getStringArray(R.array.local_jeonbuk).toSet(),
//            jeonnam = context.resources.getStringArray(R.array.local_jeonnam).toSet(),
//            chungbuk = context.resources.getStringArray(R.array.local_chungbuk).toSet()
//        )
//    }
//}