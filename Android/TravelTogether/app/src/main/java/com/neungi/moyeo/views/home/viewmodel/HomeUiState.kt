package com.neungi.moyeo.views.home.viewmodel

import com.neungi.domain.model.Festival
import com.neungi.domain.model.Place

data class HomeUiState(
    val festivals: List<Festival> = emptyList(),
    val places: List<Place> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)