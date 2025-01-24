package com.neungi.moyeo.views.aiplanning.viwmodel


data class RegionUiState(
    val layoutType: LayoutType = LayoutType.GRID,
    val selectedRegion: Boolean = false,
)

enum class LayoutType {
    GRID, HORIZONTAL
}
