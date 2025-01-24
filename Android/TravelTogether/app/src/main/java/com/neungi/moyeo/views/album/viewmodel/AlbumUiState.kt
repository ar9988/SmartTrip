package com.neungi.moyeo.views.album.viewmodel

import com.neungi.moyeo.util.EmptyState

data class AlbumUiState(
    val photoUploadValidState: EmptyState = EmptyState.EMPTY
) {
    val isPhotoUploadBtnEnable: Boolean = (photoUploadValidState == EmptyState.NONE)
}
