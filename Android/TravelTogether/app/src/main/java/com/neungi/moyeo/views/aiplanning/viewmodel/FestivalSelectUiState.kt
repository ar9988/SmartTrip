package com.neungi.moyeo.views.aiplanning.viewmodel

import com.neungi.domain.model.Festival

data class FestivalSelectUiState (
    val festival: Festival,
    val isSelected: Boolean
)