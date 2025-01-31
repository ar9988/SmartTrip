package com.neungi.moyeo.views.aiplanning.viewmodel

import com.neungi.moyeo.util.EmptyState

data class SearchUiState(
    val searchTextState: EmptyState = EmptyState.EMPTY
) {
    val searchTextIsEmpty: Boolean = (searchTextState == EmptyState.EMPTY)
}