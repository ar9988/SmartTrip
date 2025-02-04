package com.neungi.data.repository.aiplanning.datasource

import com.neungi.data.repository.aiplanning.model.RegionSubRegions

interface AiPlanningLocalRegionDataSource {
    fun getAllSubRegions(): RegionSubRegions
}