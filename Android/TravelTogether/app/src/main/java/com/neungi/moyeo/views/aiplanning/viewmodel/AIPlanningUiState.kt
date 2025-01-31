package com.neungi.moyeo.views.aiplanning.viewmodel

import com.neungi.moyeo.util.EmptyState

data class AIPlanningUiState(
    val destinationSelectState: EmptyState = EmptyState.EMPTY
) {
    val destinationSelected: Boolean = (destinationSelectState == EmptyState.NONE)
}
